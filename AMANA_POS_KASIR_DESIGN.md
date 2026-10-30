# AMANA POS — Spesifikasi Desain Antarmuka Kasir (Point of Sale)

Dokumen ini merupakan panduan dan spesifikasi arsitektur antarmuka resmi untuk pengembangan tampilan modul Kasir pada aplikasi **AMANA POS**. Spesifikasi ini disusun berdasarkan audit menyeluruh terhadap kode sumber Spring Boot, Thymeleaf, Spring Security, dan PostgreSQL yang telah ada, untuk memastikan bahwa implementasi visual berjalan presisi, fungsional, dan bebas dari rekaan data atau "AI slop".

---

## 1. Audit Project Sebelum Implementasi

Sebelum menulis atau mengubah sebaris kode CSS atau HTML kasir, implementer wajib memahami batasan teknis dan arsitektur backend yang sedang aktif.

### 1.1 Inventaris File & Modul Kasir

| Komponen | Lokasi File | Status & Peran |
| :--- | :--- | :--- |
| **Controller Kasir** | `com.kasir.amana.Controller.Kasir.KasirTransactionController` | Mengelola alur transaksi kasir, checkout POST, riwayat transaksi, dan tampilan struk. |
| **Controller Dashboard** | `com.kasir.amana.Controller.DashboardController` (`/kasir/dashboard`) | Mengelola landing page kasir pasca-login. |
| **Service Utama** | `com.kasir.amana.Service.CheckoutService` | Logika pengurangan stok atomik, pembuatan `Transaction`, `TransactionItem`, dan `Payment`. |
| **Service Varian** | `com.kasir.amana.Service.ProductVariantService` | Menyediakan varian aktif (`findActiveVariants()`) untuk katalog kasir. |
| **Entitas Terkait** | `com.kasir.amana.Model.` (`Product`, `ProductVariant`, `Transaction`, `TransactionItem`, `Payment`, `User`) | Struktur tabel basis data PostgreSQL `amana_db`. |
| **Repository** | `com.kasir.amana.Repository.` (`ProductVariantRepository`, `TransactionRepository`, `TransactionItemRepository`, `PaymentRepository`, `UserRepository`) | Kueri data persistensi. |
| **Konfigurasi Keamanan** | `com.kasir.amana.Config.SecurityConfig` | Mengatur hak akses role `ROLE_CASHIER` pada rute `/kasir/**`, proteksi CSRF, dan handler login/logout. |
| **Template Kasir** | `src/main/resources/templates/kasir/` | `dashboard.html`, `transactions/checkout.html`, `transactions/history.html`, `transactions/receipt.html`. |
| **Stylesheet Kasir** | `src/main/resources/static/css/` | `kasir-dashboard.css`, `transactions.css`, `transaction-history.css`. |

### 1.2 Aturan Keras Backend & Integritas Data
- **Dilarang keras mengarang:** Nama field, endpoint HTTP, parameter request, model attribute, method repository, atau struktur entity tidak boleh diubah atau ditambah tanpa persetujuan.
- **Rute yang Sudah Beroperasi:**
  - `GET /kasir/dashboard` : Dashboard kasir.
  - `GET /kasir/transactions` : Halaman kasir / Point of Sale utama (checkout page).
  - `POST /kasir/transactions/checkout` : Proses transaksi (menerima `variantId`, `quantity`, dan `amountReceived`).
  - `GET /kasir/transactions/history` : Riwayat transaksi kasir yang sedang login.
  - `GET /kasir/transactions/receipt/{invoiceNumber}` : Halaman cetak/struk nota transaksi.
  - `POST /logout` : Endpoint logout Spring Security (memerlukan token CSRF).
- **Atribut Model Nyata (Existing):**
  - Pada `GET /kasir/transactions`: model mengirimkan `variants` (koleksi `ProductVariant`), `checkoutUrl` (`/kasir/transactions/checkout`), `dashboardUrl` (`/kasir/dashboard`).
  - Pada `GET /kasir/transactions/history`: model mengirimkan `transactions` (List `Transaction`), `dashboardUrl` (`/kasir/dashboard`).
  - Pada `GET /kasir/transactions/receipt/{invoiceNumber}`: model mengirimkan `transaction` (`Transaction`), `items` (List `TransactionItem`).

---

## 2. Layout Utama Kasir (POS Two-Panel Workspace)

