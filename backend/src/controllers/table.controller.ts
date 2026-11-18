import { Request, Response } from 'express';
import { z } from 'zod';
import prisma from '../config/prisma.js';
import { emitToAll, emitToCashier } from '../socket/index.js';
import { AppError, normalizeTableNumber, parseIntParam } from '../utils/http.js';
import { orderInclude, serializeOrder, isOrderActive } from '../utils/orderHelpers.js';

const tableInclude = { currentOrder: { include: orderInclude } } as const;

type TableWithOrder = Awaited<ReturnType<typeof findTableOr404>>;

function serializeTable(t: TableWithOrder) {
  return {
    id: t.id,
    number: t.number,
    area: t.area,
    capacity: t.capacity,
    status: t.status,
    activeSince: t.activeSince,
    currentOrder: t.currentOrder ? serializeOrder(t.currentOrder) : null,
  };
}

async function findTableOr404(id: number) {
  const table = await prisma.table.findUnique({ where: { id }, include: tableInclude });
  if (!table) throw new AppError(404, 'Meja tidak ditemukan');
  return table;
}

function broadcastTableStatus(table: { id: number; number: string; status: string }) {
  emitToAll('table_status_changed', { tableId: table.id, tableNumber: table.number, status: table.status });
}

/** Urutkan "01","02",..,"10" secara numerik */
const byNumber = (a: { number: string }, b: { number: string }) =>
  Number(a.number) - Number(b.number) || a.number.localeCompare(b.number);

// ─── KASIR ───────────────────────────────────────────────────────────────

/** GET /api/tables — peta meja beserta pesanan aktifnya */
export const getTables = async (_req: Request, res: Response) => {
  const tables = await prisma.table.findMany({ include: tableInclude });
  return res.json({ success: true, data: tables.sort(byNumber).map(serializeTable) });
};

const updateStatusSchema = z.object({
  status: z.enum(['KOSONG', 'TERISI_BELUM_BAYAR']),
});

/**
 * PATCH /api/tables/:id/status — switch okupansi manual oleh kasir
 * (mis. tamu duduk tapi belum memesan). Untuk mengosongkan meja yang punya pesanan pakai /clear.
 */
export const updateTableStatus = async (req: Request, res: Response) => {
  const id = parseIntParam(req.params.id, 'ID meja');
  const { status } = updateStatusSchema.parse(req.body);
  const table = await findTableOr404(id);

  if (status === 'KOSONG' && table.currentOrder && isOrderActive(table.currentOrder)) {
    throw new AppError(409, `Meja ${table.number} masih punya pesanan yang belum dibayar`);
  }

  const updated = await prisma.table.update({
    where: { id },
    data:
      status === 'KOSONG'
        ? { status, activeSince: null, currentOrderId: null }
        : { status, activeSince: table.activeSince ?? new Date() },
    include: tableInclude,
  });

  broadcastTableStatus(updated);
  return res.json({ success: true, message: `Meja ${updated.number} diperbarui`, data: serializeTable(updated) });
};

/** POST /api/tables/:id/clear — kosongkan meja (hanya jika pesanan sudah lunas / tidak ada) */
export const clearTable = async (req: Request, res: Response) => {
  const id = parseIntParam(req.params.id, 'ID meja');
  const table = await findTableOr404(id);

  if (table.currentOrder && isOrderActive(table.currentOrder)) {
    throw new AppError(
      409,
      `Meja ${table.number} belum lunas. Tandai pembayaran Lunas atau batalkan pesanan terlebih dahulu.`
    );
  }

  const updated = await prisma.table.update({
    where: { id },
    data: { status: 'KOSONG', activeSince: null, currentOrderId: null },
    include: tableInclude,
  });

  broadcastTableStatus(updated);
  emitToCashier('table_cleared', { tableId: updated.id, tableNumber: updated.number });
  return res.json({ success: true, message: `Meja ${updated.number} telah dikosongkan`, data: serializeTable(updated) });
};

const moveSchema = z.object({
  targetTableId: z.number().int().positive(),
});

