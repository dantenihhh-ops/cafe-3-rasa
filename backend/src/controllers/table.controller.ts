import { Request, Response, NextFunction } from 'express';
import { z } from 'zod';
import prisma from '../config/prisma.js';
import { emitToAll, emitToCashier } from '../socket/index.js';

export const getTables = async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const tables = await prisma.table.findMany({
      orderBy: { number: 'asc' },
      include: {
        currentOrder: {
          include: {
            items: true,
          },
        },
      },
    });

    return res.json({
      success: true,
      data: tables.map((t) => ({
        id: t.id,
        number: t.number,
        capacity: t.capacity,
        status: t.status,
        activeSince: t.activeSince ? t.activeSince.toISOString() : null,
        currentOrder: t.currentOrder
          ? {
              id: t.currentOrder.id,
              orderNumber: t.currentOrder.orderNumber,
              status: t.currentOrder.status,
              paymentBillStatus: t.currentOrder.paymentBillStatus,
              paymentMethod: t.currentOrder.paymentMethod,
              orderSource: t.currentOrder.orderSource,
              subtotal: Number(t.currentOrder.subtotal),
              itemCount: t.currentOrder.items.reduce((sum, item) => sum + item.quantity, 0),
              items: t.currentOrder.items.map((i) => ({
                id: i.id,
                name: i.name,
                quantity: i.quantity,
                unitPrice: Number(i.unitPrice),
                subtotal: Number(i.subtotal),
                note: i.note,
              })),
            }
          : null,
      })),
    });
  } catch (error) {
    return next(error);
  }
};

const updateStatusSchema = z.object({
  status: z.enum(['KOSONG', 'BARU_PESAN', 'TERISI_BELUM_BAYAR', 'TERISI_LUNAS']),
});

export const updateTableStatus = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const tableId = parseInt(req.params.id, 10);
    const { status } = updateStatusSchema.parse(req.body);

    const updated = await prisma.table.update({
      where: { id: tableId },
      data: {
        status,
        activeSince: status === 'KOSONG' ? null : undefined,
      },
      include: { currentOrder: true },
    });

    emitToAll('table_status_changed', {
      tableId: updated.id,
      tableNumber: updated.number,
      status: updated.status,
    });

    return res.json({
      success: true,
      message: `Status Meja ${updated.number} berhasil diperbarui menjadi ${updated.status}`,
      data: updated,
    });
  } catch (error) {
    return next(error);
  }
};

export const clearTable = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const tableId = parseInt(req.params.id, 10);

    const updated = await prisma.table.update({
      where: { id: tableId },
      data: {
        status: 'KOSONG',
        activeSince: null,
        currentOrderId: null,
      },
    });

    emitToAll('table_status_changed', {
      tableId: updated.id,
      tableNumber: updated.number,
      status: 'KOSONG',
    });
    emitToCashier('table_cleared', { tableNumber: updated.number });

    return res.json({
      success: true,
      message: `Meja ${updated.number} telah dikosongkan`,
      data: updated,
    });
  } catch (error) {
    return next(error);
  }
};
