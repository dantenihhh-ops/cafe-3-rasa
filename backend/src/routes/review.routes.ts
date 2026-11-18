import { Router } from 'express';
import { createReview, getReviews } from '../controllers/review.controller.js';
import { adminOnly } from '../middlewares/auth.js';

const router = Router();

// Publik (web pelanggan)
router.post('/', createReview);

// Admin (laporan)
router.get('/', ...adminOnly, getReviews);

export default router;