Tampilan kasir harus didesain sebagai **Point of Sale operasional retail pakaian modern**, bukan sebagai dashboard admin atau tabel pendataan pasif.

```
+---------------------------------------------------------------------------------------------------+
|  SIDEBAR (Charcoal-Teal)  |                   MAIN WORKSPACE (Light Neutral)                      |
|                           +-----------------------------------------------------------------------+
|  [Brand AMANA POS]        | TOPBAR: Info Toko / Shift Kasir / Waktu Realtime / Status Koneksi     |
|                           +------------------------------------+----------------------------------+
|  [NAVIGASI]               | AREA KATALOG PRODUK (KIRI / 60-65%)| KERANJANG TRANSAKSI (KANAN / 35%)|
|  - Transaksi Baru (POS)   | - Search Bar (Nama/Kode/SKU)       | - Daftar Item Terpilih           |
|  - Riwayat Penjualan      | - Filter Kategori & Chip Kategori  |   (Nama, Varian, Qty, Subtotal)  |
|  - Dashboard Kasir        | - Grid Kartu Produk Retail         | - Ringkasan Perhitungan:         |
|                           |   (Thumbnail, Kode, Varian, Stok)  |   * Total Qty & Subtotal         |
|  [FOOTER]                 | - Quick Selector Ukuran/Warna      | - Input "Uang Diterima"          |
|  - Profil Kasir Login     |                                    | - Kalkulasi Kembalian Otomatis   |
|  - Tombol Logout (POST)   |                                    | - Tombol [Proses Transaksi]      |
+---------------------------------------------------------------------------------------------------+
```

### 2.1 Pembagian Zona Visual
1. **Sidebar Navigasi (Kiri):** Lebar tetap `220px` – `240px`. Berlatar gelap (`charcoal-teal`), berfungsi sebagai jangkar aplikasi yang tenang dan stabil.
2. **Top Utility Bar:** Tinggi `52px` – `56px`. Menampilkan judul modul aktif, nama toko ("Atelier AMANA"), identitas kasir yang bertugas, dan tanggal/jam shift.
3. **Katalog Produk (Workspace Kiri - Flex 1):** Area pemilihan produk pakaian dengan pencarian instan, filter kategori, kartu produk yang informatif, dan selector ukuran/warna yang jelas.
4. **Keranjang Kasir (Workspace Kanan - Fixed Width `380px` – `420px`):** Panel transaksi dengan elevasi bersih, menampilkan item yang dibeli, pengubah kuantitas (+/-), rincian harga, kalkulasi kembalian, dan tombol eksekusi transaksi yang mencolok.

---

## 3. Sidebar Kasir

Sidebar kasir berorientasi pada kemudahan navigasi saat pergantian shift dan operasional kasir.

### 3.1 Karakteristik Visual & Warna
- **Background:** Deep Charcoal-Teal (`#131F24`).
- **Garis Pembatas Kanan:** `1px solid #1C2D34`.
- **Text Primary:** `#E6EDF0`.
- **Text Secondary / Muted:** `#7F939B`.
- **Active Menu Background:** `#1B6A75` (Deep Teal Accent) dengan teks putih `#FFFFFF` dan indikator garis aksen vertikal di tepi kanan/kiri (`#38B2AC`).
- **Hover Menu Background:** `#18282E`.

### 3.2 Elemen-Elemen Sidebar
1. **Brand Header:**
   - Logo teks: **AMANA** dalam bobot tebal dengan badge kecil `POS KASIR`.
   - Subtitle: *Retail Storefront & Checkout*.
2. **Menu Kasir (Strict Role Cashier):**
   - **Kasir / POS (`/kasir/transactions`):** Ikon register/keranjang belanja. *Halaman kerja utama.*
   - **Riwayat Transaksi (`/kasir/transactions/history`):** Ikon berkas/jam transaksi.
   - **Ringkasan Shift (`/kasir/dashboard`):** Ikon meter/ikhtisar kasir.
3. **Informasi Pengguna (Bottom Section):**
   - Menampilkan nama kasir yang sedang terotentikasi via `sec:authentication="name"` atau Spring Security principal.
   - Indikator status shift aktif (dot hijau lembut `#2ECC71`).
