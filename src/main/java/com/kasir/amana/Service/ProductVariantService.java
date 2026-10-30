
package com.kasir.amana.Service;

import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Model.Product;
import com.kasir.amana.Repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ProductVariantService {

    private final ProductVariantRepository repository;

    public ProductVariantService(ProductVariantRepository repository) {
        this.repository = repository;
    }

    public List<ProductVariant> findAll() {
        return repository.findAll();
    }

    public Optional<ProductVariant> findById(Long id) {
        return repository.findById(id);
    }

    public List<ProductVariant> findByProductId(Long productId) {
        return repository.findByProductId(productId);
    }

    public List<ProductVariant> findByProductIdIn(List<Long> productIds) {
        return productIds.isEmpty() ? List.of() : repository.findByProductIdIn(productIds);
    }

    public List<ProductVariant> findActiveVariants() {
        return repository.findByActiveTrue();
    }

    public ProductVariant save(ProductVariant variant) {
        return repository.save(variant);
    }

    public String generateSku(Product product, String color, String size) {
        String productCode = normalize(product.getCode());
        String colorCode = normalize(color);
        String sizeCode = normalize(size);

        if (colorCode.isBlank()) {
            colorCode = "WARNA";
        }
        if (sizeCode.isBlank()) {
            sizeCode = "SIZE";
        }

        int maxColorLength = 60 - productCode.length() - sizeCode.length() - 2;
        if (maxColorLength < 1) {
            throw new IllegalArgumentException("Kode produk dan ukuran terlalu panjang untuk SKU.");
        }
        if (colorCode.length() > maxColorLength) {
            colorCode = colorCode.substring(0, maxColorLength);
        }

        String baseSku = productCode + "-" + colorCode + "-" + sizeCode;
        String sku = baseSku;
        int suffix = 2;

        while (repository.existsBySku(sku)) {
            String suffixText = "-" + suffix++;
            sku = baseSku.substring(0, Math.min(baseSku.length(), 60 - suffixText.length()))
                    + suffixText;
        }

        return sku;
    }

    private String normalize(String value) {
        return value.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public void delete(ProductVariant variant) {
        repository.delete(variant);
    }
}
