import { Router } from 'express';
import {
  createOrder,
  getActiveOrderForTable,
  getOrders,
  getOrderById,
  createOrderByKasir,
  addOrderItems,
  updateOrderItem,
  confirmOrder,
  updatePayment,
  cancelOrder,
} from '../controllers/order.controller.js';
import { kasirOnly } from '../middlewares/auth.js';
import { asyncHandler as h } from '../utils/http.js';

const router = Router();

// Publik (web pelanggan dari QR meja)
router.post('/', h(createOrder));
router.get('/table/:tableNumber/active', h(getActiveOrderForTable));

// Kasir & Admin
router.get('/', ...kasirOnly, h(getOrders));
router.post('/kasir', ...kasirOnly, h(createOrderByKasir));
router.get('/:id', ...kasirOnly, h(getOrderById));
router.post('/:id/items', ...kasirOnly, h(addOrderItems));
router.patch('/:id/items/:itemId', ...kasirOnly, h(updateOrderItem));
router.patch('/:id/confirm', ...kasirOnly, h(confirmOrder));
router.patch('/:id/payment', ...kasirOnly, h(updatePayment));
router.patch('/:id/cancel', ...kasirOnly, h(cancelOrder));

export default router;
