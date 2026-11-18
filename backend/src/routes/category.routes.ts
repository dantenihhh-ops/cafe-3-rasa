import { Router } from 'express';
import { getCategories, createCategory, deleteCategory } from '../controllers/category.controller.js';
import { adminOnly } from '../middlewares/auth.js';
import { asyncHandler as h } from '../utils/http.js';

const router = Router();

router.get('/', h(getCategories));
router.post('/', ...adminOnly, h(createCategory));
router.delete('/:id', ...adminOnly, h(deleteCategory));

export default router;
