import { Router } from 'express';
import { getTables, updateTableStatus, clearTable } from '../controllers/table.controller.js';

const router = Router();

router.get('/', getTables);
router.patch('/:id/status', updateTableStatus);
router.post('/:id/clear', clearTable);

export default router;