4. **Logout Form:**
   - Form semantic HTML dengan method `POST` mengarah ke `th:action="@{/logout}"`.
   - Wajib menyertakan input CSRF tersembunyi jika tidak otomatis diinjeksi Thymeleaf.
   - Tombol logout dengan label jelas "Keluar Shift" atau "Logout", menggunakan warna hover merah teredam (`#A93226`).

---

## 4. Katalog Produk Kasir

Area katalog dirancang untuk kecepatan kasir menemukan produk pakaian tanpa perlu membuka banyak dialog.

### 4.1 Filter & Pencarian Cepat
- **Input Pencarian Global:**
  - Posisi di bagian atas katalog.
  - Placeholder: *"Cari pakaian berdasarkan nama, kode PRD, atau SKU..."*
  - Dilengkapi ikon kaca pembesar dan tombol reset instan (tombol 'x').
  - Pencarian harus bekerja secara instan pada item yang ter-render di halaman.
- **Filter Kategori (Chips/Pills):**
  - Tombol chip horizontal: `Semua`, `Kemeja`, `Kaos`, `Celana`, `Gamis/Dress`, dll.
  - Chip aktif menggunakan deep teal `#1B6A75` dengan teks putih; chip tidak aktif menggunakan background putih dengan border halus `#D4DCE0`.

### 4.2 Kartu Produk Retail (Apparel Product Tile)
Bukan card dashboard admin, melainkan kartu produk POS toko pakaian yang fungsional:
- **Dimensi & Grid:** Grid responsif `repeat(auto-fill, minmax(180px, 1fr))`, gap `12px`.
- **Thumbnail Pakaian:**
  - Rasio 1:1 atau 4:5 (tinggi sekitar 120px–140px).
  - Menggunakan `object-fit: cover` untuk gambar nyata yang diunggah (`variant.product.imageUrl`).
  - Jika tidak memiliki gambar, tampilkan ilustrasi vektor pakaian monokrom minimalis.
- **Informasi Produk:**
  - Kode Produk / SKU (huruf monospace, misalnya `PRD-0001`).
  - Nama Produk (maksimal 2 baris, truncate dengan rapi jika terlalu panjang).
  - Harga Jual Satuan (format rupiah tebal, misal `Rp 125.000`).
  - Label Varian (Warna & Ukuran, misal `Navy · L`).
  - Indikator Stok:
    - Stok aman: `Stok: 14` (warna teks netral `#5A6B73`).
    - Stok menipis (≤ minimumStock): `Sisa: 2` (warna oranye teredam `#D97706`).
    - Stok habis (0): Badge `Habis` (warna abu-abu/merah redup `#94A3B8`), kartu dinonaktifkan (`opacity: 0.6; pointer-events: none;`).

---

## 5. Keranjang Transaksi (Checkout Ledger Panel)

Panel kanan adalah area fokus utama saat melayani pelanggan yang berdiri di meja kasir.

### 5.1 Struktur & Visual Keranjang
- **Background:** Putih bersih `#FFFFFF`.
- **Border Kiri:** `1px solid #DCE3E6`.
- **Header Keranjang:**
  - Judul: "Keranjang Belanja".
  - Indikator jumlah item: `Badge (X item)`.
  - Tombol `Kosongkan` (opsional) untuk mereset seluruh kuantitas ke 0.
- **Daftar Item (Scrollable Container):**
  - Item list dibatasi pada viewport kasir dengan `overflow-y: auto`.
  - Setiap baris item menampilkan:
    - Nama produk pakaian.
    - Tag varian yang kontras: `[Hitam / M]`.
    - Harga satuan (misal `@ Rp 75.000`).
    - Kontrol Kuantitas Stepper: tombol `[-]`, input kuantitas numerik, tombol `[+]`.
    - Subtotal baris item (`Qty × Harga`).
    - Tombol hapus item (ikon tempat sampah atau tombol `✕`).

### 5.2 Ringkasan Pembayaran & Eksekusi
- **Zona Rekapitulasi Harga:**
  - `Subtotal Belanja`: Rp XXX.XXX
  - `Diskon`: Rp 0 (sesuai backend saat ini belum ada diskon dinamis).
  - **TOTAL BAYAR:** Tipografi besar, tebal, dengan font tabular (misal `24px` bold `#131F24`).
