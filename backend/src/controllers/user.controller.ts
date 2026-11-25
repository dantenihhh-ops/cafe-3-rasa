import { Request, Response } from 'express';
import { z } from 'zod';
import bcrypt from 'bcryptjs';
import prisma from '../config/prisma.js';
import { AppError } from '../utils/http.js';

const publicUser = {
  id: true,
  username: true,
  fullName: true,
  role: true,
  isActive: true,
  createdAt: true,
} as const;

/** GET /api/users — daftar akun (admin) */
export const getUsers = async (_req: Request, res: Response) => {
  const users = await prisma.user.findMany({
    select: publicUser,
    orderBy: [{ role: 'desc' }, { createdAt: 'asc' }],
  });
  return res.json({ success: true, data: users });
};

const createSchema = z.object({
  username: z
    .string()
    .trim()
    .toLowerCase()
    .min(3, 'Username minimal 3 karakter')
    .max(30)
    .regex(/^[a-z0-9._-]+$/, 'Username hanya boleh huruf kecil, angka, titik, strip, garis bawah'),
  fullName: z.string().trim().min(2, 'Nama minimal 2 karakter').max(60),
  password: z.string().min(6, 'Password minimal 6 karakter').max(100),
  role: z.enum(['KASIR', 'ADMIN']).default('KASIR'),
});

/** POST /api/users — tambah akun kasir (admin) */
export const createUser = async (req: Request, res: Response) => {
  const data = createSchema.parse(req.body);
  const exists = await prisma.user.findUnique({ where: { username: data.username } });
  if (exists) throw new AppError(409, `Username "${data.username}" sudah digunakan`);

  const user = await prisma.user.create({
    data: {
      username: data.username,
      fullName: data.fullName,
      role: data.role,
      passwordHash: await bcrypt.hash(data.password, 10),
    },
    select: publicUser,
  });
  return res.status(201).json({ success: true, message: `Akun ${user.username} ditambahkan`, data: user });
};

const updateSchema = z
  .object({
    fullName: z.string().trim().min(2, 'Nama minimal 2 karakter').max(60).optional(),
    password: z.string().min(6, 'Password minimal 6 karakter').max(100).optional(),
    isActive: z.boolean().optional(),
  })
  .refine((d) => Object.keys(d).length > 0, { message: 'Tidak ada data yang diubah' });

/** PATCH /api/users/:id — ubah nama / reset password / nonaktifkan (admin) */
export const updateUser = async (req: Request, res: Response) => {
  const data = updateSchema.parse(req.body);
  const target = await prisma.user.findUnique({ where: { id: req.params.id } });
  if (!target) throw new AppError(404, 'Akun tidak ditemukan');
  if (data.isActive === false && target.id === req.user?.userId) {
    throw new AppError(409, 'Tidak bisa menonaktifkan akun yang sedang dipakai');
  }

  const user = await prisma.user.update({
    where: { id: target.id },
    data: {
      fullName: data.fullName,
      isActive: data.isActive,
      ...(data.password ? { passwordHash: await bcrypt.hash(data.password, 10) } : {}),
    },
    select: publicUser,
  });
  return res.json({ success: true, message: `Akun ${user.username} diperbarui`, data: user });
};

/** DELETE /api/users/:id — hapus akun kasir (akun admin dilindungi) */
export const deleteUser = async (req: Request, res: Response) => {
  const target = await prisma.user.findUnique({ where: { id: req.params.id } });
  if (!target) throw new AppError(404, 'Akun tidak ditemukan');
  if (target.role === 'ADMIN') throw new AppError(409, 'Akun Administrator tidak dapat dihapus');
  if (target.id === req.user?.userId) throw new AppError(409, 'Tidak bisa menghapus akun sendiri');

  await prisma.user.delete({ where: { id: target.id } });
  return res.json({ success: true, message: `Akun ${target.username} dihapus` });
};
