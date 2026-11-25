import { Request, Response } from 'express';
import QRCode from 'qrcode';
import prisma from '../config/prisma.js';
import { AppError, normalizeTableNumber } from '../utils/http.js';

/** Alamat publik web pelanggan, mis. https://kelompok7.tkj3a.my.id */
function publicBaseUrl(req: Request): string {
  const fromEnv = process.env.PUBLIC_URL?.trim().replace(/\/+$/, '');
  if (fromEnv) return fromEnv;
  return `${req.protocol}://${req.get('host')}`;
}

/** GET /api/qr — daftar meja + link QR (untuk halaman cetak qr.html) */
export const listQr = async (req: Request, res: Response) => {
  const base = publicBaseUrl(req);
  const tables = await prisma.table.findMany({ select: { number: true, area: true, capacity: true } });
  tables.sort((a, b) => Number(a.number) - Number(b.number));
  return res.json({
    success: true,
    data: tables.map((t) => ({
      ...t,
      orderUrl: `${base}/?meja=${t.number}`,
      qrImage: `${base}/api/qr/${t.number}.png`,
    })),
  });
};

/** GET /api/qr/:meja.png — gambar QR (PNG) yang membuka web pelanggan untuk meja tersebut */
export const tableQrPng = async (req: Request, res: Response) => {
  const number = normalizeTableNumber(req.params.number);
  const table = await prisma.table.findUnique({ where: { number }, select: { number: true } });
  if (!table) throw new AppError(404, `Meja ${number} tidak ditemukan`);

  const url = `${publicBaseUrl(req)}/?meja=${table.number}`;
  const png = await QRCode.toBuffer(url, { type: 'png', width: 600, margin: 2, errorCorrectionLevel: 'M' });

  res.setHeader('Content-Type', 'image/png');
  res.setHeader('Cache-Control', 'public, max-age=3600');
  if (req.query.download) res.setHeader('Content-Disposition', `attachment; filename="QR-Meja-${table.number}.png"`);
  return res.send(png);
};
