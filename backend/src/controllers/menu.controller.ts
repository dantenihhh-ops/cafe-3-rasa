import { Request, Response } from 'express';
import { z } from 'zod';
import { Prisma } from '@prisma/client';
import prisma from '../config/prisma.js';
import { emitToAll } from '../socket/index.js';
import { AppError } from '../utils/http.js';
import fs from 'fs/promises';
import path from 'path';
import { MENU_UPLOAD_DIR, WEB_DIR } from '../utils/paths.js';

type MenuWithCategory = Prisma.MenuItemGetPayload<{ include: { category: true } }>;

function serializeMenu(item: MenuWithCategory) {
  return {
    id: item.id,
    name: item.name,
    categoryId: item.categoryId,
    categoryName: item.category.name,
    price: Number(item.price),
    image: item.image,
    description: item.description,
    isAvailable: item.isAvailable,
    filters: Array.isArray(item.filters) ? (item.filters as string[]) : [],
  };
}

function broadcastMenuChanged(action: string, id: string) {
  // Web pelanggan & kasir cukup memuat ulang daftar menu saat event ini diterima
  emitToAll('menu_changed', { action, id });
}

// ─── PUBLIK ──────────────────────────────────────────────────────────────

const listQuerySchema = z.object({
  category: z.string().optional(),
  search: z.string().optional(),
  availableOnly: z.enum(['true', 'false']).optional(),
});

/** GET /api/menu?category=Makanan&search=kopi&availableOnly=true */
export const getMenuItems = async (req: Request, res: Response) => {
  const q = listQuerySchema.parse(req.query);

  const where: Prisma.MenuItemWhereInput = {};
  if (q.availableOnly === 'true') where.isAvailable = true;
  if (q.category && q.category.toUpperCase() !== 'SEMUA') {
    where.category = { name: { equals: q.category } };
  }
  if (q.search?.trim()) where.name = { contains: q.search.trim() };

  const items = await prisma.menuItem.findMany({
    where,
    include: { category: true },
    orderBy: [{ category: { displayOrder: 'asc' } }, { createdAt: 'asc' }],
  });

  return res.json({ success: true, data: items.map(serializeMenu) });
};

// ─── ADMIN ───────────────────────────────────────────────────────────────

const menuInputSchema = z.object({
  name: z.string().trim().min(2, 'Nama menu minimal 2 karakter').max(100),
  categoryId: z.string().min(1, 'Kategori wajib dipilih'),
  price: z.number().int('Harga harus bilangan bulat').positive('Harga harus lebih dari 0'),
  image: z.string().max(500).nullable().optional(),
  description: z.string().max(500).default(''),
  filters: z.array(z.string().max(30)).max(10).default([]),
  isAvailable: z.boolean().default(true),
});

async function assertCategoryExists(categoryId: string) {
  const cat = await prisma.category.findUnique({ where: { id: categoryId } });
  if (!cat) throw new AppError(400, 'Kategori tidak ditemukan');
}

/** POST /api/menu */
export const createMenuItem = async (req: Request, res: Response) => {
  const data = menuInputSchema.parse(req.body);
  await assertCategoryExists(data.categoryId);

  const item = await prisma.menuItem.create({
    data: { ...data, price: BigInt(data.price) },
    include: { category: true },
  });

  broadcastMenuChanged('created', item.id);
  return res.status(201).json({ success: true, message: 'Menu berhasil ditambahkan', data: serializeMenu(item) });
};

/** PATCH /api/menu/:id */
export const updateMenuItem = async (req: Request, res: Response) => {
  const data = menuInputSchema.partial().parse(req.body);
  if (data.categoryId) await assertCategoryExists(data.categoryId);

  const item = await prisma.menuItem.update({
    where: { id: req.params.id },
    data: { ...data, price: data.price !== undefined ? BigInt(data.price) : undefined },
    include: { category: true },
  });

  broadcastMenuChanged('updated', item.id);
  return res.json({ success: true, message: 'Menu diperbarui', data: serializeMenu(item) });
};

