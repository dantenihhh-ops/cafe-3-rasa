import { Request, Response } from 'express';
import { z } from 'zod';
import { Prisma } from '@prisma/client';
import prisma from '../config/prisma.js';
import { emitToCashier, emitToTable, emitToAll } from '../socket/index.js';
import { AppError, normalizeTableNumber } from '../utils/http.js';
import {
  orderInclude,
  serializeOrder,
  recalcOrderTotals,
  buildOrderItems,
  generateOrderNumber,
  isOrderActive,
  assertEditable,
  FullOrder,
} from '../utils/orderHelpers.js';

// ─── Skema validasi ──────────────────────────────────────────────────────

const itemInputSchema = z.object({
  menuItemId: z.coerce.string().min(1, 'ID menu wajib diisi'),
  quantity: z.number().int().min(1, 'Jumlah minimal 1').max(99, 'Jumlah maksimal 99'),
  note: z.string().max(200, 'Catatan maksimal 200 karakter').optional(),
});

const createOrderSchema = z.object({
  tableNumber: z.coerce.string().min(1, 'Nomor meja wajib diisi'),
  customerNotes: z.string().max(300).optional(),
  // Harga/nama dari client diabaikan; server mengambil dari database
  items: z.array(itemInputSchema).min(1, 'Pesanan minimal berisi 1 item').max(50),
});

const addItemsSchema = z.object({
  items: z.array(itemInputSchema).min(1).max(50),
});

const updateItemSchema = z
  .object({
    quantity: z.number().int().min(0).max(99).optional(), // 0 = hapus item
    note: z.string().max(200).nullable().optional(),
  })
  .refine((d) => d.quantity !== undefined || d.note !== undefined, {
    message: 'Isi quantity atau note',
  });

const updatePaymentSchema = z.object({
  paymentMethod: z.enum(['CASH', 'TRANSFER']),
  paymentBillStatus: z.enum(['BELUM_BAYAR', 'LUNAS']).default('LUNAS'),
});

const listQuerySchema = z.object({
  status: z.enum(['MENUNGGU_KONFIRMASI', 'DIPROSES', 'SELESAI', 'DIBATALKAN']).optional(),
  payment: z.enum(['BELUM_BAYAR', 'LUNAS']).optional(),
  search: z.string().optional(),
  from: z.coerce.date().optional(),
  to: z.coerce.date().optional(),
  limit: z.coerce.number().int().min(1).max(500).default(100),
});

// ─── Helper broadcast ────────────────────────────────────────────────────

function broadcastOrderUpdated(order: FullOrder) {
  const payload = serializeOrder(order);
  emitToCashier('order_updated', payload);
  emitToTable(order.table.number, 'order_updated', payload);
}

function broadcastTableStatus(table: { id: number; number: string }, status: string) {
  emitToAll('table_status_changed', { tableId: table.id, tableNumber: table.number, status });
}

async function findOrderOr404(id: string) {
  const order = await prisma.order.findUnique({ where: { id }, include: orderInclude });
  if (!order) throw new AppError(404, 'Pesanan tidak ditemukan');
  return order;
}

/** Jalankan ulang transaksi jika nomor pesanan bentrok (2 pesanan masuk bersamaan) */
async function withOrderNumberRetry<T>(fn: () => Promise<T>, attempts = 3): Promise<T> {
  for (let i = 1; ; i++) {
    try {
      return await fn();
    } catch (err) {
      const isDuplicate = err instanceof Prisma.PrismaClientKnownRequestError && err.code === 'P2002';
      if (!isDuplicate || i >= attempts) throw err;
    }
  }
}

// ─── PUBLIK: pelanggan dari web meja ─────────────────────────────────────

/**
 * POST /api/orders
 * Pelanggan mengirim pesanan. Jika meja masih punya pesanan aktif (belum bayar),
 * item baru ditambahkan ke pesanan itu (add-on) sehingga satu meja = satu tagihan.
 */
