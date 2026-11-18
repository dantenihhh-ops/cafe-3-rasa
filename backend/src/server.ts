import { env } from './config/env.js'; // harus paling atas: memuat .env & validasi
import http from 'http';
import path from 'path';
import { fileURLToPath } from 'url';
import express, { Request, Response } from 'express';
import cors from 'cors';
import helmet from 'helmet';

import prisma from './config/prisma.js';
import { initSocket } from './socket/index.js';
import { errorHandler, notFoundHandler } from './middlewares/errorHandler.js';

import authRoutes from './routes/auth.routes.js';
import menuRoutes from './routes/menu.routes.js';
import categoryRoutes from './routes/category.routes.js';
import tableRoutes from './routes/table.routes.js';
import orderRoutes from './routes/order.routes.js';
import requestRoutes from './routes/request.routes.js';
import reviewRoutes from './routes/review.routes.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const server = http.createServer(app);

// Inisialisasi Socket.io
initSocket(server, env.corsOrigin);

// Security & Parsing Middlewares
app.use(helmet({ crossOriginResourcePolicy: false, contentSecurityPolicy: false }));
app.use(
  cors({
    origin: env.corsOrigin === '*' ? '*' : env.corsOrigin.split(',').map((s) => s.trim()),
  })
);
app.use(express.json({ limit: '100kb' }));
app.use(express.urlencoded({ extended: true }));

// Sajikan Website Pelanggan (tigarasa-web) langsung dari server backend
// (dari src/ saat dev maupun dist/ saat production, keduanya 2 tingkat di bawah root repo)
const webDistPath = path.resolve(__dirname, '../../tigarasa-web');
app.use(express.static(webDistPath));

// Health Check: benar-benar mengecek koneksi database
app.get('/api/health', async (_req: Request, res: Response) => {
  const startedAt = Date.now();
  try {
    await prisma.$queryRaw`SELECT 1`;
    res.json({
      status: 'ok',
      app: 'Cafe 3 Rasa Backend API & WebSocket Server',
      database: 'connected',
      dbLatencyMs: Date.now() - startedAt,
      taxRatePercent: env.taxRatePercent,
      timestamp: new Date().toISOString(),
    });
  } catch (err) {
    console.error('[Health] Database tidak terhubung:', err instanceof Error ? err.message : err);
    res.status(503).json({
      status: 'error',
      database: 'disconnected',
      message: 'Server berjalan, tetapi database tidak dapat dihubungi. Cek DATABASE_URL di .env',
      timestamp: new Date().toISOString(),
    });
  }
});

// Mount Routes
app.use('/api/auth', authRoutes);
app.use('/api/menu', menuRoutes);
app.use('/api/categories', categoryRoutes);
app.use('/api/tables', tableRoutes);
app.use('/api/orders', orderRoutes);
app.use('/api/requests', requestRoutes);
app.use('/api/reviews', reviewRoutes);

// Endpoint API yang tidak ada -> 404 JSON
app.use('/api', notFoundHandler);

// Global Error Handler
app.use(errorHandler);

server.listen(env.port, async () => {
  console.log(`[Cafe 3 Rasa] Server aktif di http://localhost:${env.port}`);
  console.log(`[Website E-Menu] Buka di browser: http://localhost:${env.port}/?meja=04`);
  console.log(`[Socket.io] WebSocket siap menerima koneksi real-time`);
  try {
    await prisma.$queryRaw`SELECT 1`;
    console.log('[Database] Terhubung ke PostgreSQL');
  } catch (err) {
    console.error('[Database] GAGAL terhubung. Cek DATABASE_URL & DIRECT_URL di backend/.env');
    console.error('          ', err instanceof Error ? err.message.split('\n').slice(-2).join(' ') : err);
  }
});

// Tutup koneksi database dengan rapi saat server dimatikan (Ctrl+C)
const shutdown = async () => {
  await prisma.$disconnect();
  server.close(() => process.exit(0));
};
process.on('SIGINT', shutdown);
process.on('SIGTERM', shutdown);
