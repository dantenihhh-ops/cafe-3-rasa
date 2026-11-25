# Backend Cafe 3 Rasa

REST API + Socket.io untuk Web Pelanggan (QR meja) dan Aplikasi Kasir/Admin.
Stack: Node.js, TypeScript, Express, Prisma ORM, PostgreSQL (Supabase).

## Setup pertama kali

```bash
cd backend
npm install
copy .env.example .env      # Windows (Mac/Linux: cp .env.example .env)
```

Isi `.env`:
1. `DATABASE_URL` & `DIRECT_URL` — ganti `[YOUR-PASSWORD]` dengan password database Supabase
   (Supabase → Project Settings → Database). Jika password berisi simbol seperti `@ # / ?`, ubah dulu
   ke format URL (mis. `@` → `%40`).
2. `JWT_SECRET` — isi string acak:
   `node -e "console.log(require('crypto').randomBytes(48).toString('hex'))"`

Lalu buat tabel & isi data awal:

```bash
npm run setup    # = prisma db push + seed (5 menu, 3 kategori, 12 meja, akun kasir & admin)
npm run dev      # server di http://localhost:5000
```

Cek: buka http://localhost:5000/api/health → harus `"database": "connected"`.
Web pelanggan: http://localhost:5000/?meja=04

Perintah lain: `npm run prisma:studio` (lihat isi database di browser), `npm run seed` (isi ulang data awal),
`npm run build && npm start` (mode production).

## Akun awal (dari seed)
| Username | Password | Peran |
|---|---|---|
| kasir | kasir123 | KASIR |
| admin | admin123 | ADMIN |

## Aturan bisnis
- Pembayaran: **Cash** dan **Transfer**.
- Tanpa pajak (`TAX_RATE_PERCENT=0`). Jika suatu saat perlu PB1, ubah nilai ini dan `TAX_RATE` di `tigarasa-web/js/data.js`.
- Harga & nama menu selalu diambil server dari database, bukan dari data yang dikirim browser.
- Satu meja = satu tagihan aktif: pesanan tambahan dari meja yang belum bayar digabung ke pesanan yang sama.

## Daftar Endpoint

🔓 publik · 🔑 login (Kasir/Admin, header `Authorization: Bearer <token>`) · 👑 khusus Admin

| Method | Endpoint | Akses | Keterangan |
|---|---|---|---|
| GET | /api/health | 🔓 | Status server & database |
| POST | /api/auth/login | 🔓 | `{ username, password }` → `{ token, user }` |
| GET | /api/auth/me | 🔑 | Data user yang login |
| GET | /api/menu | 🔓 | `?category=Makanan&search=kopi&availableOnly=true` |
| PATCH | /api/menu/:id/toggle | 🔑 | Tersedia ↔ Habis |
| POST | /api/menu | 👑 | `{ name, categoryId, price, image?, description?, filters? }` |
| PATCH | /api/menu/:id | 👑 | Ubah produk |
| DELETE | /api/menu/:id | 👑 | Hapus produk |
| GET | /api/categories | 🔓 | Daftar kategori + jumlah produk |
| POST | /api/categories | 👑 | `{ name }` |
| DELETE | /api/categories/:id | 👑 | Ditolak jika masih berisi produk |
| GET | /api/tables | 🔑 | Peta meja + pesanan aktif |
| PATCH | /api/tables/:id/status | 🔑 | `{ status: "KOSONG" \| "TERISI_BELUM_BAYAR" }` |
| POST | /api/tables/:id/clear | 🔑 | Kosongkan meja (harus sudah lunas) |
| POST | /api/tables/:id/move | 🔑 | Ganti meja `{ targetTableId }` |
| POST | /api/tables | 👑 | `{ number, area, capacity }` |
| PATCH | /api/tables/:id | 👑 | Ubah area / kapasitas |
| DELETE | /api/tables/:id | 👑 | Hanya meja tanpa riwayat pesanan |
| POST | /api/orders | 🔓 | Pesanan web `{ tableNumber, items:[{ menuItemId, quantity, note? }] }` |
| GET | /api/orders/table/:tableNumber/active | 🔓 | Pesanan aktif meja (halaman Status) |
| GET | /api/orders | 🔑 | Log pesanan `?status=&payment=&search=&from=&to=&limit=` |
| GET | /api/orders/:id | 🔑 | Detail pesanan |
| POST | /api/orders/kasir | 🔑 | Pesanan manual dari kasir (format sama dengan web) |
| POST | /api/orders/:id/items | 🔑 | Tambah item `{ items:[...] }` |
| PATCH | /api/orders/:id/items/:itemId | 🔑 | Ubah porsi/catatan `{ quantity?, note? }` (quantity 0 = hapus) |
| PATCH | /api/orders/:id/confirm | 🔑 | Konfirmasi & kirim ke dapur |
| PATCH | /api/orders/:id/serve | 🔑 | Tandai sudah disajikan/diantar ke meja |
| PATCH | /api/orders/:id/payment | 🔑 | `{ paymentMethod: "CASH"\|"TRANSFER", paymentBillStatus: "LUNAS"\|"BELUM_BAYAR" }` |
| PATCH | /api/orders/:id/cancel | 🔑 | Batalkan pesanan belum bayar |
| POST | /api/requests | 🔓 | Panggil waiter / minta bill / alat makan `{ tableNumber, message }` |
| GET | /api/requests | 🔑 | Permintaan aktif |
| PATCH | /api/requests/:id/respond | 🔑 | `{ responseMessage }` |
| PATCH | /api/requests/:id/done | 🔑 | Tandai selesai |
| POST | /api/reviews | 🔓 | `{ tableNumber, rating, aspects?, comment? }` |
| GET | /api/reviews | 👑 | Ulasan + rata-rata rating |

## Event Socket.io

| Arah | Event | Keterangan |
|---|---|---|
| client → server | `join_table` (`"04"`) | Web meja bergabung ke room mejanya |
| client → server | `join_cashier` (`{ token }`) | Kasir/Admin bergabung (wajib token login) |
| server → kasir | `new_order`, `order_updated`, `call_waiter`, `request_updated`, `table_cleared`, `table_moved` | |
| server → meja | `order_confirmed`, `order_served`, `order_updated`, `payment_completed`, `service_confirmed`, `service_completed` | |
| server → semua | `table_status_changed`, `tables_changed`, `menu_changed` | |
