import { Router } from 'express';
import {
  getTables,
  updateTableStatus,
  clearTable,
  moveTable,
  createTable,
  updateTable,
  deleteTable,
} from '../controllers/table.controller.js';
import { kasirOnly, adminOnly } from '../middlewares/auth.js';
import { asyncHandler as h } from '../utils/http.js';

const router = Router();

// Kasir
router.get('/', ...kasirOnly, h(getTables));
router.patch('/:id/status', ...kasirOnly, h(updateTableStatus));
router.post('/:id/clear', ...kasirOnly, h(clearTable));
router.post('/:id/move', ...kasirOnly, h(moveTable));

// Admin
router.post('/', ...adminOnly, h(createTable));
router.patch('/:id', ...adminOnly, h(updateTable));
router.delete('/:id', ...adminOnly, h(deleteTable));

export default router;
