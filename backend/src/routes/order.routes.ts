import { Router } from 'express';
import { createOrder, confirmOrder, updatePayment, getOrders } from '../controllers/order.controller.js';

const router = Router();

router.post('/', createOrder);
router.get('/', getOrders);
router.patch('/:id/confirm', confirmOrder);
router.patch('/:id/payment', updatePayment);

export default router;
