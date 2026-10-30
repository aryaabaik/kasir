# AMANA POS --- Admin Pages Design System

## Scrapbook Editorial, Minimal, Purposeful UI

## 1. Tujuan

Redesign **seluruh halaman Admin yang sudah ada** agar konsisten dengan
arah visual scrapbook editorial dari referensi pengguna, tetapi tetap
sederhana, profesional, mudah digunakan, dan tidak terlihat seperti UI
hasil template AI generik.

Prinsip utama: **less, but intentional**. Jangan menambah kartu hanya
agar halaman terlihat penuh. Gunakan hierarki tipografi, whitespace yang
disengaja, garis pemisah halus, warna kertas, layout tabel/form yang
matang, dan beberapa aksen scrapbook terpilih.

Halaman Admin yang perlu diperiksa berdasarkan route yang ada: -
`/admin/dashboard` - `/products` - `/categories` - `/admin/stock` -
`/admin/transactions` - `/admin/reports` - `/admin/users`

Jangan mengasumsikan semua halaman menggunakan template yang sama.
Inspeksi project terlebih dahulu dan identifikasi template aktual untuk
setiap route.

## 2. Arah Visual

**Konsep:** Editorial scrapbook meets refined retail software.

Gunakan suasana kolase kertas---ivory, kraft, sobekan kertas, washi
tape, botanical line art, gelombang laut navy, dan aksen emas---namun
komposisinya tetap seperti software bisnis yang rapi.

Jangan menjadikan setiap elemen sebagai scrapbook. Elemen dekoratif
hanya menjadi identitas visual pada header, judul halaman, label kecil,
atau area terpilih. Area kerja utama tetap fungsional.

### Palet warna

-   Paper: `#F7F2E8`
-   Paper light: `#FFFCF6`
-   Kraft: `#C7A77B`
-   Kraft dark: `#A9875C`
-   Navy: `#244766`
-   Ocean: `#477FA3`
-   Blue wash: `#DCE8EE`
-   Gold: `#C7A45D`
-   Ink: `#26333A`
-   Muted: `#6D7778`
-   Line: `#E5DCCE`
-   Surface: `#FFFFFF`
-   Danger: `#B84E43`
-   Danger wash: `#F8E9E5`

Hindari border hitam tebal, hard shadow, gradient mencolok, banyak
badge/pill, glassmorphism, neon, font eksternal/CDN, emoji atau glyph
Unicode sebagai ikon, serta chart/angka palsu.

## 3. Aturan Anti-AI-Slop

1.  Jangan membungkus setiap bagian menjadi card. Gunakan struktur
    alami: heading, toolbar, tabel/form, separator, dan panel hanya bila
    benar-benar membantu.
2.  Batasi card yang menonjol. Banyak halaman sebaiknya menggunakan
    background kertas, divider tipis, dan area konten langsung.
3.  Maksimal satu aksen scrapbook utama per halaman, dengan aksen kecil
    tambahan bila perlu.
4.  Jangan menambah statistik, grafik, persentase, avatar, activity
    feed, atau mock content yang tidak ada di database.
5.  Jangan menggunakan ilustrasi stok generik atau gambar acak.
    Prioritaskan CSS dan SVG line art sederhana yang konsisten.
6.  Jangan membuat semua teks uppercase atau menggunakan micro-label
    secara berlebihan.
7.  Hierarki tombol harus jelas: primary, secondary, destructive, dan
    text-link.
8.  Animasi minimal; transisi pendek pada hover/focus saja bila berguna.
9.  Whitespace harus disengaja, tidak membuat halaman terasa rusak atau
    kosong tanpa tujuan.
10. Desain harus terasa dibuat khusus untuk AMANA POS, bukan template
    dashboard generik.

## 4. Shell Admin yang Konsisten

-   Pertahankan struktur navigasi admin dan semua route.
-   Pastikan sidebar, brand, ikon, active state, header, area konten,
    dan logout konsisten.
-   Jika sudah ada shared fragment/layout, gunakan yang ada. Jangan
    membuat sistem layout baru tanpa alasan kuat.
-   Jangan mengubah navigasi Kasir atau tampilan Kasir.
-   Jangan mengubah autentikasi, security, role mapping, atau POST
    logout.

### Page header

Setiap halaman harus memiliki judul jelas, deskripsi singkat yang
relevan, dan action utama pada posisi yang mudah ditemukan. Breadcrumb
hanya jika sesuai struktur yang sudah ada. Tambahkan aksen scrapbook
kecil jika cocok, bukan hero dekoratif besar di semua halaman.

### Tabel dan daftar

-   Prioritaskan keterbacaan data dan tindakan.
-   Header kolom jelas, jarak baris seimbang, divider tipis.
-   Hindari nested cards dan kotak di dalam kotak.
-   Search/filter/action toolbar berada dalam satu baris jika muat
    secara wajar.