/** POST /api/tables/:id/move — Ganti Meja: pindahkan pesanan aktif ke meja kosong */
export const moveTable = async (req: Request, res: Response) => {
  const fromId = parseIntParam(req.params.id, 'ID meja');
  const { targetTableId } = moveSchema.parse(req.body);
  if (fromId === targetTableId) throw new AppError(400, 'Meja tujuan sama dengan meja asal');

  const [from, to] = await Promise.all([findTableOr404(fromId), findTableOr404(targetTableId)]);
  if (!from.currentOrder) throw new AppError(409, `Meja ${from.number} tidak punya pesanan untuk dipindahkan`);
  if (to.status !== 'KOSONG' || to.currentOrderId) throw new AppError(409, `Meja ${to.number} tidak kosong`);

  const orderId = from.currentOrder.id;

  const [updatedFrom, updatedTo] = await prisma.$transaction(async (tx) => {
    // Lepas dulu dari meja asal (currentOrderId bersifat unik)
    const f = await tx.table.update({
      where: { id: from.id },
      data: { status: 'KOSONG', activeSince: null, currentOrderId: null },
      include: tableInclude,
    });
    await tx.order.update({ where: { id: orderId }, data: { tableId: to.id } });
    const t = await tx.table.update({
      where: { id: to.id },
      data: { status: from.status, activeSince: from.activeSince ?? new Date(), currentOrderId: orderId },
      include: tableInclude,
    });
    return [f, t];
  });

  broadcastTableStatus(updatedFrom);
  broadcastTableStatus(updatedTo);
  emitToCashier('table_moved', { fromTableNumber: from.number, toTableNumber: to.number, orderId });
  emitToAll('table_moved', { fromTableNumber: from.number, toTableNumber: to.number });

  return res.json({
    success: true,
    message: `Pesanan dipindahkan dari Meja ${from.number} ke Meja ${to.number}`,
    data: { from: serializeTable(updatedFrom), to: serializeTable(updatedTo) },
  });
};

// ─── ADMIN ───────────────────────────────────────────────────────────────

const tableInputSchema = z.object({
  number: z.coerce.string().min(1, 'Nomor meja wajib diisi'),
  area: z.string().min(1).max(50).default('Indoor'),
  capacity: z.number().int().min(1).max(50).default(4),
});

/** POST /api/tables — tambah meja baru */
export const createTable = async (req: Request, res: Response) => {
  const data = tableInputSchema.parse(req.body);
  const number = normalizeTableNumber(data.number);

  const exists = await prisma.table.findUnique({ where: { number } });
  if (exists) throw new AppError(409, `Meja ${number} sudah ada`);

  const created = await prisma.table.create({
    data: { number, area: data.area, capacity: data.capacity },
    include: tableInclude,
  });

  emitToAll('tables_changed', { action: 'created', tableNumber: created.number });
  return res.status(201).json({ success: true, message: `Meja ${number} ditambahkan`, data: serializeTable(created) });
};

/** PATCH /api/tables/:id — ubah area / kapasitas */
export const updateTable = async (req: Request, res: Response) => {
  const id = parseIntParam(req.params.id, 'ID meja');
  const data = tableInputSchema.omit({ number: true }).partial().parse(req.body);
  await findTableOr404(id);

  const updated = await prisma.table.update({ where: { id }, data, include: tableInclude });
  emitToAll('tables_changed', { action: 'updated', tableNumber: updated.number });
  return res.json({ success: true, message: `Meja ${updated.number} diperbarui`, data: serializeTable(updated) });
};

/** DELETE /api/tables/:id — hapus meja yang belum pernah punya pesanan */
export const deleteTable = async (req: Request, res: Response) => {
  const id = parseIntParam(req.params.id, 'ID meja');
  const table = await findTableOr404(id);

  const orderCount = await prisma.order.count({ where: { tableId: id } });
  if (orderCount > 0) {
    throw new AppError(409, `Meja ${table.number} sudah punya riwayat pesanan sehingga tidak bisa dihapus`);
  }

  await prisma.table.delete({ where: { id } });
  emitToAll('tables_changed', { action: 'deleted', tableNumber: table.number });
  return res.json({ success: true, message: `Meja ${table.number} dihapus` });
};
