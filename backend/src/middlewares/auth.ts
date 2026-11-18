import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { env } from '../config/env.js';

export interface AuthUserPayload {
  userId: string;
  username: string;
  role: 'KASIR' | 'ADMIN';
  fullName: string;
}

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Express {
    interface Request {
      user?: AuthUserPayload;
    }
  }
}

/** Wajib login (Kasir atau Admin). Token dikirim di header: Authorization: Bearer <token> */
export const authenticate = (req: Request, res: Response, next: NextFunction) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ success: false, message: 'Akses ditolak: silakan login terlebih dahulu' });
  }

  try {
    const decoded = jwt.verify(authHeader.slice(7), env.jwtSecret) as AuthUserPayload;
    req.user = decoded;
    return next();
  } catch {
    return res.status(401).json({ success: false, message: 'Sesi login kadaluarsa atau tidak valid' });
  }
};

/** Batasi endpoint ke peran tertentu. Admin selalu boleh mengakses endpoint Kasir. */
export const requireRole = (role: 'ADMIN' | 'KASIR') => {
  return (req: Request, res: Response, next: NextFunction) => {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Tidak terautentikasi' });
    }
    if (role === 'ADMIN' && req.user.role !== 'ADMIN') {
      return res.status(403).json({ success: false, message: 'Akses terlarang: khusus Administrator' });
    }
    return next();
  };
};

/** Gabungan praktis */
export const kasirOnly = [authenticate, requireRole('KASIR')];
export const adminOnly = [authenticate, requireRole('ADMIN')];