export const createOrder = async (req: Request, res: Response) => {
  const data = createOrderSchema.parse(req.body);
  const tableNumber = normalizeTableNumber(data.tableNumber);

  const table = await prisma.table.findUnique({ where: { number: tableNumber }, include: { currentOrder: true } });
  if (!table) throw new AppError(404, `Meja ${tableNumber} tidak ditemukan`);

  const isAddOn = !!table.currentOrder && isOrderActive(table.currentOrder);

  const order = await withOrderNumberRetry(() =>
    prisma.$transaction(async (tx) => {
      const items = await buildOrderItems(tx, data.items);
      const now = new Date();

      let orderId: string;
      if (isAddOn) {
        orderId = table.currentOrder!.id;
        await tx.orderItem.createMany({ data: items.map((i) => ({ ...i, orderId })) });
        await tx.order.update({
          where: { id: orderId },
          // Ada tambahan pesanan -> kasir perlu konfirmasi lagi
          data: {
            status: 'MENUNGGU_KONFIRMASI',
            ...(data.customerNotes ? { customerNotes: data.customerNotes } : {}),
          },
        });
      } else {
        const created = await tx.order.create({
          data: {
            orderNumber: await generateOrderNumber(tx),
            tableId: table.id,
            orderSource: 'EMENU',
            customerNotes: data.customerNotes,
            items: { create: items },
          },
        });
        orderId = created.id;
      }

      await tx.table.update({
        where: { id: table.id },
        data: {
          status: 'BARU_PESAN',
          activeSince: isAddOn ? undefined : now,
          currentOrderId: orderId,
        },
      });

      return recalcOrderTotals(tx, orderId);
    })
  );

  const payload = serializeOrder(order);
  emitToCashier('new_order', { ...payload, isAddOn });
  broadcastTableStatus(table, 'BARU_PESAN');

  return res.status(201).json({
    success: true,
    message: isAddOn ? 'Tambahan pesanan berhasil dikirim ke kasir' : 'Pesanan berhasil dikirim ke kasir',
    data: payload,
  });
};

/**
 * GET /api/orders/table/:tableNumber/active
 * Web pelanggan melihat pesanan aktif mejanya sendiri (halaman Status).
 */
export const getActiveOrderForTable = async (req: Request, res: Response) => {
  const tableNumber = normalizeTableNumber(req.params.tableNumber);
  const table = await prisma.table.findUnique({
    where: { number: tableNumber },
    include: { currentOrder: { include: orderInclude } },
  });
  if (!table) throw new AppError(404, `Meja ${tableNumber} tidak ditemukan`);

  const order = table.currentOrder;
  return res.json({
    success: true,
    data: order && order.status !== 'DIBATALKAN' ? serializeOrder(order) : null,
  });
};

// ─── KASIR ───────────────────────────────────────────────────────────────

/** GET /api/orders — log pesanan dengan filter */
export const getOrders = async (req: Request, res: Response) => {
  const q = listQuerySchema.parse(req.query);

  const where: Prisma.OrderWhereInput = {};
  if (q.status) where.status = q.status;
  if (q.payment) where.paymentBillStatus = q.payment;
  if (q.from || q.to) where.createdAt = { gte: q.from, lte: q.to };
  if (q.search?.trim()) {
    const s = q.search.trim();
    where.OR = [
      { orderNumber: { contains: s, mode: 'insensitive' } },
      { table: { number: { contains: s } } },
      { items: { some: { name: { contains: s, mode: 'insensitive' } } } },
    ];
  }

  const orders = await prisma.order.findMany({
    where,
    take: q.limit,
    orderBy: { createdAt: 'desc' },
    include: orderInclude,
  });

  return res.json({ success: true, data: orders.map(serializeOrder) });
};

/** GET /api/orders/:id */
export const getOrderById = async (req: Request, res: Response) => {
  const order = await findOrderOr404(req.params.id);
  return res.json({ success: true, data: serializeOrder(order) });
};

