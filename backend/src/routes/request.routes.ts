import { Router } from 'express';
import {
  createRequest,
  getActiveRequests,
  respondRequest,
  completeRequest,
} from '../controllers/request.controller.js';
import { kasirOnly } from '../middlewares/auth.js';
import { asyncHandler as h } from '../utils/http.js';

const router = Router();

// Publik (web pelanggan)
router.post('/', h(createRequest));

// Kasir
router.get('/', ...kasirOnly, h(getActiveRequests));
router.patch('/:id/respond', ...kasirOnly, h(respondRequest));
router.patch('/:id/done', ...kasirOnly, h(completeRequest));

export default router;
