# AMANA POS --- Admin Dashboard Scrapbook Redesign

## 1. Tujuan

Redesign **hanya tampilan visual** halaman Admin Dashboard AMANA POS
agar terinspirasi dari gambar referensi scrapbook/collage yang diberikan
pengguna. Jangan mengubah perilaku aplikasi atau membuat ulang dashboard
dari nol secara fungsional.

Arah visual: **editorial scrapbook, handmade paper collage, calm coastal
botanical**. Hasil harus terasa artistik, hangat, rapi, profesional, dan
tetap mudah dibaca sebagai dashboard POS---bukan halaman poster yang
sulit digunakan.

## 2. Referensi Visual yang Wajib Diikuti

Gambar referensi memiliki ciri berikut:

1.  Kolase kertas bertekstur: kraft cokelat, karton krem, kertas ivory,
    kertas robek, dan lapisan potongan kertas yang terlihat alami.
2.  Washi tape bercorak kotak-kotak dan aksen pita/kertas yang menempel
    pada kolase.
3.  Ilustrasi botanical line-art biru, bunga kering, siluet figur
    manusia minimalis, gelombang laut biru dengan garis emas, serta pola
    ornamen tradisional.
4.  Ruang kosong yang luas dan sengaja disisakan untuk menjaga
    keterbacaan.
5.  Palet dominan krem/ivory, kraft/tan, biru laut/navy, dengan aksen
    emas lembut.
6.  Komposisi asimetris: kolase dekoratif berada di satu sisi, sedangkan
    area konten utama tetap lapang dan mudah dibaca.

Jangan menyalin gambar sebagai satu screenshot besar yang menutupi
dashboard. Gunakan referensi sebagai **bahasa visual** untuk membangun
antarmuka nyata.

## 3. Batasan Utama: Desain Saja

-   Jangan mengubah business logic, perhitungan, query, entity,
    repository, service, security, role, autentikasi, atau skema
    database.
-   Jangan mengubah route, nama model atribut, Thymeleaf binding, form
    action, form method, atau mekanisme logout.
-   Jangan mengubah halaman Kasir atau halaman lain.
-   Jangan mengganti data asli dengan mock/fake data.
-   Jangan menghapus fitur dashboard yang sudah ada.
-   Pertahankan ringkasan penjualan, jumlah transaksi, produk aktif,
    kategori, stok menipis, ringkasan bulanan, transaksi terbaru, dan
    peringatan stok yang saat ini sudah tampil.
-   Prioritaskan perubahan pada:
    -   `src/main/resources/templates/admin/dashboard.html`
    -   `src/main/resources/static/css/admin-dashboard.css`
-   Ubah file lain hanya jika benar-benar diperlukan untuk aset visual;
    jelaskan alasannya sebelum melakukan perubahan backend.

## 4. Arah Desain Dashboard

### 4.1 Kanvas dan komposisi

-   Gunakan background utama ivory hangat, misalnya `#F7F2E8`,
    `#F8F4EB`, atau warna sejenis.
-   Buat area konten tetap terang dan kontras; jangan menaruh tekstur
    ramai tepat di belakang teks penting.
-   Tambahkan elemen collage dekoratif secara terukur, misalnya pada
    area header, sudut konten, atau panel sambutan.
-   Gunakan komposisi asimetris dengan ruang kosong yang disengaja,
    bukan ruang kosong besar yang tampak seperti layout rusak.
-   Hindari membuat seluruh dashboard terlihat seperti tumpukan kartu
    generik.
-   Pertahankan sidebar yang jelas dan mudah dipakai, tetapi selaraskan
    warnanya dengan tema kertas dan navy.
-   Dashboard harus tetap tampak seperti aplikasi kasir profesional,
    bukan halaman scrapbook pribadi.

### 4.2 Elemen scrapbook

Gunakan CSS dan aset lokal yang tersedia untuk menciptakan: - Panel
kertas krem/ivory dengan tepi halus. - Lapisan kertas kraft/tan yang
tampak sedikit bergeser. - Potongan kertas robek atau tepi tidak rata
secara halus; gunakan `clip-path` atau pseudo-elements bila sesuai dan
tidak merusak aksesibilitas. - Washi tape bermotif grid/check kecil,
dibuat dengan CSS bila memungkinkan. - Garis botanical sederhana
menggunakan SVG inline atau aset lokal yang sudah ada. - Aksen wave
biru/navy dengan garis emas tipis. - Label kecil seperti potongan label
arsip atau tag kertas. - Tekstur noise/paper sangat halus menggunakan
aset lokal yang sudah tersedia atau CSS ringan.

