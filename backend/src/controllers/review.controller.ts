import { Request, Response, NextFunction } from 'express';
import { z } from 'zod';
import prisma from '../config/prisma.js';
import { normalizeTableNumber } from '../utils/http.js';

const createReviewSchema = z.object({
  tableNumber: z.coerce.string().min(1, 'Nomor meja wajib diisi'),
  rating: z.number().int().min(1).max(5),
  aspects: z.array(z.string().max(40)).max(10).optional().default([]),
  comment: z.string().max(500).optional(),
});

export const createReview = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const data = createReviewSchema.parse(req.body);

    const table = await prisma.table.findUnique({
      where: { number: normalizeTableNumber(data.tableNumber) },
    });

    if (!table) {
      return res.status(404).json({ success: false, message: 'Meja tidak ditemukan' });
    }

    const review = await prisma.review.create({
      data: {
        tableId: table.id,
        rating: data.rating,
        aspects: data.aspects,
        comment: data.comment,
      },
    });

    return res.status(201).json({
      success: true,
      message: 'Terima kasih atas ulasan dan rating Anda untuk Cafe 3 Rasa!',
      data: review,
    });
  } catch (error) {
    return next(error);
  }
};

export const getReviews = async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const reviews = await prisma.review.findMany({
      orderBy: { createdAt: 'desc' },
      include: { table: true },
    });

    const totalReviews = reviews.length;
    const avgRating =
      totalReviews > 0
        ? (reviews.reduce((acc, r) => acc + r.rating, 0) / totalReviews).toFixed(1)
        : '0.0';

    return res.json({
      success: true,
      summary: {
        totalReviews,
        avgRating: parseFloat(avgRating),
      },
      data: reviews.map((r) => ({
        id: r.id,
        tableNumber: r.table.number,
        rating: r.rating,
        aspects: Array.isArray(r.aspects) ? r.aspects : [],
        comment: r.comment,
        createdAt: r.createdAt,
      })),
    });
  } catch (error) {
    return next(error);
  }
};
