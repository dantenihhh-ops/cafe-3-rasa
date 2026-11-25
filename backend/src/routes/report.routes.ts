import { Router } from 'express';
import { getSummary } from '../controllers/report.controller.js';
import { adminOnly } from '../middlewares/auth.js';
import { asyncHandler as h } from '../utils/http.js';

const router = Router();

router.get('/summary', ...adminOnly, h(getSummary));

export default router;