Jangan menggunakan semua dekorasi sekaligus pada setiap komponen.
Terapkan pada 1--2 area utama saja, lalu biarkan data dan tabel tetap
bersih.

### 4.3 Palet warna

Gunakan palet konsisten seperti: - Ivory / paper: `#F7F2E8` - Warm
white: `#FFFCF6` - Kraft: `#C7A77B` atau `#B99568` - Deep navy:
`#244766` - Ocean blue: `#477FA3` - Muted blue: `#DCE8EE` - Soft gold:
`#C7A45D` - Text: `#26333A` - Muted text: `#6D7778` - Border:
`#E5DCCE` - Stock warning: warna terracotta/merah lembut yang tetap
mudah dibaca, misalnya `#B84E43`

Jangan memakai neon, warna hijau sebagai aksen utama, pink mencolok,
atau kontras hitam tebal.

### 4.4 Tipografi

-   Gunakan font stack sistem yang aman dan tidak membutuhkan internet.
-   Pilih hirarki tipografi yang jelas: judul kuat, subjudul lembut,
    label kecil dengan letter-spacing secukupnya.
-   Jangan membuat seluruh teks uppercase.
-   Angka KPI harus menonjol dan mudah dibaca.
-   Invoice dan nilai rupiah harus tetap jelas; jangan gunakan font
    dekoratif untuk data penting.
-   Jangan menambahkan font eksternal atau CDN.

### 4.5 Header sambutan

Buat area sambutan yang terasa seperti cover scrapbook editorial: -
Judul sambutan tetap dinamis dari data yang ada. - Deskripsi singkat
tetap tersedia. - Sisipkan lapisan kertas/ilustrasi kecil di satu sisi,
bukan memenuhi seluruh header. - Role badge dan tanggal harus tetap
terbaca. - Pastikan ornamen tidak menutupi judul, tanggal, atau
interaksi.

### 4.6 KPI cards

Pertahankan semua nilai dan binding yang sudah ada: - Penjualan hari
ini - Transaksi hari ini - Produk aktif - Stok menipis

Desain: - Kartu dapat terlihat seperti label arsip/kertas yang ditumpuk
secara subtil. - Gunakan aksen kecil berupa icon chip, garis navy, label
kertas, atau corner tape. - Hindari border hitam tebal dan bayangan
keras. - Stok menipis menggunakan aksen terracotta yang lembut, bukan
seluruh kartu merah menyala. - Jangan menambah persentase atau tren jika
tidak ada data valid dari backend.

### 4.7 Ringkasan bulanan

-   Pertahankan bulan, total penjualan, dan jumlah transaksi yang
    sekarang ditampilkan.
-   Tampilkan sebagai panel editorial yang ringkas dengan pemisah halus.
-   Bisa menggunakan label tanggal seperti tag kertas kecil.
-   Jangan mengubah cara menghitung nilai.

### 4.8 Tabel transaksi dan stok

Data table adalah area fungsional; dekorasinya harus minimal. - Header
tabel tetap bersih dan memiliki kontras cukup. - Gunakan garis pemisah
tipis, padding konsisten, dan hover lembut. - Hindari tekstur ramai di
belakang sel tabel. - Pastikan kolom invoice, kasir, total, status, dan
waktu terbaca. - Jangan membuat horizontal scrollbar yang tidak
diperlukan pada viewport desktop 1366px. - Bila nomor invoice terlalu
panjang, gunakan ellipsis hanya secara visual dan pertahankan nilai
lengkap pada data/atribut `title`. - Tabel stok harus tetap menunjukkan
produk, varian, stok, dan batas minimum. - Link ke halaman transaksi dan
stok tetap berfungsi. - Empty state harus tetap ada jika data kosong.

### 4.9 Sidebar

-   Pertahankan semua menu, urutan, route, dan active state.
-   Gunakan warna warm paper/ivory dan navy.
-   Ikon harus berupa aset yang sudah tersedia, SVG inline yang valid,
    atau ikon library yang memang sudah terpasang.
-   Jangan gunakan glyph Unicode yang muncul sebagai kotak.
-   Brand AMANA POS harus tetap mudah dibaca.
-   Logout tetap memakai form POST yang sudah ada; jangan menggantinya
    dengan tautan GET.

## 5. Layout dan Respons Visual

-   Target utama: desktop 1366×768 dan 1920×1080.
-   Gunakan ruang kosong dengan sengaja. Jangan menambahkan dekorasi
    hanya untuk mengisi ruang.
-   Isi konten harus memiliki ritme vertikal yang seimbang.
-   Pada layar 1366px, jangan sampai tabel atau kartu saling bertumpuk
    atau terpotong.