/** DELETE /api/menu/:id — riwayat pesanan tetap aman karena nama & harga sudah disalin */
export const deleteMenuItem = async (req: Request, res: Response) => {
  const item = await prisma.menuItem.delete({ where: { id: req.params.id } });
  broadcastMenuChanged('deleted', item.id);
  return res.json({ success: true, message: `Menu "${item.name}" dihapus` });
};

// ─── KASIR ───────────────────────────────────────────────────────────────

/** PATCH /api/menu/:id/toggle — Tersedia <-> Habis */
export const toggleAvailability = async (req: Request, res: Response) => {
  const current = await prisma.menuItem.findUnique({ where: { id: req.params.id } });
  if (!current) throw new AppError(404, 'Menu tidak ditemukan');

  const updated = await prisma.menuItem.update({
    where: { id: current.id },
    data: { isAvailable: !current.isAvailable },
    include: { category: true },
  });

  broadcastMenuChanged('updated', updated.id);
  return res.json({
    success: true,
    message: `"${updated.name}" sekarang ${updated.isAvailable ? 'Tersedia' : 'Habis'}`,
    data: serializeMenu(updated),
  });
};

// ─── ADMIN: foto menu ────────────────────────────────────────────────────

const MAX_IMAGE_BYTES = 2 * 1024 * 1024; // aturan hosting: maksimal 2 MB per file

const imageSchema = z.object({
  // isi file dalam base64 (boleh diawali "data:image/...;base64,")
  data: z.string().min(100, 'Data gambar kosong'),
});

/** Kenali jenis gambar dari isi file (bukan dari nama), hanya jpg/png/webp yang diterima */
function detectImageType(buf: Buffer): 'jpg' | 'png' | 'webp' | null {
  if (buf.length > 3 && buf[0] === 0xff && buf[1] === 0xd8 && buf[2] === 0xff) return 'jpg';
  if (buf.length > 8 && buf.subarray(0, 8).equals(Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]))) return 'png';
  if (buf.length > 12 && buf.toString('ascii', 0, 4) === 'RIFF' && buf.toString('ascii', 8, 12) === 'WEBP') return 'webp';
  return null;
}

/** POST /api/menu/:id/image — upload / ganti foto menu */
export const uploadMenuImage = async (req: Request, res: Response) => {
  const { data } = imageSchema.parse(req.body);
  const menu = await prisma.menuItem.findUnique({ where: { id: req.params.id } });
  if (!menu) throw new AppError(404, 'Menu tidak ditemukan');

  const base64 = data.includes(',') ? data.slice(data.indexOf(',') + 1) : data;
  const buf = Buffer.from(base64, 'base64');
  if (buf.length > MAX_IMAGE_BYTES) throw new AppError(413, 'Ukuran foto maksimal 2 MB');
  const ext = detectImageType(buf);
  if (!ext) throw new AppError(400, 'Format foto harus JPG, PNG, atau WEBP');

  await fs.mkdir(MENU_UPLOAD_DIR, { recursive: true });
  const safeId = menu.id.replace(/[^a-zA-Z0-9_-]/g, '');
  const fileName = `menu-${safeId}-${Date.now()}.${ext}`;
  await fs.writeFile(path.join(MENU_UPLOAD_DIR, fileName), buf);

  // Hapus foto upload lama (foto bawaan img/... tidak disentuh)
  if (menu.image?.startsWith('uploads/menu/')) {
    await fs.unlink(path.join(WEB_DIR, menu.image)).catch(() => undefined);
  }

  const item = await prisma.menuItem.update({
    where: { id: menu.id },
    data: { image: `uploads/menu/${fileName}` },
    include: { category: true },
  });

  broadcastMenuChanged('updated', item.id);
  return res.json({ success: true, message: `Foto "${item.name}" diperbarui`, data: serializeMenu(item) });
};