- **Input Pembayaran Tunai (Cash Received):**
  - Label: *"Uang Tunai Diterima (Rp)"*.
  - Input field besar dengan format angka yang jelas (`font-size: 18px`).
  - Tombol pecahan cepat (Quick Cash Buttons): `Uang Pas`, `50.000`, `100.000`, `200.000` untuk mempercepat kasir tanpa mengetik manual.
- **Kalkulasi Kembalian (Live Change Preview):**
  - Menampilkan selisih: `Kembalian: Rp XX.XXX`.
  - Jika uang kurang: Teks berwarna merah teredam *"Uang pembayaran masih kurang Rp XX.XXX"*.
  - Jika uang cukup/lebih: Teks berwarna hijau tua *"Kembalian: Rp XX.XXX"*.
- **Tombol Proses Transaksi:**
  - Tombol primer berukuran penuh (*full width*), tinggi `46px`–`48px`.
  - Warna: Deep Teal `#1B6A75` (Hover: `#14555E`).
  - Teks: **BAYAR & SELESAIKAN (F9 / Enter)**.
  - State disabled jika keranjang kosong atau uang diterima kurang dari total.

---

## 6. Pemilihan Varian & Validasi Stok

Aplikasi AMANA POS menyimpan produk dalam bentuk kombinasi induk `Product` dan turunan `ProductVariant` (ukuran & warna).

### 6.1 Aturan Bisnis Stok & Varian
1. **Penyajian Data:**
   Controller `KasirTransactionController.checkoutPage` mengirimkan `List<ProductVariant>` yang aktif dan dimiliki oleh produk yang aktif. Setiap baris mewakili satu SKU unik dengan stoknya masing-masing.
2. **Kesesuaian Form HTML:**
   Form checkout mengirimkan:
   - `variantId` : ID dari `ProductVariant`.
   - `quantity` : Jumlah yang dibeli.
   - `amountReceived` : Uang yang diserahkan pelanggan.
3. **Validasi Kuantitas di Sisi Klien:**
   - Input kuantitas memiliki atribut `min="0"` dan `th:attr="max=${variant.stock}"`.
   - Tombol stepper `[+]` tidak boleh menambah angka melebihi batas stok yang tersedia.
   - Jika stok adalah 0, input dinonaktifkan (`disabled`) dan kartu produk tidak dapat diklik.
4. **Validasi Keras Backend:**
   `CheckoutService.checkout` memverifikasi ulang stok di basis data. Jika kasir mencoba memanipulasi form dan kuantitas melebihi stok, backend akan melempar `IllegalArgumentException("Stok tidak mencukupi...")` dan mengembalikan flash attribute `errorMessage`.

---

## 7. Alur Pembayaran & Struk

Alur transaksi harus sepenuhnya sinkron dengan kapabilitas backend yang tersedia pada `CheckoutService` dan `KasirTransactionController`.

### 7.1 Eksekusi Checkout
1. Kasir memilih varian dan menentukan jumlah pesanan.
2. Kasir menginput jumlah uang yang diterima dari pembeli pada field `name="amountReceived"`.
3. Kasir menekan tombol submit `POST /kasir/transactions/checkout`.
4. Backend memvalidasi ketersediaan stok, memotong stok pada tabel `product_variants`, membuat entitas `Transaction` (status `COMPLETED`), membuat rincian `TransactionItem`, dan mencatat `Payment` (metode `CASH`).
5. Controller mengarahkan kasir ke halaman struk resmi:
   `redirect:/kasir/transactions/receipt/{invoiceNumber}`.

### 7.2 Halaman Struk Nota (`receipt.html`)
- Menggantikan tampilan lama yang gelap dengan tema struk kertas kasir profesional (*clean white thermal receipt print layout*).
- Menampilkan nomor invoice (misal: `INV-20261023153000-A1B2C3`), tanggal, nama kasir, daftar item (produk, varian, qty, harga, subtotal), total belanja, uang diterima, dan kembalian.
- Tombol aksi:
  - **Cetak Struk (`window.print()`):** Diformat dengan CSS `@media print` agar pas pada printer thermal kasir (lebar 58mm / 80mm).
  - **Transaksi Baru:** Tautan kembali ke `/kasir/transactions` untuk melayani pelanggan berikutnya.

---

## 8. Riwayat Transaksi Kasir (`history.html`)

Halaman riwayat transaksi kasir digunakan untuk pengecekan rekonsiliasi kas dan cetak ulang nota pelanggan yang hilang.