/** POST /api/orders/kasir — kasir membuat / menambah pesanan manual untuk meja */
export const createOrderByKasir = async (req: Request, res: Response) => {
  const data = createOrderSchema.parse(req.body);
  const tableNumber = normalizeTableNumber(data.tableNumber);

  const table = await prisma.table.findUnique({ where: { number: tableNumber }, include: { currentOrder: true } });
  if (!table) throw new AppError(404, `Meja ${tableNumber} tidak ditemukan`);

  const isAddOn = !!table.currentOrder && isOrderActive(table.currentOrder);

  const order = await withOrderNumberRetry(() =>
    prisma.$transaction(async (tx) => {
      const items = await buildOrderItems(tx, data.items);
      let orderId: string;

      if (isAddOn) {
        orderId = table.currentOrder!.id;
        await tx.orderItem.createMany({ data: items.map((i) => ({ ...i, orderId })) });
      } else {
        const created = await tx.order.create({
          data: {
            orderNumber: await generateOrderNumber(tx),
            tableId: table.id,
            orderSource: 'KASIR',
            customerNotes: data.customerNotes,
            // Input oleh kasir dianggap langsung terkonfirmasi
            status: 'DIPROSES',
            confirmedAt: new Date(),
            items: { create: items },
          },
        });
        orderId = created.id;
        await tx.table.update({
          where: { id: table.id },
          data: { status: 'TERISI_BELUM_BAYAR', activeSince: new Date(), currentOrderId: orderId },
        });
      }

      return recalcOrderTotals(tx, orderId);
    })
  );

  broadcastOrderUpdated(order);
  if (!isAddOn) broadcastTableStatus(table, 'TERISI_BELUM_BAYAR');

  return res.status(201).json({ success: true, message: 'Pesanan kasir tersimpan', data: serializeOrder(order) });
};

/** POST /api/orders/:id/items — kasir menambah item ke pesanan */
export const addOrderItems = async (req: Request, res: Response) => {
  const { items } = addItemsSchema.parse(req.body);
  const existing = await findOrderOr404(req.params.id);
  assertEditable(existing);

  const order = await prisma.$transaction(async (tx) => {
    const built = await buildOrderItems(tx, items);
    await tx.orderItem.createMany({ data: built.map((i) => ({ ...i, orderId: existing.id })) });
    return recalcOrderTotals(tx, existing.id);
  });

  broadcastOrderUpdated(order);
  return res.status(201).json({ success: true, message: 'Item ditambahkan', data: serializeOrder(order) });
};

/** PATCH /api/orders/:id/items/:itemId — ubah porsi / catatan dapur (quantity 0 = hapus item) */
export const updateOrderItem = async (req: Request, res: Response) => {
  const data = updateItemSchema.parse(req.body);
  const existing = await findOrderOr404(req.params.id);
  assertEditable(existing);

  const item = existing.items.find((i) => i.id === req.params.itemId);
  if (!item) throw new AppError(404, 'Item tidak ditemukan pada pesanan ini');

  if (data.quantity === 0 && existing.items.length === 1) {
    throw new AppError(409, 'Item terakhir tidak bisa dihapus. Batalkan pesanan jika ingin mengosongkan.');
  }

  const order = await prisma.$transaction(async (tx) => {
    if (data.quantity === 0) {
      await tx.orderItem.delete({ where: { id: item.id } });
    } else {
      const quantity = data.quantity ?? item.quantity;
      await tx.orderItem.update({
        where: { id: item.id },
        data: {
          quantity,
          subtotal: item.unitPrice * BigInt(quantity),
          ...(data.note !== undefined ? { note: data.note?.trim() || null } : {}),
        },
      });
    }
    return recalcOrderTotals(tx, existing.id);
  });

  broadcastOrderUpdated(order);
  return res.json({ success: true, message: 'Pesanan diperbarui', data: serializeOrder(order) });
};