-   Jangan menghapus pencarian, sorting, filter, pagination, empty
    state, feedback, atau aksi demi desain.
-   Jangan memotong data penting. Jika memakai ellipsis, pertahankan
    nilai lengkap lewat `title` atau detail yang sudah ada.
-   Pada desktop 1366px, hindari horizontal overflow jika memungkinkan.
    Jika tabel memang lebar, gunakan scroll terkontrol, bukan
    menyembunyikan kolom diam-diam.

### Form

-   Label eksplisit di atas input; jangan mengandalkan placeholder.
-   Kelompokkan field yang berkaitan tanpa membuat card untuk setiap
    kelompok.
-   Ukuran input, select, textarea, dan tombol konsisten.
-   Pertahankan pesan validasi/error/sukses yang sudah ada.
-   Aksi utama dan batal harus berbeda jelas.
-   Pertahankan `th:field`, `name`, `id`, method, action, validation,
    dan binding.
-   Jangan mengubah logika form atau mengganti submit menjadi
    JavaScript.

## 5. Spesifikasi Per Halaman

### A. Dashboard Admin --- `/admin/dashboard`

-   Ikuti `AMANA_POS_Scrapbook_Dashboard_Design.md` jika tersedia.
-   Pertahankan KPI, ringkasan bulanan, transaksi terbaru, peringatan
    stok, dan data yang ada.
-   Kolase utama diletakkan selektif pada header/sambutan.
-   Tabel tetap bersih.
-   Jangan menambah chart/data baru kecuali benar-benar tersedia dari
    backend dan disetujui.
-   Jika tema scrapbook sudah diterapkan, jangan merombak ulang; cukup
    selaraskan shell dan token jika perlu.

### B. Produk --- `/products`

-   Hierarki jelas: judul, deskripsi singkat, pencarian/filter jika ada,
    dan aksi "Tambah Produk".
-   Prioritaskan nama, kode, kategori, harga, varian/stok, dan tindakan
    hanya jika field tersebut benar-benar tersedia.
-   Jangan membuat setiap produk menjadi kartu besar. Gunakan tabel/list
    editorial yang cocok dengan data aktual.
-   Form create/edit harus nyaman, dengan informasi dasar dan varian
    ukuran/warna/stok yang jelas tanpa card bertumpuk.
-   Pertahankan auto-generated product code dan perilaku varian.
-   Jangan membuat ulang Model, Repository, atau Service hanya untuk
    mengubah UI.

### C. Kategori --- `/categories`

-   Daftar kategori sederhana dan rapi.
-   Nama kategori menjadi fokus; deskripsi/jumlah produk hanya jika
    sudah tersedia.
-   Action tambah kategori jelas dan form sederhana.
-   Edit/hapus dan konfirmasi yang ada tetap berfungsi.
-   Jangan membuat statistik palsu atau field database baru.

### D. Stok --- `/admin/stock`

-   Fokus pada stok dan kebutuhan restock.
-   Stok kritis mudah dipindai dengan warna lembut, bukan blok merah
    besar.
-   Tampilkan produk, ukuran/warna, stok tersedia, dan batas minimum
    jika tersedia.
-   Pertahankan pencarian/filter/penyesuaian stok yang ada.
-   Jangan menambah alur restock atau aksi yang belum didukung backend.

### E. Transaksi --- `/admin/transactions`

-   Jadikan tabel transaksi sebagai elemen utama.
-   Susun filter/search yang sudah ada dalam toolbar rapi.
-   Nomor invoice, waktu, kasir, total, dan status mudah dipindai jika
    tersedia.
-   Status badge minimal dan konsisten.
-   Detail transaksi dan tindakan tetap berfungsi.
-   Jangan membuat data contoh atau mengganti semantik status.

### F. Laporan --- `/admin/reports`

-   Prioritaskan filter periode dan ringkasan yang didukung controller.
-   Tata ulang chart/tabel yang sudah ada dengan jelas.
-   Jangan menambah chart palsu atau angka tren.
-   Gunakan satu visualisasi utama hanya jika data dan library yang ada
    mendukungnya.
-   Periode, total, dan format rupiah harus tetap benar.
-   Jangan mengubah perhitungan laporan.

### G. Pengguna --- `/admin/users`

-   Daftar pengguna jelas dengan username/nama dan role berdasarkan
    field aktual.
-   Role badge sederhana; aksi edit/hapus hanya jika sudah tersedia.
-   Form tambah/edit tidak perlu dipecah menjadi banyak kartu.
-   Jangan menampilkan data sensitif atau menambah field yang tidak ada.
-   Jangan mengubah aturan otorisasi atau pengelolaan role.