-   Jika dekorasi kolase memakai `position: absolute`, pastikan tidak
    menutupi elemen interaktif dan parent memiliki ukuran yang benar.
-   Gunakan `pointer-events: none` untuk dekorasi yang tidak interaktif.
-   Pastikan `z-index` tidak menghalangi tombol, menu, tabel, atau form.
-   Tambahkan fallback background jika aset tekstur tidak tersedia.

## 6. Aset dan Dependensi

-   Periksa aset lokal dan library ikon yang sudah tersedia sebelum
    membuat sesuatu yang baru.
-   Jangan mengunduh gambar dari internet, jangan menambah CDN, dan
    jangan menambah dependency.
-   Utamakan CSS, pseudo-elements, dan SVG inline yang ringan.
-   Jangan gunakan gambar referensi sebagai background penuh yang
    membuat teks sulit dibaca.
-   Bila menggunakan tekstur lokal, pastikan path aset benar dan file
    memang ada.
-   Jangan membuat klaim bahwa tampilan sudah diverifikasi di browser
    jika hanya menjalankan compile.

## 7. Aksesibilitas dan Animasi

-   Jaga kontras teks dan background.
-   Semua kontrol interaktif harus memiliki focus-visible yang jelas.
-   Dekorasi tidak boleh mengganggu screen reader; tandai sebagai
    dekoratif bila perlu.
-   Gunakan animasi kecil saja (misalnya opacity atau transform ringan),
    tidak wajib.
-   Hormati `prefers-reduced-motion: reduce`.
-   Jangan menambahkan JavaScript hanya untuk animasi dekoratif.

## 8. Workflow yang Wajib Diikuti OpenCode

### Tahap 1 --- Inspect

1.  Baca dashboard HTML dan CSS saat ini secara lengkap.
2.  Identifikasi semua Thymeleaf binding, route, dan form.
3.  Periksa aset lokal dan dependency yang tersedia.
4.  Catat bagian yang boleh disentuh dan yang tidak boleh diubah.

### Tahap 2 --- Plan

1.  Susun rencana singkat untuk menerapkan tema scrapbook.
2.  Prioritaskan header/hero sebagai area kolase utama.
3.  Terapkan aksen kecil di KPI dan monthly summary.
4.  Pertahankan tabel sebagai area data yang bersih.
5.  Pastikan layout tidak menjadi terlalu ramai.

### Tahap 3 --- Implement

1.  Edit HTML/CSS seperlunya.
2.  Pertahankan seluruh binding dan route.
3.  Hindari backend changes.
4.  Jangan menghapus atau mengganti fitur yang ada.
5.  Jangan membuat mock data.

### Tahap 4 --- Verify

1.  Periksa diff untuk memastikan hanya file yang diperlukan berubah.
2.  Cari apakah ada Thymeleaf binding, route, atau logout form yang
    tidak sengaja berubah.
3.  Jalankan `.\mvnw.cmd compile`.
4.  Bila memungkinkan, jalankan aplikasi dan periksa halaman di browser
    pada 1366×768 dan 1920×1080.
5.  Jika tidak dapat membuka browser, katakan dengan jujur bahwa
    verifikasi visual manual masih diperlukan.
6.  Laporkan file yang diubah, bagian yang didesain ulang, hasil
    compile, dan batasan yang belum diverifikasi.

## 9. Acceptance Criteria

Pekerjaan dianggap berhasil jika: - Dashboard memiliki karakter
scrapbook/collage yang jelas, mengambil inspirasi dari referensi
pengguna. - Palet ivory, kraft, navy/ocean blue, dan aksen emas terasa
harmonis. - Ada detail paper layering, torn-paper feel, washi tape, atau
botanical/wave accents secara terukur. - Ruang kosong terasa disengaja
dan layout tetap seimbang. - Dashboard masih terasa profesional dan
mudah digunakan untuk operasional toko. - Tidak ada ikon kotak-kotak,
teks yang sulit dibaca, atau dekorasi yang menutupi konten. - Tabel
tidak terpotong pada viewport desktop target. - Seluruh data dan fungsi
dashboard tetap bekerja. - Tidak ada perubahan pada halaman Kasir,
security, database, atau business logic. - Build berhasil.

## 10. Instruksi Akhir untuk OpenCode

Mulai dengan memeriksa implementasi dashboard yang ada, lalu
implementasikan redesign **visual saja** berdasarkan spesifikasi ini.
Jangan merombak ulang backend atau mengganti fitur. Setelah selesai,
berikan ringkasan perubahan dan hasil verifikasi yang benar-benar
dilakukan.
