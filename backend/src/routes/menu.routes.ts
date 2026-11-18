import { Router } from 'express';
import {
  getMenuItems,
  createMenuItem,
  updateMenuItem,
  deleteMenuItem,
  toggleAvailability,
} from '../controllers/menu.controller.js';
import { kasirOnly, adminOnly } from '../middlewares/auth.js';
import { asyncHandler as h } from '../utils/http.js';

const router = Router();

// Publik (web pelanggan)
router.get('/', h(getMenuItems));

// Kasir: tandai Tersedia / Habis
router.patch('/:id/toggle', ...kasirOnly, h(toggleAvailability));

// Admin: kelola produk
router.post('/', ...adminOnly, h(createMenuItem));
router.patch('/:id', ...adminOnly, h(updateMenuItem));
router.delete('/:id', ...adminOnly, h(deleteMenuItem));

export default router;
