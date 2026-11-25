import { Router } from 'express';
import { createReview, updateReview, getReviews } from '../controllers/review.controller.js';
import { adminOnly } from '../middlewares/auth.js';

const router = Router();

// Publik (web pelanggan)
router.post('/', createReview);
router.patch('/:id', updateReview);

// Admin (Pusat Informasi & Ulasan)
router.get('/', ...adminOnly, getReviews);

export default router;
