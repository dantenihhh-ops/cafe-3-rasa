# Arsip — Layar Admin versi awal

Layar admin yang dibuat untuk **PR #1** (`feat/admin-screens`, merged sebagai `cf1ccc8` → merge `0ea12bd`).
Isinya mengikuti mockup desain PDF halaman 05, 07, 08, 09:

| File | Layar |
|---|---|
| `AdminPanel.kt` | kerangka panel (header + isi + footer nav) |
| `AdminChrome.kt` | `AdminHeader`, `AdminFooterNav`, `AdminTextButton` |
| `AdminProdukScreen.kt` | 07 Produk & Kategori |
| `AdminLogScreen.kt` | 08 Log & Statistik |
| `AdminMejaQrScreen.kt` | 09 Meja & QR (+ `QrCode` canvas placeholder) |

## Kenapa diarsipkan

Commit `5ce5704`..`ecb0333` (Nov 2026) menambahkan implementasi panel admin baru —
`AdminScreen` + `ProdukScreen` / `LaporanScreen` / `MejaScreen` / `AkunScreen` — dan
sejak itu `App.kt` memakai jalur baru tersebut. File di sini **tidak dipanggil dari mana pun**,
jadi dipindahkan keluar dari source set supaya tidak ikut dikompilasi dan tidak bentrok
dengan implementasi yang dipakai (mis. dua definisi `AdminHeader` dan dua enum `AdminTab`).

Kode masih utuh dan tersimpan di riwayat git: `git show cf1ccc8 -- <path>`.

## Kalau mau dipakai lagi

Pindahkan kembali ke `composeApp/src/commonMain/kotlin/com/cafe3rasa/kasir/ui/admin/`,
lalu sambungkan `AdminPanel(state)` ke navigasi. Perlu dicek dulu terhadap API
`state/AdminPanelState.kt` yang sekarang (enum `AdminTab` di `ui/admin/AdminModels.kt`
berbeda dengan `AdminTab` di `state/`).
