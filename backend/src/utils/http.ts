import { Request, Response, NextFunction, RequestHandler } from 'express';

/** Error dengan kode status HTTP, dilempar dari controller lalu ditangani errorHandler. */
export class AppError extends Error {
  constructor(public statusCode: number, message: string) {
    super(message);
    this.name = 'AppError';
  }
}

/** Membungkus handler async agar error otomatis diteruskan ke errorHandler (tanpa try/catch berulang). */
export const asyncHandler =
  (fn: (req: Request, res: Response, next: NextFunction) => Promise<unknown>): RequestHandler =>
  (req, res, next) => {
    fn(req, res, next).catch(next);
  };

/** Parse parameter ID numerik (mis. ID meja). */
export function parseIntParam(value: string, label = 'ID'): number {
  const n = Number.parseInt(value, 10);
  if (!Number.isInteger(n) || n <= 0) throw new AppError(400, `${label} tidak valid`);
  return n;
}

/** Samakan format nomor meja: "4", "04", "meja-4" -> "04" */
export function normalizeTableNumber(value: string): string {
  const digits = String(value).replace(/\D/g, '');
  if (!digits) throw new AppError(400, 'Nomor meja tidak valid');
  return digits.padStart(2, '0');
}