### 8.1 Batasan Hak Akses Data
- Sesuai `checkoutService.findCashierTransactions(authentication.getName())`, kasir **hanya dapat melihat transaksi yang diproses oleh akun kasir itu sendiri**.
- Kasir tidak dapat melihat transaksi kasir lain atau laporan laba kotor toko (itu hak akses admin).

### 8.2 Desain Tabel Riwayat
- Menggantikan style lama (background gelap `#101713` dan neon `#b7f36b`) dengan antarmuka tabel kasir yang terang dan bersih.
- Kolom tabel:
  1. `No.` : Nomor urut.
  2. `No. Invoice` : Kode faktur berhuruf monospace (misal: `INV-20261023...`).
  3. `Waktu Transaksi` : Format tanggal & jam Indonesia (`dd-MM-yyyy HH:mm`).
  4. `Total Belanja` : Format mata uang tabular (`Rp XXX.XXX`).
  5. `Status` : Badge status (`COMPLETED` dengan warna hijau teredam).
  6. `Aksi` : Tombol tautan kompak "Lihat Struk" menuju `/kasir/transactions/receipt/{invoice}`.
- Kotak pencarian nomor invoice lokal untuk memfilter riwayat hari ini dengan cepat.

---

## 9. Desain Responsif & Target Resolusi Desktop

Aplikasi AMANA POS adalah aplikasi desktop kasir toko fisik. Prioritas antarmuka ditujukan pada rasio layar berikut:

| Resolusi | Target Tampilan | Pengaturan Layout |
| :--- | :--- | :--- |
| **1366 × 768** (Laptop Toko Standar) | **Prioritas Utama** | Seluruh workspace (katalog dan keranjang) harus tampil dalam satu layar tanpa scrollbar pada level halaman utama (`body { overflow: hidden; }`). Scrollbar hanya berada di dalam kontainer daftar katalog dan kontainer daftar keranjang. |
| **1920 × 1080** (Monitor Full HD) | **Prioritas Utama** | Grid kartu produk membesar secara proporsional (`auto-fill`), keranjang tetap pada batas maksimal yang nyaman (`420px`). |

### Aturan Bebas Overflow
- Dilarang keras memicu horizontal scrollbar pada window browser.
- Gunakan CSS Grid atau Flexbox yang terkunci tinggi viewpornya (`height: 100vh; overflow: hidden;`).

---

## 10. Design Tokens (Palet Warna, Tipografi & Status)

Implementasi stylesheet kasir harus menggunakan CSS Custom Properties berikut:

```css
:root {
  /* ─── Kanvas & Workspace Kasir ─── */
  --pos-bg-body:          #EEF2F4;   /* Abu-abu terang sejuk untuk kanvas kasir */
  --pos-bg-surface:       #FFFFFF;   /* Putih bersih untuk panel dan kartu */
  --pos-bg-surface-alt:   #F5F8F9;   /* Abu-abu kebiruan lembut untuk table header */
  --pos-bg-hover:         #E8EFF2;   /* Hover interaktif */

  /* ─── Sidebar Kasir (Charcoal-Teal Gelap) ─── */
  --pos-sidebar-bg:       #131F24;   /* Charcoal-teal pekat */
  --pos-sidebar-surface:  #18282E;   /* Elevated sidebar element */
  --pos-sidebar-border:   #1E3138;   /* Garis pemisah halus gelap */
  --pos-sidebar-text:     #E6EDF0;   /* Teks utama sidebar */
  --pos-sidebar-muted:    #7F939B;   /* Teks sekunder sidebar */

  /* ─── Aksen Utama Operasional (Deep Teal) ─── */
  --pos-primary:          #1B6A75;   /* Deep Teal operasional */
  --pos-primary-hover:    #14555E;   /* Deep Teal saat ditekan/hover */
  --pos-primary-subtle:   #E4F2F4;   /* Latar tint teal lembut */
  --pos-primary-border:   #B8DEE3;   /* Border aksen teal */

  /* ─── Tipografi & Tinta Konten ─── */
  --pos-text-main:        #1A262C;   /* Tinta hitam charcoal legibel */
  --pos-text-secondary:   #546670;   /* Slate netral untuk label */
  --pos-text-muted:       #8295A0;   /* Abu-abu untuk metadata & SKU */

  /* ─── Garis Pembatas (Subtle Borders) ─── */
  --pos-border-subtle:    #DCE3E6;   /* Border panel dan kartu */
  --pos-border-focus:     #1B6A75;   /* Outline saat elemen aktif */

  /* ─── Status & Feedback (Restrained) ─── */
  --pos-success-bg:       #EAF5EE;   /* Hijau sage lembut */
  --pos-success-text:     #1E6B37;   /* Hijau tua kontras tinggi */
  --pos-success-border:   #C5E6CE;

  --pos-danger-bg:        #FDF2F2;   /* Merah teredam lembut */
  --pos-danger-text:      #A93226;   /* Merah gelap peringatan */
  --pos-danger-border:    #F5C6CB;

  --pos-warning-bg:       #FEF9E7;   /* Kuning amber redup */
  --pos-warning-text:     #9A7D0A;
  --pos-warning-border:   #F9E79F;

  /* ─── Tipografi & Numerik ─── */
  --pos-font-sans:        "Segoe UI", -apple-system, BlinkMacSystemFont, "Inter", Roboto, sans-serif;
  --pos-font-mono:        "Cascadia Code", "Consolas", "Roboto Mono", monospace;

  /* ─── Radius & Bayangan Halus ─── */
  --pos-radius-sm:        4px;
  --pos-radius-md:        8px;
  --pos-radius-lg:        12px;
  --pos-shadow-sm:        0 1px 3px rgba(19, 31, 36, 0.05);
  --pos-shadow-card:      0 2px 6px rgba(19, 31, 36, 0.06);

  /* ─── Transisi ─── */
  --pos-transition-fast:  150ms ease;
}
```

