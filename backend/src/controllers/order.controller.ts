import { Request, Response, NextFunction } from 'express';
import { z } from 'zod';
import prisma from '../config/prisma.js';
import { emitToCashier, emitToTable, emitToAll } from '../socket/index.js';

const orderItemInputSchema = z.object({
  menuItemId: z.string().optional(),
  name: z.string().min(1, 'Nama item wajib diisi'),
  categoryName: z.string().default('Lainnya'),
  unitPrice: z.number().nonnegative(),
  quantity: z.number().int().positive('Jumlah minimal 1'),
  note: z.string().optional(),
});

const createOrderSchema = z.object({
  tableNumber: z.string().min(1, 'Nomor meja wajib diisi'),
  orderSource: z.enum(['EMENU', 'KASIR']).default('EMENU'),
  customerNotes: z.string().optional(),
  items: z.array(orderItemInputSchema).min(1, 'Pesanan minimal berisi 1 item'),
});

export const createOrder = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const data = createOrderSchema.parse(req.body);

    const table = await prisma.table.findUnique({
      where: { number: data.tableNumber },
    });

    if (!table) {
      return res.status(404).json({
        success: false,
        message: `Meja dengan nomor "${data.tableNumber}" tidak ditemukan`,
      });
    }

    // Hitung subtotal (bersih tanpa pajak sesuai aturan bisnis Cafe 3 Rasa)
    const subtotal = data.items.reduce((acc, item) => acc + item.unitPrice * item.quantity, 0);

    const now = new Date();
    const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '');
    const randomSuffix = Math.floor(1000 + Math.random() * 9000);
    const orderNumber = `ORD-${dateStr}-${randomSuffix}`;

    const newOrder = await prisma.$transaction(async (tx) => {
      const order = await tx.order.create({
        data: {
          orderNumber,
          tableId: table.id,
          orderSource: data.orderSource,
          customerNotes: data.customerNotes,
          subtotal: BigInt(subtotal),
          status: 'MENUNGGU_KONFIRMASI',
          paymentBillStatus: 'BELUM_BAYAR',
          items: {
            create: data.items.map((i) => ({
              menuItemId: i.menuItemId,
              name: i.name,
              categoryName: i.categoryName,
              unitPrice: BigInt(i.unitPrice),
              quantity: i.quantity,
              note: i.note,
              subtotal: BigInt(i.unitPrice * i.quantity),
            })),
          },
        },
        include: {
          items: true,
          table: true,
        },
      });

      // Update meja jadi BARU_PESAN (dot berkedip di Kasir)
      await tx.table.update({
        where: { id: table.id },
        data: {
          status: 'BARU_PESAN',
          activeSince: now,
          currentOrderId: order.id,
        },
      });

      return order;
    });

    // Real-time broadcast ke kasir
    emitToCashier('new_order', {
      orderId: newOrder.id,
      orderNumber: newOrder.orderNumber,
      tableNumber: table.number,
      orderSource: newOrder.orderSource,
      subtotal,
      itemCount: newOrder.items.reduce((sum, item) => sum + item.quantity, 0),
      items: newOrder.items.map((item) => ({
        id: item.id,
        name: item.name,
        quantity: item.quantity,
        unitPrice: Number(item.unitPrice),
        subtotal: Number(item.subtotal),
        note: item.note,
      })),
      createdAt: newOrder.createdAt.toISOString(),
    });

    emitToAll('table_status_changed', {
      tableId: table.id,
      tableNumber: table.number,
      status: 'BARU_PESAN',
    });

    return res.status(201).json({
      success: true,
      message: 'Pesanan berhasil dikirim ke kasir',
      data: {
        id: newOrder.id,
        orderNumber: newOrder.orderNumber,
        tableNumber: table.number,
        subtotal,
        status: newOrder.status,
        createdAt: newOrder.createdAt,
      },
    });
  } catch (error) {
    return next(error);
  }
};

