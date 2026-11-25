/**
 * Titik masuk untuk cPanel "Setup Node.js App" (Application startup file: app.cjs).
 * Hosting memuat file ini dengan require(), sedangkan backend memakai ES Module,
 * sehingga server dijalankan lewat import() dinamis dari hasil build (dist/).
 *
 * Sebelum dijalankan di hosting: npm run build (membuat folder dist/).
 */
import('./dist/server.js').catch((err) => {
  console.error('[Startup] Gagal menjalankan dist/server.js. Sudah menjalankan "npm run build"?', err);
  process.exit(1);
});
