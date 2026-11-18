import { Request, Response } from 'express';
import { z } from 'zod';
import prisma from '../config/prisma.js';
import { emitToCashier, emitToTable } from '../socket/index.js';
import { AppError, normalizeTableNumber } from '../utils/http.js';

const REQUEST_TYPES = ['PANGGIL_PELAYAN', 'MINTA_BILL', 'MINTA_ALAT_MAKAN', 'LAINNYA'] as const;

/** Tebak jenis permintaan dari teks catatan web (chip "Minta Bill", "Alat Makan", dll.) */
function detectType(message?: string): (typeof REQUEST_TYPES)[number] {
  const m = (message || '').toLowerCase();
  if (m.includes('bill') || m.includes('tagihan') || m.includes('bayar')) return 'MINTA_BILL';
  if (m.includes('alat makan') || m.includes('sendok') || m.includes('garpu')) return 'MINTA_ALAT_MAKAN';
  if (m.includes('waiter') || m.includes('pelayan')) return 'PANGGIL_PELAYAN';
  return 'LAINNYA';
}

function serializeRequest(r: {
  id: string;
  table: { number: string };
  type: string;
  message: string | null;
  status: string;
  responseMessage: string | null;
  createdAt: Date;
  confirmedAt: Date | null;
}) {
  return {
    id: r.id,
    tableNumber: r.table.number,
    type: r.type,
    message: r.message,
    status: r.status,
    responseMessage: r.responseMessage,
    createdAt: r.createdAt,
    confirmedAt: r.confirmedAt,
  };
}

// ─── PUBLIK ──────────────────────────────────────────────────────────────

const createRequestSchema = z.object({
  tableNumber: z.coerce.string().min(1, 'Nomor meja wajib diisi'),
  type: z.enum(REQUEST_TYPES).optional(),
  message: z.string().max(300).optional(),
});

/** POST /api/requests — panggil waiter / minta bill / alat makan */
export const createRequest = async (req: Request, res: Response) => {
  const data = createRequestSchema.parse(req.body);
  const tableNumber = normalizeTableNumber(data.tableNumber);

  const table = await prisma.table.findUnique({ where: { number: tableNumber } });
  if (!table) throw new AppError(404, `Meja ${tableNumber} tidak ditemukan`);

  // Cegah spam: maksimal 1 permintaan menunggu per meja per 30 detik
  const recent = await prisma.serviceRequest.findFirst({
    where: { tableId: table.id, status: 'MENUNGGU', createdAt: { gte: new Date(Date.now() - 30_000) } },
  });
  if (recent) throw new AppError(429, 'Permintaan sebelumnya masih diproses, mohon tunggu sebentar');

  // Web lama selalu mengirim PANGGIL_PELAYAN, jadi jenis ditebak dari isi catatan
  const type = !data.type || data.type === 'PANGGIL_PELAYAN' ? detectType(data.message) : data.type;

  const request = await prisma.serviceRequest.create({
    data: { tableId: table.id, type, message: data.message?.trim() || null },
    include: { table: true },
  });

  const payload = serializeRequest(request);
  emitToCashier('call_waiter', payload);

  return res.status(201).json({
    success: true,
    message: 'Permintaan bantuan telah diteruskan ke kasir',
    data: payload,
  });
};

// ─── KASIR ───────────────────────────────────────────────────────────────

/** GET /api/requests — permintaan yang belum selesai */
export const getActiveRequests = async (_req: Request, res: Response) => {
  const requests = await prisma.serviceRequest.findMany({
    where: { status: { in: ['MENUNGGU', 'DIKONFIRMASI'] } },
    orderBy: { createdAt: 'desc' },
    include: { table: true },
  });
  return res.json({ success: true, data: requests.map(serializeRequest) });
};

const respondSchema = z.object({
  responseMessage: z.string().trim().min(1, 'Pesan balasan wajib diisi').max(300),
});

/** PATCH /api/requests/:id/respond — kasir mengonfirmasi & membalas */
export const respondRequest = async (req: Request, res: Response) => {
  const { responseMessage } = respondSchema.parse(req.body);

  const updated = await prisma.serviceRequest.update({
    where: { id: req.params.id },
    data: { status: 'DIKONFIRMASI', responseMessage, confirmedAt: new Date() },
    include: { table: true },
  });

  const payload = serializeRequest(updated);
  emitToTable(updated.table.number, 'service_confirmed', payload);
  emitToCashier('request_updated', payload);

  return res.json({ success: true, message: `Balasan dikirim ke Meja ${updated.table.number}`, data: payload });
};

/** PATCH /api/requests/:id/done — tandai selesai (hilang dari daftar kasir) */
export const completeRequest = async (req: Request, res: Response) => {
  const updated = await prisma.serviceRequest.update({
    where: { id: req.params.id },
    data: { status: 'SELESAI', confirmedAt: new Date() },
    include: { table: true },
  });

  const payload = serializeRequest(updated);
  emitToCashier('request_updated', payload);
  emitToTable(updated.table.number, 'service_completed', payload);

  return res.json({ success: true, message: 'Permintaan ditandai selesai', data: payload });
};
