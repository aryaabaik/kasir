package com.kasir.amana;

import com.kasir.amana.Service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTests {

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    void storeProductImageReturnsNullWhenEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("image", "", "image/png", new byte[0]);
        String url = fileStorageService.storeProductImage(emptyFile);
        assertThat(url).isNull();
    }

    @Test
    void storeProductImageStoresValidImageAndReturnsPath() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "sample-shirt.png",
                "image/png",
                "dummy image content".getBytes()
        );

        String url = fileStorageService.storeProductImage(file);
        assertThat(url).isNotNull();
        assertThat(url).startsWith("/uploads/products/");
        assertThat(url).endsWith(".png");
    }

    @Test
    void storeProductImageRejectsInvalidExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "malicious.sh",
                "text/x-sh",
                "echo hello".getBytes()
        );

        assertThatThrownBy(() -> fileStorageService.storeProductImage(file))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void storeProductImageRejectsOversizedFile() {
        byte[] oversized = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "big.jpg",
                "image/jpeg",
                oversized
        );

        assertThatThrownBy(() -> fileStorageService.storeProductImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5 MB");
    }

    @Test
    void deleteProductImageRemovesStoredFileSafely() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test-linen.webp",
                "image/webp",
                "dummy webp content".getBytes()
        );

        String url = fileStorageService.storeProductImage(file);
        assertThat(url).isNotNull();

        fileStorageService.deleteProductImage(url);
    }
}
