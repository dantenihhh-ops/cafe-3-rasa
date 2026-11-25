import { Router } from 'express';
import { listQr, tableQrPng } from '../controllers/qr.controller.js';
import { asyncHandler as h } from '../utils/http.js';

const router = Router();

// Publik: isi QR hanya link web pelanggan per meja
router.get('/', h(listQr));
router.get('/:number.png', h(tableQrPng));

export default router;