## 6. Responsif dan Kualitas Visual

-   Target desktop: 1366×768 dan 1920×1080.
-   Pastikan header tidak bertabrakan dengan action, tabel tidak
    terpotong, dan form tidak melebar berlebihan.
-   Pada viewport lebih kecil, gunakan wrapping atau scrolling
    terkontrol.
-   Hindari ukuran tetap yang menyebabkan layout pecah.
-   Focus-visible harus jelas, kontras memadai, dan reduced motion
    dihormati.
-   Animasi minimal; tidak perlu animasi masuk pada setiap komponen.

## 7. Batasan Teknis

-   Stack saat ini: Spring Boot, Thymeleaf, CSS, PostgreSQL. Ikuti
    struktur project aktual.
-   Jangan menambah dependency atau mengubah build setup.
-   Jangan mengubah Java backend kecuali benar-benar dibutuhkan untuk
    UI; minta persetujuan sebelum melakukannya.
-   Jangan mengganti nama template, route, controller, model attribute,
    atau binding tanpa alasan teknis kuat.
-   Jangan menghapus class/CSS global tanpa memeriksa halaman lain yang
    memakainya.
-   Hindari menulis ulang stylesheet global jika token dan selector
    scoped cukup.
-   Jangan menduplikasi sidebar/header jika sudah ada fragment yang
    cocok.

## 8. Workflow OpenCode

### Tahap 1 --- Audit

1.  Inspect semua route Admin dan template aktual.
2.  Catat file CSS bersama, fragment/layout, dan dependency ikon/chart
    yang tersedia.
3.  Identifikasi fitur tiap halaman: filter, submit, action, route,
    binding, pagination, empty state.
4.  Catat fungsi yang tidak boleh berubah.
5.  Jangan mulai coding sebelum mapping route → template → stylesheet
    selesai.

### Tahap 2 --- Rencana

1.  Laporkan singkat file yang akan diubah dan alasannya.
2.  Tentukan apakah shared shell bisa digunakan tanpa perubahan besar.
3.  Buat rencana per halaman; jangan gunakan satu template generik untuk
    semuanya.
4.  Terapkan scrapbook secara selektif, bukan sebagai dekorasi besar
    yang diulang di semua halaman.

### Tahap 3 --- Implementasi

1.  Kerjakan satu halaman/kelompok kecil pada satu waktu.
2.  Gunakan design tokens bersama bila cocok dengan struktur aktual.
3.  Pertahankan semua binding dan form.
4.  Jangan menambahkan mock data, aksi palsu, chart palsu, atau
    statistik rekaan.
5.  Jangan membuat banyak card baru.
6.  Periksa diff setelah setiap kelompok halaman.

### Tahap 4 --- Verifikasi

1.  Jalankan `.\mvnw.cmd compile`.
2.  Jalankan `.\mvnw.cmd test-compile`.
3.  Periksa diff dan pastikan backend/security/business logic tidak
    berubah.
4.  Pastikan `th:action`, `th:href`, `th:field`, `th:each`, route, dan
    binding tetap benar.
5.  Jika browser tersedia, periksa tiap halaman pada desktop target.
6.  Jika tidak bisa menjalankan browser, laporkan bahwa pemeriksaan
    visual manual masih diperlukan.
7.  Laporkan halaman/file yang diubah, hasil build, dan hal yang perlu
    diuji manual.

## 9. Acceptance Criteria

-   Semua halaman Admin memakai design language yang konsisten.
-   Scrapbook terasa elegan dan selektif, tidak berlebihan.
-   Tidak banyak card; gunakan whitespace, typography, divider, tabel,
    dan form secara tepat.
-   Setiap halaman dirancang sesuai tugasnya, bukan copy-paste template
    generik.
-   Tidak ada ikon kotak, teks terpotong, tabel rusak, atau tombol
    kehilangan fungsi.
-   Tidak ada mock data, angka palsu, chart palsu, atau aksi yang tidak
    terhubung.
-   Route, role, security, form, Thymeleaf binding, dan business logic
    tetap terjaga.
-   Compile dan test-compile berhasil atau kegagalannya dijelaskan
    dengan tepat.

## 10. Instruksi Akhir

Audit semua halaman Admin yang benar-benar ada di project, lalu lakukan
redesign visual sesuai spesifikasi ini. Jangan mengasumsikan nama
template atau field; baca implementasi aktual terlebih dahulu.
Prioritaskan konsistensi, keterbacaan, detail yang purposeful, dan
minimnya card. Jangan menyentuh fitur yang tidak terkait desain. Jika
perubahan visual memerlukan backend atau schema, hentikan bagian itu dan
jelaskan alasannya sebelum mengubahnya.
