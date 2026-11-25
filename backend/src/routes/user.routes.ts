import { Router } from 'express';
import { getUsers, createUser, updateUser, deleteUser } from '../controllers/user.controller.js';
import { adminOnly } from '../middlewares/auth.js';
import { asyncHandler as h } from '../utils/http.js';

const router = Router();

router.get('/', ...adminOnly, h(getUsers));
router.post('/', ...adminOnly, h(createUser));
router.patch('/:id', ...adminOnly, h(updateUser));
router.delete('/:id', ...adminOnly, h(deleteUser));

export default router;
