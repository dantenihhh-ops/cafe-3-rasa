import { Router } from 'express';
import { createRequest, respondRequest, getActiveRequests } from '../controllers/request.controller.js';

const router = Router();

router.post('/', createRequest);
router.get('/', getActiveRequests);
router.patch('/:id/respond', respondRequest);

export default router;
