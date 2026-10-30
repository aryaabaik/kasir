package com.kasir.amana.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".webp");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/pjpeg",
            "image/png",
            "image/x-png",
            "image/webp"
    );
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    private final Path uploadRootPath;

    public FileStorageService(@Value("${amana.upload.dir:uploads/products}") String uploadDir) {
        this.uploadRootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRootPath);
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membuat direktori upload: " + this.uploadRootPath, e);
        }
    }

    /**
     * Menyimpan file gambar produk yang diunggah secara aman.
     * Mengembalikan URL publik produk (misal: /uploads/products/uuid.webp) atau null jika file kosong.
     */
    public String storeProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Ukuran gambar melebihi batas maksimal 5 MB.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String cleanMime = contentType.toLowerCase(Locale.ROOT).trim();
            if (!ALLOWED_MIME_TYPES.contains(cleanMime)) {
                throw new IllegalArgumentException("Format gambar tidak didukung (" + contentType + "). Hanya menerima JPEG, PNG, dan WebP.");
            }
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Ekstensi file '" + extension + "' tidak didukung. Gunakan .jpg, .jpeg, .png, atau .webp.");
        }

        // Generate safe unique filename to avoid path traversal and name collisions
        String safeFilename = UUID.randomUUID().toString().replace("-", "") + extension;
        Path targetLocation = this.uploadRootPath.resolve(safeFilename).normalize();

        // Path traversal defense
        if (!targetLocation.startsWith(this.uploadRootPath)) {
            throw new SecurityException("Akses direktori tidak sah terdeteksi.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/products/" + safeFilename;
        } catch (IOException e) {
            throw new IllegalStateException("Gagal menyimpan file gambar produk.", e);
        }
    }

    /**
     * Menghapus file gambar jika sudah tidak digunakan atau saat penggantian file.
     */
    public void deleteProductImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/products/")) {
            return;
        }

        String filename = imageUrl.substring("/uploads/products/".length());
        if (filename.isBlank() || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            return;
        }

        try {
            Path filePath = this.uploadRootPath.resolve(filename).normalize();
            if (filePath.startsWith(this.uploadRootPath)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ignored) {
            // Logged/ignored if already removed or locked
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT).trim();
    }
}
