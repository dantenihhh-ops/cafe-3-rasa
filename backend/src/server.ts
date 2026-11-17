import http from 'http';
import path from 'path';
import { fileURLToPath } from 'url';
import express, { Request, Response } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import dotenv from 'dotenv';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

import { initSocket } from './socket/index.js';
import { errorHandler } from './middlewares/errorHandler.js';

import authRoutes from './routes/auth.routes.js';
import menuRoutes from './routes/menu.routes.js';
import tableRoutes from './routes/table.routes.js';
import orderRoutes from './routes/order.routes.js';
import requestRoutes from './routes/request.routes.js';
import reviewRoutes from './routes/review.routes.js';

const app = express();
const server = http.createServer(app);

const PORT = process.env.PORT || 5000;
const CORS_ORIGIN = process.env.CORS_ORIGIN || '*';

// Inisialisasi Socket.io
initSocket(server, CORS_ORIGIN);

// Security & Parsing Middlewares
app.use(
  helmet({
    crossOriginResourcePolicy: false,
    contentSecurityPolicy: false,
  })
);
app.use(
  cors({
    origin: CORS_ORIGIN === '*' ? '*' : CORS_ORIGIN.split(','),
    credentials: true,
  })
);
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Sajikan Website Pelanggan (tigarasa-web) langsung dari server backend
const webDistPath = path.resolve(__dirname, '../../tigarasa-web');
app.use(express.static(webDistPath));

// Health Check API
app.get('/api/health', (_req: Request, res: Response) => {
  res.json({
    status: 'ok',
    app: 'Cafe 3 Rasa Backend API & WebSocket Server',
    database: 'Supabase PostgreSQL Cloud (Connected)',
    author: 'Maharewa',
    timestamp: new Date().toISOString(),
  });
});

// Mount Routes
app.use('/api/auth', authRoutes);
app.use('/api/menu', menuRoutes);
app.use('/api/tables', tableRoutes);
app.use('/api/orders', orderRoutes);
app.use('/api/requests', requestRoutes);
app.use('/api/reviews', reviewRoutes);

// Fallback untuk root URL jika index.html di tigarasa-web dibuka
app.get('/', (_req: Request, res: Response) => {
  res.sendFile(path.join(webDistPath, 'index.html'));
});

// Global Error Handler
app.use(errorHandler);

server.listen(PORT, () => {
  console.log(`[Cafe 3 Rasa] Server aktif berjalan di http://localhost:${PORT}`);
  console.log(`[Website E-Menu] Buka di browser: http://localhost:${PORT}/?meja=04`);
  console.log(`[Socket.io] WebSocket siap menerima koneksi real-time`);
});
