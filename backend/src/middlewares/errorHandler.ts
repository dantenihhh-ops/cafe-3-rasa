import { Request, Response, NextFunction } from 'express';
import { ZodError } from 'zod';
import { Prisma } from '@prisma/client';
import { AppError } from '../utils/http.js';

export const notFoundHandler = (req: Request, res: Response) => {
  res.status(404).json({ success: false, message: `Endpoint ${req.method} ${req.originalUrl} tidak ditemukan` });
};

export const errorHandler = (
  err: unknown,
  _req: Request,
  res: Response,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  _next: NextFunction
) => {
  // Body JSON rusak
  if (err instanceof SyntaxError && 'body' in err) {
    return res.status(400).json({ success: false, message: 'Format JSON tidak valid' });
  }

  // Body melebihi batas ukuran (mis. foto > 2 MB)
  if (typeof err === 'object' && err !== null && (err as { type?: string }).type === 'entity.too.large') {
    return res.status(413).json({ success: false, message: 'Ukuran data terlalu besar (foto maksimal 2 MB)' });
  }

  if (err instanceof AppError) {
    return res.status(err.statusCode).json({ success: false, message: err.message });
  }

  if (err instanceof ZodError) {
    return res.status(400).json({
      success: false,
      message: 'Validasi data gagal',
      errors: err.errors.map((e) => ({ field: e.path.join('.'), message: e.message })),
    });
  }

  if (err instanceof Prisma.PrismaClientKnownRequestError) {
    if (err.code === 'P2025') {
      return res.status(404).json({ success: false, message: 'Data tidak ditemukan' });
    }
    if (err.code === 'P2002') {
      return res.status(409).json({ success: false, message: 'Data dengan nilai tersebut sudah ada' });
    }
    if (err.code === 'P2003') {
      return res.status(409).json({ success: false, message: 'Data masih dipakai oleh data lain' });
    }
  }

  if (err instanceof Prisma.PrismaClientInitializationError) {
    console.error('[Database] Gagal terhubung:', err.message);
    return res.status(503).json({ success: false, message: 'Database sedang tidak dapat dihubungi' });
  }

  console.error('[Error]', err);
  // Pesan internal tidak dikirim ke client agar detail server tidak bocor
  return res.status(500).json({ success: false, message: 'Terjadi kesalahan internal server' });
};
