import { Router } from 'express';
import { getMenuItems, createMenuItem, toggleAvailability } from '../controllers/menu.controller.js';
import { authenticate, requireRole } from '../middlewares/auth.js';

const router = Router();

router.get('/', getMenuItems);
router.post('/', authenticate, requireRole('ADMIN'), createMenuItem);
router.patch('/:id/toggle', authenticate, toggleAvailability);

export default router;