---

## 11. Aturan Anti-"AI Slop"

Implementasi visual kasir dilarang memuat ciri-ciri desain generik buatan AI yang tidak mencerminkan kebutuhan kasir toko retail nyata:

1. **Dilarang menambahkan kartu metrik admin:** Tidak boleh ada kartu "Total Pendapatan Toko", "Grafik Penjualan Bulanan", atau "Kinerja Kasir" di tengah alur transaksi kasir.
2. **Dilarang menggunakan ilustrasi dekoratif tak berguna:** Tidak boleh menaruh gambar kartun 3D melayang, ornamen pastel berlebihan, atau hiasan stiker.
3. **Dilarang menggunakan gradient pelangi:** Gunakan warna tegas solid atau subtle tint netral.
4. **Dilarang menaruh tombol tanpa fungsi:** Setiap tombol yang dibuat harus memiliki aksi nyata (misalnya submit form, navigasi rute valid, atau kontrol kuantitas form).
5. **Dilarang membuat data dummy statis:** Semua nama pakaian, ukuran, warna, harga, dan invoice harus berasal dari model Thymeleaf `${...}`.

---

## 12. Batasan Implementasi (Strict Guardrails)

Untuk menjaga stabilitas sistem yang sedang berjalan:

- **Isolasi Halaman Admin:** Tidak boleh mengubah file template di `templates/admin/**` atau file stylesheet `products.css`, `categories.css`, `admin-dashboard.css`.
- **CSS Khusus Kasir:** Seluruh styling kasir harus berada di file stylesheet kasir (`kasir-dashboard.css`, `transactions.css`, `transaction-history.css`).
- **Preservasi Nama Input Form:**
  - `name="variantId"` (List)
  - `name="quantity"` (List)
  - `name="amountReceived"` (Single BigDecimal)
- **Preservasi Rute & Keamanan:** Rute Spring Security `/kasir/**` dan proteksi CSRF pada form POST tidak boleh dirusak atau dinonaktifkan.

---

## 13. Kriteria Penerimaan (Acceptance Criteria)

Implementasi antarmuka kasir dinyatakan selesai dan diterima jika memenuhi seluruh poin berikut:

### Kriteria Visual
- [ ] Sidebar kiri kasir menggunakan tema charcoal-teal gelap (`#131F24`) dengan logo AMANA POS dan menu role kasir yang rapi.
- [ ] Area kerja kasir terbagi secara seimbang antara katalog pakaian dan keranjang belanja.
- [ ] Tidak terjadi overflow/horizontal scrollbar pada window browser pada resolusi 1366×768 dan 1920×1080.
- [ ] Kartu produk menampilkan foto nyata (atau placeholder monokrom bersih jika tanpa gambar), kode, nama pakaian, kombinasi warna/ukuran, harga, dan stok.
- [ ] Keranjang kasir memiliki hierarki visual yang jelas: item belanja, kuantitas stepper, total bayar tebal, input uang diterima, dan preview kembalian.
- [ ] Halaman riwayat transaksi kasir dan halaman struk bersih dari style lama yang gelap/neon hijau, berganti ke tampilan profesional retail kasir.