/** PATCH /api/orders/:id/confirm — kasir konfirmasi & kirim ke dapur */
export const confirmOrder = async (req: Request, res: Response) => {
  const existing = await findOrderOr404(req.params.id);
  assertEditable(existing);

  const order = await prisma.$transaction(async (tx) => {
    const updated = await tx.order.update({
      where: { id: existing.id },
      data: { status: 'DIPROSES', confirmedAt: new Date() },
      include: orderInclude,
    });
    if (existing.table.currentOrderId === existing.id) {
      await tx.table.update({ where: { id: existing.tableId }, data: { status: 'TERISI_BELUM_BAYAR' } });
    }
    return updated;
  });

  emitToTable(order.table.number, 'order_confirmed', {
    orderId: order.id,
    orderNumber: order.orderNumber,
    tableNumber: order.table.number,
    status: order.status,
    confirmedAt: order.confirmedAt,
  });
  broadcastOrderUpdated(order);
  broadcastTableStatus(order.table, 'TERISI_BELUM_BAYAR');

  return res.json({
    success: true,
    message: `Pesanan ${order.orderNumber} dikonfirmasi dan dikirim ke dapur`,
    data: serializeOrder(order),
  });
};

/** PATCH /api/orders/:id/payment — tandai Lunas / Belum Bayar (Cash / Transfer) */
export const updatePayment = async (req: Request, res: Response) => {
  const { paymentMethod, paymentBillStatus } = updatePaymentSchema.parse(req.body);
  const existing = await findOrderOr404(req.params.id);

  if (existing.status === 'DIBATALKAN') throw new AppError(409, 'Pesanan yang dibatalkan tidak bisa dibayar');
  if (existing.status === 'MENUNGGU_KONFIRMASI' && paymentBillStatus === 'LUNAS') {
    throw new AppError(409, 'Konfirmasi pesanan terlebih dahulu sebelum menandai lunas');
  }

  const isLunas = paymentBillStatus === 'LUNAS';
  const isCurrent = existing.table.currentOrderId === existing.id;

  const order = await prisma.$transaction(async (tx) => {
    const updated = await tx.order.update({
      where: { id: existing.id },
      data: {
        paymentMethod,
        paymentBillStatus,
        status: isLunas ? 'SELESAI' : 'DIPROSES',
        paidAt: isLunas ? new Date() : null,
      },
      include: orderInclude,
    });
    if (isCurrent) {
      await tx.table.update({
        where: { id: existing.tableId },
        data: { status: isLunas ? 'TERISI_LUNAS' : 'TERISI_BELUM_BAYAR' },
      });
    }
    return updated;
  });

  emitToTable(order.table.number, 'payment_completed', {
    orderId: order.id,
    orderNumber: order.orderNumber,
    paymentMethod,
    paymentBillStatus,
    total: Number(order.total),
    paidAt: order.paidAt,
  });
  broadcastOrderUpdated(order);
  if (isCurrent) broadcastTableStatus(order.table, isLunas ? 'TERISI_LUNAS' : 'TERISI_BELUM_BAYAR');

  return res.json({
    success: true,
    message: `Pembayaran ${order.orderNumber}: ${paymentBillStatus === 'LUNAS' ? 'Lunas' : 'Belum Bayar'} (${paymentMethod})`,
    data: serializeOrder(order),
  });
};

/** PATCH /api/orders/:id/cancel — batalkan pesanan yang belum dibayar */
export const cancelOrder = async (req: Request, res: Response) => {
  const existing = await findOrderOr404(req.params.id);
  assertEditable(existing);

  const isCurrent = existing.table.currentOrderId === existing.id;

  const order = await prisma.$transaction(async (tx) => {
    const updated = await tx.order.update({
      where: { id: existing.id },
      data: { status: 'DIBATALKAN' },
      include: orderInclude,
    });
    if (isCurrent) {
      await tx.table.update({
        where: { id: existing.tableId },
        data: { status: 'KOSONG', activeSince: null, currentOrderId: null },
      });
    }
    return updated;
  });

  broadcastOrderUpdated(order);
  if (isCurrent) broadcastTableStatus(order.table, 'KOSONG');

  return res.json({ success: true, message: `Pesanan ${order.orderNumber} dibatalkan`, data: serializeOrder(order) });
};
