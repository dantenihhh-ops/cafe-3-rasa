import { Request, Response, NextFunction } from 'express';
import { ZodError } from 'zod';

export const errorHandler = (
  err: unknown,
  _req: Request,
  res: Response,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  _next: NextFunction
) => {
  console.error('[Error Handler]:', err);

  // Error validasi Zod
  if (err instanceof ZodError) {
    return res.status(400).json({
      success: false,
      message: 'Validasi data gagal',
      errors: err.errors.map((e) => ({
        field: e.path.join('.'),
        message: e.message,
      })),
    });
  }

  // Error umum
  const message = err instanceof Error ? err.message : 'Terjadi kesalahan internal server';
  return res.status(500).json({
    success: false,
    message,
  });
};