### Kriteria Fungsional
- [ ] Produk tanpa stok (0) berstatus nonaktif dan tidak dapat ditambahkan ke keranjang.
- [ ] Pengurangan stok dan pencatatan transaksi berhasil dieksekusi oleh backend saat tombol bayar ditekan.
- [ ] Tombol bayar dicegah aktif jika uang yang diterima kurang dari total pembayaran.
- [ ] Sukses checkout langsung mengarahkan ke halaman struk invoice yang dapat dicetak (`window.print()`).
- [ ] Riwayat transaksi menampilkan transaksi milik kasir yang sedang login.
- [ ] Tombol logout POST berhasil mengeluarkan kasir dari sistem Spring Security.
- [ ] Seluruh pengujian backend (`mvnw test`) tetap lulus 100%.

---

## 14. Checklist Pengujian Manual

Lakukan pengujian langkah-demi-langkah berikut di browser setelah implementasi selesai:

- [ ] **Login Kasir:** Masuk dengan kredensial kasir; verifikasi diarahkan otomatis ke `/kasir/dashboard` atau menu transaksi.
- [ ] **Akses Keamanan:** Coba ketik manual URL admin (misal `/admin/dashboard` atau `/products`) di browser kasir; pastikan ditolak/HTTP 403 Forbidden.
- [ ] **Pencarian Produk:** Ketikkan nama produk atau kode produk pada search bar; pastikan katalog memfilter produk secara akurat.
- [ ] **Validasi Kuantitas & Stok:**
  - Tambah kuantitas produk hingga menyamai batas stok maksimal; pastikan tidak bisa menambah melebihi stok.
  - Coba kurangi kuantitas hingga 0; pastikan item terhapus dari keranjang.
- [ ] **Perhitungan Transaksi:**
  - Masukkan uang diterima lebih kecil dari total belanja; pastikan muncul peringatan bahwa uang kurang.
  - Masukkan uang pas atau lebih; pastikan kembalian terhitung dengan tepat.
- [ ] **Checkout Transaksi:**
  - Klik tombol "Proses Transaksi" / "Bayar"; pastikan transaksi berhasil dan halaman berpindah ke `/kasir/transactions/receipt/{invoiceNumber}`.
  - Uji tombol cetak struk dan tombol transaksi baru.
- [ ] **Pemeriksaan Riwayat:**
  - Buka `/kasir/transactions/history`; pastikan transaksi yang baru saja diproses tercatat pada baris teratas dengan total dan tanggal yang benar.
- [ ] **Logout Kasir:** Klik tombol "Keluar Shift"; pastikan session berakhir dan pengguna diarahkan ke `/login`.

---

## 15. Workflow Implementasi Berikutnya

Implementasi desain kasir harus mengikuti urutan tahapan terstruktur:

1. **Fase 1 — Audit Konfirmasi:** Periksa kembali file HTML kasir di `src/main/resources/templates/kasir/` dan pastikan mapping Thymeleaf tidak terlewat.
2. **Fase 2 — Penyusunan Stylesheet Kasir:** Buat/perbarui file CSS di `src/main/resources/static/css/` sesuai design tokens charcoal-teal dan layout kasir POS.
3. **Fase 3 — Refactoring Template Kasir:** Perbarui HTML pada `checkout.html`, `history.html`, `receipt.html`, dan `dashboard.html` dengan markup semantic yang presisi.
4. **Fase 4 — Kompilasi & Pengujian Kode:**
   - Jalankan `.\mvnw.cmd compile`
   - Jalankan `.\mvnw.cmd test-compile`
   - Jalankan `.\mvnw.cmd test`
5. **Fase 5 — Verifikasi Diff:** Tinjau perubahan file menggunakan `git status` atau inspeksi diff untuk memastikan tidak ada file admin atau backend yang tersenggol secara tidak sengaja.
6. **Fase 6 — Verifikasi Browser:** Buka browser pada resolusi 1366×768 dan jalankan checklist pengujian manual.
