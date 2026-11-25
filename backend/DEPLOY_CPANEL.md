# Deploy Backend Cafe 3 Rasa ke Hosting cPanel (tkj3a.my.id)

Database: MariaDB `tkjh1984_kel7` (server yang sama dengan backend → query sangat cepat).
Backend + web pelanggan berjalan sebagai **satu** aplikasi Node.js.

## 0. Persiapan di laptop
```bash
cd backend
npm install
npm run build          # menghasilkan folder dist/
```
Lalu buat ZIP berisi folder **backend** (TANPA `node_modules` dan TANPA `.env`) dan folder **tigarasa-web**.

## 1. Atur charset database (sekali saja)
phpMyAdmin → pilih database `tkjh1984_kel7` → tab **SQL** → jalankan:
```sql
ALTER DATABASE tkjh1984_kel7 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 2. Siapkan alamat (subdomain)
cPanel → **Domains** → buat subdomain, mis. `kelompok7.tkj3a.my.id`
(Document root boleh dibiarkan bawaan). Jalankan **SSL/TLS Status → Run AutoSSL**.

## 3. Upload file
cPanel → **File Manager** → buka folder home (`/home/tkjh1984`) → buat folder `kel7-cafe3rasa`
→ Upload ZIP → **Extract**. Struktur akhirnya:
```
/home/tkjh1984/kel7-cafe3rasa/
├── backend/        (app.cjs, dist/, prisma/, src/, package.json, ...)
└── tigarasa-web/   (index.html, js/, css/, img/, ...)
```
> Jangan taruh di dalam `public_html` agar file `.env` tidak bisa dibuka dari browser.

## 4. Buat file `.env` di server
File Manager → masuk `kel7-cafe3rasa/backend` → **+ File** → nama `.env` → Edit, isi:
```
NODE_ENV=production
CORS_ORIGIN=*
JWT_SECRET=<string acak panjang>
JWT_EXPIRES_IN=7d
TAX_RATE_PERCENT=0
DATABASE_URL="mysql://<user_mysql>:<password_url_encoded>@localhost:3306/tkjh1984_kel7"
SEED_KASIR_PASSWORD=kasir123
SEED_ADMIN_PASSWORD=admin123
```
(Aktifkan **Settings → Show Hidden Files** di File Manager agar `.env` terlihat.)

## 5. Buat aplikasi Node.js
cPanel → **Setup Node.js App** → **Create Application**:
| Isian | Nilai |
|---|---|
| Node.js version | versi tertinggi (≥ 18) |
| Application mode | Production |
| Application root | `kel7-cafe3rasa/backend` |
| Application URL | `kelompok7.tkj3a.my.id` |
| Application startup file | `app.cjs` |

Klik **Create** → lalu **Run NPM Install** (tunggu sampai selesai).

## 6. Buat tabel & data awal
Di halaman aplikasi Node.js → bagian **Run JS script** → pilih `db:setup` → **Run**.
(Membuat semua tabel di MariaDB + 5 menu, 3 kategori, 12 meja, akun kasir & admin.)

Jika memakai Terminal cPanel: salin perintah "source ..." di bagian atas halaman aplikasi, lalu
```bash
npm run db:setup
```

## 7. Jalankan & tes
Klik **Restart**. Cek di browser:
- `https://kelompok7.tkj3a.my.id/api/health` → `"database":"connected"`
- `https://kelompok7.tkj3a.my.id/?meja=04` → web pelanggan

App kasir: di layar login ketuk **Server** → isi `kelompok7.tkj3a.my.id` → Tes Koneksi → Simpan.

## Update kode berikutnya
1. Di laptop: `npm run build`
2. Upload ulang folder yang berubah (`dist/`, `prisma/`, `tigarasa-web/`) ke server (Replace)
3. Jika `schema.prisma` berubah: Run JS script → `db:push`
4. **Restart** aplikasi di Setup Node.js App

## Catatan
- Akun cPanel dipakai bersama satu kelas: semua orang yang bisa login cPanel bisa membaca `.env`.
  Jangan pakai password yang sama dengan akun pribadi.
- Jika real-time terasa lambat, Socket.io otomatis memakai mode *polling* (tetap berfungsi).
