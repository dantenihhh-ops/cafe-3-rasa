import 'dotenv/config';
import { PrismaClient } from '@prisma/client';
import { PrismaMariaDb } from '@prisma/adapter-mariadb';

// Polyfill agar BigInt bisa di-serialize ke JSON response tanpa error
(BigInt.prototype as unknown as { toJSON: () => number }).toJSON = function () {
  return Number(this);
};

/**
 * Ubah DATABASE_URL menjadi konfigurasi driver MariaDB.
 * Format yang didukung:
 *   mysql://USER:PASSWORD@localhost:3306/NAMA_DB
 *   mysql://USER:PASSWORD@localhost/NAMA_DB?socket=/var/lib/mysql/mysql.sock   (hosting cPanel)
 */
function adapterConfigFromUrl(raw: string | undefined) {
  if (!raw) throw new Error('DATABASE_URL belum diisi di backend/.env');
  const url = new URL(raw);
  const socketPath = url.searchParams.get('socket') || undefined;
  return {
    host: url.hostname || 'localhost',
    port: url.port ? Number(url.port) : 3306,
    user: decodeURIComponent(url.username),
    password: decodeURIComponent(url.password),
    database: decodeURIComponent(url.pathname.replace(/^\//, '')),
    ...(socketPath ? { socketPath } : {}),
    // Jumlah koneksi kecil: cukup untuk kafe & ramah untuk server bersama
    connectionLimit: Number(process.env.DB_CONNECTION_LIMIT || 5),
    connectTimeout: 10_000,
  };
}

const globalForPrisma = global as unknown as { prisma: PrismaClient };

export const prisma =
  globalForPrisma.prisma ||
  new PrismaClient({
    adapter: new PrismaMariaDb(adapterConfigFromUrl(process.env.DATABASE_URL)),
    // Set PRISMA_LOG_QUERIES=true di .env jika ingin melihat setiap query SQL di terminal
    log: process.env.PRISMA_LOG_QUERIES === 'true' ? ['query', 'error', 'warn'] : ['error', 'warn'],
    transactionOptions: {
      maxWait: 10_000,
      timeout: 20_000,
    },
  });

if (process.env.NODE_ENV !== 'production') globalForPrisma.prisma = prisma;

export default prisma;
