import { Request, Response, NextFunction } from 'express';
import { z } from 'zod';
import prisma from '../config/prisma.js';
import { normalizeTableNumber } from '../utils/http.js';

/** Aspek yang bisa dipilih pelanggan di web (harus sama dengan tombol di bantuan.html) */
export const REVIEW_ASPECTS = ['Rasa Makanan', 'Kecepatan Pelayanan', 'Kemudahan E-Menu', 'Keramahan Staf'] as const;

/** Ulasan hanya bisa diubah dalam rentang waktu ini sejak dibuat */
const EDIT_WINDOW_MS = 6 * 60 * 60 * 1000;

const reviewSchema = z.object({
  tableNumber: z.coerce.string().min(1, 'Nomor meja wajib diisi'),
  rating: z.number().int().min(1).max(5),
  aspects: z
    .array(z.string())
    .max(10)
    .optional()
    .default([])
    // hanya terima aspek yang dikenal agar statistik tetap rapi
    .transform((list) => [...new Set(list.filter((a) => (REVIEW_ASPECTS as readonly string[]).includes(a)))]),
  comment: z.string().trim().max(500).optional(),
});

/** POST /api/reviews (publik) — kirim rating baru */
export const createReview = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const data = reviewSchema.parse(req.body);

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
        comment: data.comment || null,
      },
    });

    return res.status(201).json({
      success: true,
      message: 'Terima kasih atas ulasan dan rating Anda untuk Cafe 3 Rasa!',
      data: { id: review.id },
    });
  } catch (error) {
    return next(error);
  }
};

/**
 * PATCH /api/reviews/:id (publik) — ubah rating yang sudah dikirim.
 * Hanya bisa untuk meja yang sama dan maksimal 6 jam sejak dikirim,
 * sehingga "Ubah rating" tidak menambah ulasan baru (tidak dobel dihitung).
 */
export const updateReview = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const data = reviewSchema.parse(req.body);
    const existing = await prisma.review.findUnique({
      where: { id: String(req.params.id) },
      include: { table: true },
    });

    const sameTable = existing && existing.table.number === normalizeTableNumber(data.tableNumber);
    const fresh = existing && Date.now() - existing.createdAt.getTime() < EDIT_WINDOW_MS;
    if (!existing || !sameTable || !fresh) {
      // ulasan lama tidak ditemukan / kedaluwarsa → web akan membuat ulasan baru
      return res.status(404).json({ success: false, message: 'Ulasan tidak ditemukan atau sudah tidak bisa diubah' });
    }

    await prisma.review.update({
      where: { id: existing.id },
      data: { rating: data.rating, aspects: data.aspects, comment: data.comment || null },
    });

    return res.json({ success: true, message: 'Ulasan Anda berhasil diperbarui', data: { id: existing.id } });
  } catch (error) {
    return next(error);
  }
};

/** GET /api/reviews (admin) — ringkasan rating + daftar ulasan terbaru */
export const getReviews = async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const reviews = await prisma.review.findMany({
      orderBy: { createdAt: 'desc' },
      include: { table: { select: { number: true } } },
    });

    const total = reviews.length;
    const sum = reviews.reduce((acc, r) => acc + r.rating, 0);
    const avgRating = total ? Math.round((sum / total) * 10) / 10 : 0;

    // Sebaran bintang 5 → 1
    const distribution = [5, 4, 3, 2, 1].map((stars) => ({
      stars,
      count: reviews.filter((r) => r.rating === stars).length,
    }));

    // Statistik per aspek: berapa % pelanggan memilihnya + rata-rata bintang mereka
    const aspectsOf = (r: (typeof reviews)[number]) => (Array.isArray(r.aspects) ? (r.aspects as string[]) : []);
    const aspects = REVIEW_ASPECTS.map((name) => {
      const picked = reviews.filter((r) => aspectsOf(r).includes(name));
      const avg = picked.length ? picked.reduce((a, r) => a + r.rating, 0) / picked.length : 0;
      return {
        name,
        count: picked.length,
        percent: total ? Math.round((picked.length / total) * 100) : 0,
        avgRating: Math.round(avg * 10) / 10,
      };
    });

    return res.json({
      success: true,
      data: {
        summary: {
          totalReviews: total,
          avgRating,
          withComment: reviews.filter((r) => r.comment && r.comment.trim()).length,
        },
        distribution,
        aspects,
        // 50 ulasan terbaru untuk ditampilkan di admin
        reviews: reviews.slice(0, 50).map((r) => ({
          id: r.id,
          tableNumber: r.table.number,
          rating: r.rating,
          aspects: aspectsOf(r),
          comment: r.comment,
          createdAt: r.createdAt,
        })),
      },
    });
  } catch (error) {
    return next(error);
  }
};