export const confirmOrder = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { id } = req.params;

    const order = await prisma.order.findUnique({
      where: { id },
      include: { table: true },
    });

    if (!order) {
      return res.status(404).json({ success: false, message: 'Pesanan tidak ditemukan' });
    }

    const updated = await prisma.$transaction(async (tx) => {
      const ord = await tx.order.update({
        where: { id },
        data: {
          status: 'DIPROSES',
          confirmedAt: new Date(),
        },
      });

      // Update status meja jadi TERISI_BELUM_BAYAR (dot merah solid, kedip berhenti)
      await tx.table.update({
        where: { id: order.tableId },
        data: {
          status: 'TERISI_BELUM_BAYAR',
        },
      });

      return ord;
    });

    // Beritahu web meja pelanggan bahwa kasir sudah mengonfirmasi
    emitToTable(order.table.number, 'order_confirmed', {
      orderId: order.id,
      orderNumber: order.orderNumber,
      tableNumber: order.table.number,
      status: 'DIPROSES',
      confirmedAt: updated.confirmedAt,
    });

    emitToAll('table_status_changed', {
      tableId: order.table.id,
      tableNumber: order.table.number,
      status: 'TERISI_BELUM_BAYAR',
    });

    return res.json({
      success: true,
      message: `Pesanan ${order.orderNumber} berhasil dikonfirmasi kasir`,
      data: updated,
    });
  } catch (error) {
    return next(error);
  }
};

const updatePaymentSchema = z.object({
  paymentMethod: z.enum(['CASH', 'TRANSFER', 'QRIS']),
  paymentBillStatus: z.enum(['BELUM_BAYAR', 'LUNAS']).default('LUNAS'),
});

export const updatePayment = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { id } = req.params;
    const { paymentMethod, paymentBillStatus } = updatePaymentSchema.parse(req.body);

    const order = await prisma.order.findUnique({
      where: { id },
      include: { table: true },
    });

    if (!order) {
      return res.status(404).json({ success: false, message: 'Pesanan tidak ditemukan' });
    }

    const isLunas = paymentBillStatus === 'LUNAS';

    const updated = await prisma.$transaction(async (tx) => {
      const ord = await tx.order.update({
        where: { id },
        data: {
          paymentMethod,
          paymentBillStatus,
          status: isLunas ? 'SELESAI' : order.status,
          paidAt: isLunas ? new Date() : null,
        },
      });

      if (isLunas) {
        await tx.table.update({
          where: { id: order.tableId },
          data: {
            status: 'TERISI_LUNAS',
          },
        });
      }

      return ord;
    });

    emitToTable(order.table.number, 'payment_completed', {
      orderId: order.id,
      orderNumber: order.orderNumber,
      paymentMethod,
      paymentBillStatus,
      paidAt: updated.paidAt,
    });

    if (isLunas) {
      emitToAll('table_status_changed', {
        tableId: order.table.id,
        tableNumber: order.table.number,
        status: 'TERISI_LUNAS',
      });
    }

    return res.json({
      success: true,
      message: `Pembayaran ${order.orderNumber} berhasil diproses (${paymentMethod})`,
      data: updated,
    });
  } catch (error) {
    return next(error);
  }
};

export const getOrders = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { status, limit = 50 } = req.query;

    const where: any = {};
    if (status && typeof status === 'string') {
      where.status = status;
    }

    const orders = await prisma.order.findMany({
      where,
      take: Number(limit),
      orderBy: { createdAt: 'desc' },
      include: {
        table: true,
        items: true,
      },
    });

    return res.json({
      success: true,
      data: orders.map((o) => ({
        id: o.id,
        orderNumber: o.orderNumber,
        tableNumber: o.table.number,
        status: o.status,
        paymentBillStatus: o.paymentBillStatus,
        paymentMethod: o.paymentMethod,
        orderSource: o.orderSource,
        customerNotes: o.customerNotes,
        subtotal: Number(o.subtotal),
        itemSummary: o.items.map((i) => `${i.name} x${i.quantity}`).join(', '),
        createdAt: o.createdAt,
        paidAt: o.paidAt,
        items: o.items.map((i) => ({
          id: i.id,
          name: i.name,
          quantity: i.quantity,
          unitPrice: Number(i.unitPrice),
          subtotal: Number(i.subtotal),
          note: i.note,
        })),
      })),
    });
  } catch (error) {
    return next(error);
  }
};
