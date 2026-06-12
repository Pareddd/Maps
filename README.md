# Radar Ekspedisi 🚚

Selamat datang di **Radar Ekspedisi**, sebuah aplikasi Android inovatif yang dirancang untuk menyederhanakan pengalaman pelacakan logistik. Radar Ekspedisi memadukan prinsip desain UI/UX modern dengan sistem manajemen data lokal yang tangguh untuk memberikan platform navigasi dan estimasi pengiriman yang komprehensif bagi pengguna di wilayah Makassar dan Gowa.

<p align="center">
  <img src="app/src/main/res/drawable/logo_radar.png" alt="Radar Ekspedisi Logo" width="120px" />
</p>

## 🌟 Ringkasan

Radar Ekspedisi bukan sekadar alat pemetaan biasa. Ini adalah ekosistem logistik yang dirancang dengan cermat untuk membantu pengguna menemukan agen ekspedisi terdekat (seperti JNE, J&T, SiCepat, dan SPX), menghitung estimasi ongkos kirim, serta memberikan ulasan berbasis komunitas.
Baik kamu seorang mahasiswa, pemilik bisnis lokal, atau seseorang yang sering mengirim paket, Radar Ekspedisi memberikan pengalaman yang mulus, tangguh, dan interaktif untuk semua kebutuhan pengirimanmu.

## ✨ Fitur Utama

*   **Peta Interaktif**: Visualisasi peta secara real-time yang didukung oleh OSM dan `OSRMRoadManager` untuk perencanaan rute yang efisien.
*   **Perhitungan Jarak Cerdas**: Mengimplementasikan rumus Haversine untuk mendeteksi agen pengiriman terdekat dengan akurat berdasarkan lokasi pengguna.
*   **Kalkulator Ongkir**: Estimasi biaya pengiriman dinamis berdasarkan wilayah tujuan, berat paket, dan kategori barang (Pakaian, Elektronik, dll).
*   **Ulasan Komunitas**: Sistem ulasan terstruktur dengan umpan balik komunitas dan fungsi penilaian.
*   **Dukungan Offline**: Penyimpanan data lokal menggunakan SQLite memastikan aplikasi tetap berfungsi meskipun tanpa koneksi jaringan.
*   **Sistem Favorit**: Fitur "Bookmark" yang dipersonalisasi untuk mengakses agen pengiriman favorit dengan cepat.
*   **UI/UX Modern**: Dibangun dengan fokus pada estetika dan kegunaan, dilengkapi dengan tema Gelap/Terang yang adaptif.

## 🛠 Tech Stack

Radar Ekspedisi menggunakan praktik dan teknologi pengembangan Android modern:
*   **Platform**: Android (Min SDK 24)
*   **Bahasa**: Java
*   **Build System**: Gradle
*   **Arsitektur**: Navigation Component, ViewBinding
*   **Networking**: Retrofit, OkHttp
*   **Database**: SQLite (melalui `DatabaseHelper` dengan migrasi versi)
*   **Lokasi**: osmdroid, OSRM API

## 📋 Prasyarat

Untuk membangun dan menjalankan proyek ini, kamu akan membutuhkan:
*   **Android Studio**: Versi stabil terbaru sangat disarankan.
*   **Java Development Kit (JDK)**: JDK 17 atau lebih tinggi.
*   **Android SDK**: Versi 34/35.

## 🚀 Instalasi dan Pengaturan

Ikuti langkah-langkah berikut untuk menjalankan proyek di perangkatmu:
1. **Clone Repository**:
   `git clone <repository-url>`
2. **Buka Proyek**: Luncurkan Android Studio, pilih **File > Open**, dan arahkan ke direktori proyek.
3. **Build dan Run**: Izinkan Gradle untuk melakukan sinkronisasi dependensi, pilih perangkat target, dan klik tombol **Run**.

## 📂 Struktur Proyek

*   **api/**: Berisi client Retrofit dan antarmuka service untuk operasi jaringan.
*   **db/**: Menyimpan `DatabaseHelper` untuk manajemen SQLite, termasuk seeder data dummy dan definisi skema.
*   **model/**: Berisi model data seperti `LocationModel` dan `ReviewData`.
*   **ui/**: Mencakup `ListFragment` dan `MapFragment` untuk tampilan berbasis navigasi.
*   **adapter/**: Berisi `LocationAdapter` untuk mengelola data RecyclerView dan interaksi long-press.

## 🤝 Panduan Kontribusi

Kami mengikuti **Semantic Versioning** dan conventional commits. Untuk berkontribusi:
1. Fork repository ini.
2. Buat branch fitur: `git checkout -b feat/nama-fitur-kamu`
3. Lakukan commit menggunakan awalan semantik (contoh: `feat:`, `fix:`, `chore:`).
4. Push ke branch: `git push origin feat/nama-fitur-kamu`
5. Buka Pull Request.

## 📄 Lisensi

Proyek ini dilisensikan di bawah MIT License. Silakan gunakan, modifikasi, dan distribusikan sesuai dengan ketentuan lisensi.

---
Dibuat dengan ❤️ oleh **Rizky Nur Fariid (NIM H071241037)**
