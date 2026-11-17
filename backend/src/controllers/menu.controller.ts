import { Request, Response, NextFunction } from 'express';
import { z } from 'zod';
import prisma from '../config/prisma.js';

export const getMenuItems = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { category, search, availableOnly } = req.query;

    const where: any = {};

    if (availableOnly === 'true') {
      where.isAvailable = true;
    }

    if (category && typeof category === 'string' && category.toUpperCase() !== 'SEMUA') {
      where.category = {
        name: { equals: category, mode: 'insensitive' },
      };
    }

    if (search && typeof search === 'string' && search.trim() !== '') {
      where.name = { contains: search.trim(), mode: 'insensitive' };
    }

    const items = await prisma.menuItem.findMany({
      where,
      include: {
        category: true,
      },
      orderBy: { createdAt: 'asc' },
    });

    return res.json({
      success: true,
      data: items.map((item) => ({
        id: item.id,
        name: item.name,
        categoryId: item.categoryId,
        categoryName: item.category.name,
        price: Number(item.price),
        image: item.image,
        description: item.description,
        isAvailable: item.isAvailable,
        filters: item.filters,
      })),
    });
  } catch (error) {
    return next(error);
  }
};

const createMenuItemSchema = z.object({
  id: z.string().min(1, 'ID menu wajib diisi (misal: m1, d1)'),
  name: z.string().min(2, 'Nama menu minimal 2 karakter'),
  categoryId: z.string().min(1, 'Kategori wajib dipilih'),
  price: z.number().positive('Harga harus lebih dari 0'),
  image: z.string().optional(),
  description: z.string().optional().default(''),
  filters: z.array(z.string()).optional().default([]),
});

export const createMenuItem = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const data = createMenuItemSchema.parse(req.body);

    const item = await prisma.menuItem.create({
      data: {
        id: data.id,
        name: data.name,
        categoryId: data.categoryId,
        price: BigInt(data.price),
        image: data.image,
        description: data.description,
        filters: data.filters,
      },
      include: { category: true },
    });

    return res.status(201).json({
      success: true,
      message: 'Menu berhasil ditambahkan',
      data: {
        ...item,
        price: Number(item.price),
      },
    });
  } catch (error) {
    return next(error);
  }
};

export const toggleAvailability = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { id } = req.params;

    const current = await prisma.menuItem.findUnique({ where: { id } });
    if (!current) {
      return res.status(404).json({ success: false, message: 'Menu tidak ditemukan' });
    }

    const updated = await prisma.menuItem.update({
      where: { id },
      data: { isAvailable: !current.isAvailable },
      include: { category: true },
    });

    return res.json({
      success: true,
      message: `Menu sekarang ${updated.isAvailable ? 'Tersedia' : 'Habis'}`,
      data: {
        ...updated,
        price: Number(updated.price),
      },
    });
  } catch (error) {
    return next(error);
  }
};
