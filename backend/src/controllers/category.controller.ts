import { Request, Response } from 'express';
import { z } from 'zod';
import prisma from '../config/prisma.js';
import { emitToAll } from '../socket/index.js';
import { AppError } from '../utils/http.js';

const slugify = (s: string) =>
  s
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');

/** GET /api/categories (publik) */
export const getCategories = async (_req: Request, res: Response) => {
  const categories = await prisma.category.findMany({
    orderBy: [{ displayOrder: 'asc' }, { name: 'asc' }],
    include: { _count: { select: { menuItems: true } } },
  });
  return res.json({
    success: true,
    data: categories.map((c) => ({
      id: c.id,
      name: c.name,
      slug: c.slug,
      displayOrder: c.displayOrder,
      menuCount: c._count.menuItems,
    })),
  });
};

const categoryInputSchema = z.object({
  name: z.string().trim().min(2, 'Nama kategori minimal 2 karakter').max(40),
  displayOrder: z.number().int().min(0).optional(),
});

/** POST /api/categories (admin) */
export const createCategory = async (req: Request, res: Response) => {
  const data = categoryInputSchema.parse(req.body);
  const slug = slugify(data.name);
  if (!slug) throw new AppError(400, 'Nama kategori tidak valid');

  const exists = await prisma.category.findFirst({
    where: { OR: [{ slug }, { name: { equals: data.name } }] },
  });
  if (exists) throw new AppError(409, `Kategori "${data.name}" sudah ada`);

  const last = await prisma.category.aggregate({ _max: { displayOrder: true } });
  const created = await prisma.category.create({
    data: { name: data.name, slug, displayOrder: data.displayOrder ?? (last._max.displayOrder ?? 0) + 1 },
  });

  emitToAll('menu_changed', { action: 'category_created', id: created.id });
  return res.status(201).json({ success: true, message: `Kategori "${created.name}" ditambahkan`, data: created });
};

/** DELETE /api/categories/:id (admin) — ditolak jika kategori masih berisi produk */
export const deleteCategory = async (req: Request, res: Response) => {
  const category = await prisma.category.findUnique({
    where: { id: req.params.id },
    include: { _count: { select: { menuItems: true } } },
  });
  if (!category) throw new AppError(404, 'Kategori tidak ditemukan');
  if (category._count.menuItems > 0) {
    throw new AppError(
      409,
      `Kategori "${category.name}" masih berisi ${category._count.menuItems} produk. Pindahkan atau hapus produknya dulu.`
    );
  }

  await prisma.category.delete({ where: { id: category.id } });
  emitToAll('menu_changed', { action: 'category_deleted', id: category.id });
  return res.json({ success: true, message: `Kategori "${category.name}" dihapus` });
};
