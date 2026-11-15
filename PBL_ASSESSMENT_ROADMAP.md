# 📋 Roadmap & Assessment Checklist PBL (Cafe 3 Rasa)

Dokumen ini memetakan kriteria penilaian **Project-based Learning (PBL)** dan standar **Tech Stack** ke dalam status pengerjaan proyek saat ini serta rencana pengembangan (Task List).

---

## 🎯 1. Kriteria Penilaian Aplikasi (Bobot: 50 Point)

| No | Kriteria Penilaian | Bobot | Status Saat Ini | Keterangan / Rencana Pengembangan |
|---|---|---|---|---|
| **1** | **Splash Screen** | 5 Point | ✅ **SUDAH DIBUAT** | Animasi logo "Tiga Rasa" dinamis sudah selesai di `SplashScreen.kt` dan otomatis beralih ke layar berikutnya. |
| **2** | **On Boarding** | 5 Point | ⏳ **BELUM DIBUAT** | Perlu dibuat slider/carousel onboarding 3 slide (pengenalan sistem POS, digital QR ordering pelanggan, dan monitoring statistik penjualan). |
| **3** | **Activity Login (SharedPreferences) & Register** *(Konsep CRUD)* | 5 Point | 🟡 **SEBAGIAN** | - Layar Login sudah ada di `LoginScreen.kt` (Kasir & Admin).<br>- *Yang belum:* Persistence sesi menggunakan SharedPreferences / DataStore dan form pendaftaran/register akun baru. |
| **4** | **Dashboard** | 5 Point | ✅ **SUDAH DIBUAT** | - **Dashboard Kasir:** Peta meja, detail pesanan, quick menu, pembayaran.<br>- **Dashboard Admin:** Ringkasan statistik, grafik omzet, menu terlaris. |
| **5** | **Fitur-Fitur Aplikasi (CRUD & Order)** | 20 Point | ✅ **SUDAH DIBUAT** | - **Order & Transaksi:** Input order, ubah kuantiti (+/-), catatan menu, konfirmasi bayar, ganti meja.<br>- **CRUD Produk:** Tambah produk baru, hapus produk, toggle ketersediaan stok.<br>- **CRUD Meja & QR:** Tambah meja, filter meja, cetak/lihat QR pemesanan.<br>- **Kategori Menu:** Tambah dan hapus kategori makanan/minuman/camilan. |
| **6** | **Menu Info Aplikasi, Deskripsi & Nama Pencipta** | 5 Point | ⏳ **BELUM DIBUAT** | Perlu dibuat halaman modal/screen dialog "Tentang Aplikasi" yang menampilkan versi aplikasi, deskripsi sistem Cafe 3 Rasa, dan nama tim pengembang (nama mahasiswa/anggota PBL). |
| **7** | **Logout** | 5 Point | ✅ **SUDAH DIBUAT** | Berfungsi di Dashboard Kasir (dropdown ⚙️) dan Panel Admin (menu Keluar). Otomatis membersihkan sesi dan kembali ke halaman Login. |

> **Total Estimasi Nilai Saat Ini:** ~35 / 50 Point.  
> *Sisa 15 point:* Onboarding (5), Register + SharedPreferences (5), dan Halaman Info/Pencipta Aplikasi (5).

---

## 🛠️ 2. Keselarasan Standar Tech Stack

| Komponen | Standar yang Disarankan | Implementasi Proyek Saat Ini | Status |
|---|---|---|---|
| **UI Framework** | Jetpack Compose + Material 3 | Jetpack Compose + Compose Multiplatform + Material 3 | ✅ Sesuai Standar |
| **Architecture** | MVVM + Repository Pattern | State Management berbasis Compose (`AdminStore`, `CashierDashboardState`) | 🟡 Bertahap menuju Repository |
| **Networking** | Retrofit + OkHttp / Ktor | Ktor Client / REST API Mock | ⏳ Tahap integrasi backend |
| **Asynchronous** | Kotlin Coroutines + Flow | Kotlin Coroutines (`rememberCoroutineScope`, `LaunchedEffect`) | ✅ Sesuai Standar |
| **Local Database** | Room / DataStore / SharedPrefs | In-Memory Reactive List (`mutableStateListOf`) | ⏳ Perlu integrasi DataStore / Room |
| **Backend** | PHP + MySQL, Laravel, Firebase, dll | Web statis + Local API Mock | ⏳ Rencana integrasi Firebase / Laravel |
| **Authentication** | Firebase Auth / Secure Backend Auth | Role-based Auth (`UserAccount`, Kasir vs Admin) | 🟡 Siap dihubungkan ke Firebase / API |
| **UI/UX Design** | Figma | Anti-AI Solid Design System (Material 3 Dark/Light) | ✅ Sesuai Standar |
| **Version Control**| Git + GitHub | Monorepo Git di GitHub (`dantenihhh-ops/cafe-3-rasa`) | ✅ Sesuai Standar |
| **Project Management**| GitHub Projects / Trello | GitHub Issues & Project Tracking | ⏳ Siap dimasukkan ke GitHub Projects |

---

## 📌 3. Action Plan Pengerjaan Selanjutnya (Next Sprint)

Untuk memenuhi **nilai maksimal (50/50)**, berikut roadmap prioritas yang perlu dikerjakan:

- [ ] **Sprint 1: Onboarding Screen (5 Point)**
  - Buat layar onboarding interaktif 3 langkah setelah Splash Screen untuk pengguna pertama.
  - Tambahkan tombol "Lewati" dan "Mulai Sekarang" menuju halaman Login.
- [ ] **Sprint 2: Persistence SharedPreferences / DataStore & Register (5 Point)**
  - Simpan sesi login (`isLoggedIn`, role pengguna, nama user) ke penyimpanan lokal agar tidak perlu login ulang saat aplikasi ditutup.
  - Tambahkan form Register akun kasir baru.
- [ ] **Sprint 3: Halaman Info Aplikasi & Nama Pencipta (5 Point)**
  - Tambahkan menu "Info Aplikasi" di header / settings kasir dan admin.
  - Tampilkan profil tim pengembang PBL, logo kampus, deskripsi sistem, dan versi rilis aplikasi.
- [ ] **Sprint 4: Integrasi GitHub Projects**
  - Buat Kanban board di tab **Projects** repositori GitHub untuk mendokumentasikan setiap milestone di atas.
