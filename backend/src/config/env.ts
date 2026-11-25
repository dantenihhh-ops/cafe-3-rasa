import 'dotenv/config';

/**
 * Konfigurasi terpusat. Server menolak berjalan jika variabel penting belum diisi,
 * agar tidak diam-diam memakai secret bawaan yang bisa ditebak orang lain.
 */

function required(name: string): string {
  const value = process.env[name];
  if (!value || value.trim() === '') {
    console.error(`[Config] Variabel lingkungan "${name}" belum diisi di file backend/.env`);
    console.error('[Config] Salin backend/.env.example menjadi backend/.env lalu lengkapi isinya.');
    process.exit(1);
  }
  return value;
}

const taxRate = Number(process.env.TAX_RATE_PERCENT ?? 0);

export const env = {
  // Di hosting (Passenger/LiteSpeed) PORT bisa berupa path socket, bukan angka
  port: (() => {
    const raw = process.env.PORT || '5000';
    return /^\d+$/.test(raw) ? Number(raw) : raw;
  })() as number | string,
  nodeEnv: process.env.NODE_ENV || 'development',
  corsOrigin: process.env.CORS_ORIGIN || '*',
  jwtSecret: required('JWT_SECRET'),
  jwtExpiresIn: process.env.JWT_EXPIRES_IN || '7d',
  databaseUrl: required('DATABASE_URL'),
  taxRatePercent: Number.isFinite(taxRate) && taxRate >= 0 ? taxRate : 0,
};
