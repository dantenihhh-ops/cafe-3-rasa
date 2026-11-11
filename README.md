# ☕ Cafe 3 Rasa — Qris Digital & Kasir

![Platform](https://img.shields.io/badge/platform-Desktop%20%26%20Android-31837F?style=flat-square)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Compose%20Multiplatform-1.7.0-4285F4?style=flat-square)
![Web](https://img.shields.io/badge/Web-HTML%2FCSS%2FJS-E34F26?style=flat-square)

> Aplikasi pemesanan digital untuk kafe **Cafe 3 Rasa by Maharewa** — pelanggan memesan lewat QR di meja, kasir mengelola meja & pesanan, admin mengelola produk, statistik, dan QR meja.

---

## 🧩 Gambaran Besar

Project ini terdiri dari **2 bagian** dalam satu monorepo:

| Modul | Teknologi | Untuk siapa | Deskripsi |
|---|---|---|---|
| **`composeApp`** | Kotlin + Compose Multiplatform (Desktop **&** Android) | Kasir & Admin | Klim kasir (POS), panel admin (produk, statistik, meja/QR), splash screen |
| **`tigarasa-web`** | HTML/CSS/JavaScript (statis) | Pelanggan | Menu, konfirmasi pesanan, live status, bantuan (dibuka lewat QR meja) |

```
cafe-3-rasa/
├── composeApp/            # Kotlin Multiplatform — app kasir & admin
│   └── src/
│       ├── commonMain/    # Kode yang dipakai bersama (Desktop + Android)
│       ├── desktopMain/   # Entry point Desktop (Main.kt)
│       └── androidMain/   # Entry point Android (MainActivity.kt)
└── tigarasa-web/          # Web pelanggan (statis)
    ├── index.html         # Menu Catalog
    ├── pesanan.html       # Konfirmasi Pesanan
    ├── status.html        # Order Summary / live status
    ├── bantuan.html       # Cashier Sync / bantuan
    ├── css/style.css
    └── js/{app,data}.js
```

---

## ✨ Fitur

### 🛒 Web Pelanggan (`tigarasa-web`)
- **Menu Catalog** — kategori, menu unggulan, daftar menu + harga, tambah ke keranjang
- **Konfirmasi Pesanan** — ubah jumlah (stepper), catatan per menu, ringkasan tagihan + PB1 10%, tombol kirim
- **Order Summary** — live status pesanan, timer estimasi masak (ETA), rincian & total
- **Cashier Sync / Bantuan** — panggil waiter, minta bill, minta alat makan/tambahan
- Keranjang & order tersimpan di `localStorage` per nomor meja
- Mode meja dibaca dari URL: `index.html?meja=04`

### 🧾 App Kasir (POS) — `composeApp`
- **Peta Meja** — status tiap meja (terisi / kosong / butuh bill), filter, pilih meja
- **Pesanan per Meja** — ubah jumlah, catatan, toggle okupansi, ganti meja, kosongkan meja
- **Quick Menu** — tambah menu cepat ke pesanan
- **Ringkasan Pembayaran** — sub-total, status tagihan, konfirmasi lunas

### 🛠 Panel Admin — `composeApp`
- **05 Login** — gerbang masuk ke panel admin
- **07 Produk & Kategori** — daftar produk, tambah/edit/hapus, toggle ketersediaan, kelola kategori
- **08 Log & Statistik** — KPI, grafik pendapatan per jam, menu terlaris, log pesanan (filter + pagination)
- **09 Meja & QR** — daftar meja, tambah meja, preview + cetak QR meja

### 🌀 Splash Screen
- Animasi logo "Tiga Rasa" saat app dimulai

---

## 🚀 Cara Menjalankan

### Prasyarat
| Kebutuhan | Versi |
|---|---|
| Java (JDK) | 17+ (disarankan 21) |
| Android SDK | API 34 (untuk build Android) |
| Gradle | Sudah via wrapper (`gradlew`) |

### Web pelanggan (tanpa server)
Web ini statis — cukup buka dengan **Live Server** (VS Code):

1. Buka folder `tigarasa-web` di VS Code.
2. Install ekstensi **Live Server** (Ritwick Dey).
3. Klik kanan `index.html` → **Open with Live Server**.
4. Tambah nomor meja di URL, contoh: `http://127.0.0.1:5500/index.html?meja=04`

> 📱 **Coba dari HP:** pastikan HP & laptop dalam WiFi yang sama. Cari IP laptop (`ipconfig` → IPv4 Address), lalu buka `http://<IP-laptop>:5500/index.html?meja=04`.

### App kasir / admin (Desktop)
```bash
# jalankan langsung
./gradlew :composeApp:run

# atau build distribusi
./gradlew :composeApp:packageDmg      # macOS
./gradlew :composeApp:packageMsi      # Windows
./gradlew :composeApp:packageDeb      # Linux
```

### App kasir / admin (Android)
Buka folder project di **Android Studio**, tunggu sync, lalu tekan ▶ Run pada konfigurasi `composeApp` (Android).

---

## 🔑 Alur Login Admin

```
Splash → POS Kasir (Peta Meja)
           └─ tombol ⚙ (header) → Login Admin → Panel Admin
                 └─ footer nav: Produk & Kategori / Log & Statistik / Meja & QR / Keluar
```

> Catatan: saat ini login bersifat simulasi (data statis). Belum ada autentikasi sungguhan.

---

## 🧪 Perintah Build & Test

| Perintah | Fungsi |
|---|---|
| `./gradlew :composeApp:compileKotlinDesktop` | Kompilasi cepat (Desktop) — cek kode |
| `./gradlew :composeApp:build` | Build lengkap (semua target) |
| `./gradlew :composeApp:run` | Jalankan app Desktop |
| `./gradlew :composeApp:test` | Jalankan unit test (jika ada) |

---

## 📁 Struktur Kode (Compose)

```
com.cafe3rasa.kasir/
├── App.kt                      # Root: splash → kasir → login → admin
├── model/                      # data class & enum
│   ├── MenuItem.kt             # MenuItemModel, OrderItemModel, OrderLogEntry, dll
│   ├── Table.kt                # TableModel, TableStatus
│   └── TableRequest.kt         # TableRequest, RequestBadgeType
├── state/                      # state holder (bukan ViewModel — murni Compose)
│   ├── CashierDashboardState.kt
│   └── AdminPanelState.kt
├── theme/                      # warna & tipografi (Color/Theme/Type.kt)
├── splash/                     # splash screen
└── ui/
    ├── CashierDashboardScreen.kt
    ├── components/             # section komponen kasir (header, order, payment, dll)
    └── admin/                  # panel admin
        ├── AdminChrome.kt      # header + footer nav admin
        ├── AdminLoginScreen.kt
        ├── AdminProdukScreen.kt
        ├── AdminLogScreen.kt
        ├── AdminMejaQrScreen.kt
        └── AdminPanel.kt       # kerangka panel (header + isi + footer)
```

---

## 🎨 Tema & Palet

Palet diambil dari desain UI project (lihat `theme/Color.kt`):

| Nama | Warna | Dipakai untuk |
|---|---|---|
| Teal | `#318380` | Aksen utama, tombol, elemen aktif |
| Navy | `#1B2A4B` | Judul, header, teks kuat |
| Coral/Rust | `#C1573F` | Aksen sekunder, status, tombol tegas |
| Cream | `#F7EFDC` | Latar dekoratif (login arch) |
| Mint | `#E8F2F1` | Latar soft / status sukses |
| Peach | `#F9E6E0` | Latar soft / status peringatan |

---

## ⚠️ Status & Roadmap

Aks **dijalankan** (untuk demo PBL), tapi **belum tersambung backend**:

- [x] UI & alur semua layar (mobile + desktop)
- [x] Data statis / mock untuk demo
- [ ] **Koneksi ke backend** (Firebase / REST API) — pesanan pelanggan masuk ke kasir
- [ ] Autentikasi admin sungguhan (bukan simulasi)
- [ ] Generator QR sungguhan (saat ini placeholder dekoratif)
- [ ] Sinkronisasi real-time antara web & kasir

---

## 🧑‍🤝‍🧑 Anggota (Kelompok 7 PBL)

> *(ubah sesuai anggota kelompok)*

| Nama | NIM | Peran |
|---|---|---|
| *(Nama)* | *(NIM)* | Project Leader |
| *(Nama)* | *(NIM)* | Backend / Integrasi |
| *(Nama)* | *(NIM)* | Mobile (Compose) |
| *(Nama)* | *(NIM)* | Web Pelanggan |
| *(Nama)* | *(NIM)* | Desain UI/UX |

---

## 📜 Lisensi

© 2026 Cafe 3 Rasa by Maharewa. Project ini dibuat untuk keperluan **Proyek Belajar Lapangan (PBL)**.
