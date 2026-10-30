package com.kasir.amana.Service;

import com.kasir.amana.Model.Product;
import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Model.Category;
import com.kasir.amana.Repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantService productVariantService;

    public ProductService(
            ProductRepository productRepository,
            ProductVariantService productVariantService) {
        this.productRepository = productRepository;
        this.productVariantService = productVariantService;
    }
    
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> searchProducts(
        String keyword,
        Long categoryId,
        Boolean active) {

    if (keyword == null || keyword.isBlank()) {
        keyword = "";
    }

    return productRepository.searchProducts(
            keyword,
            categoryId,
            active
    );
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    
    public Product save(Product product) {
        if (product.getCode() == null || product.getCode().isBlank()) {
            product.setCode(generateCode());
        }

        return productRepository.save(product);
    }

    public Product deactivateProduct(Long id) {
    Product product = productRepository.findById(id)
            .orElseThrow(() ->
                    new IllegalArgumentException("Produk tidak ditemukan"));

    product.setActive(false);

    return productRepository.save(product);
    }

    public Product activateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Produk tidak ditemukan"));

        product.setActive(true);

        return productRepository.save(product);
    }

    @Transactional
    public ProductVariant addVariant(
            Long productId,
            String size,
            String color,
            java.math.BigDecimal purchasePrice,
            java.math.BigDecimal sellingPrice,
            Integer stock,
            Integer minimumStock) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Produk tidak ditemukan."));

        if (size == null || size.isBlank()
                || color == null || color.isBlank()) {
            throw new IllegalArgumentException(
                    "Ukuran dan warna wajib diisi.");
        }

        if (purchasePrice == null || purchasePrice.signum() < 0
                || sellingPrice == null || sellingPrice.signum() < 0
                || stock == null || stock < 0
                || minimumStock == null || minimumStock < 0) {
            throw new IllegalArgumentException(
                    "Harga dan stok harus valid.");
        }

        ProductVariant newVariant = new ProductVariant();
        newVariant.setSize(size.trim());
        newVariant.setColor(color.trim());

        List<ProductVariant> existingVariants =
                productVariantService.findByProductId(productId);

        boolean duplicate = existingVariants.stream().anyMatch(existing ->
                existing.getSize().trim().equalsIgnoreCase(size.trim())
                && existing.getColor().trim().equalsIgnoreCase(color.trim()));

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Kombinasi ukuran dan warna tersebut sudah ada.");
        }

        newVariant.setProduct(product);
        newVariant.setSku(productVariantService.generateSku(
                product, color, size));
        newVariant.setPurchasePrice(purchasePrice);
        newVariant.setSellingPrice(sellingPrice);
        newVariant.setStock(stock);
        newVariant.setMinimumStock(minimumStock);
        newVariant.setActive(true);

        return productVariantService.save(newVariant);
    }

    @Transactional
    public void updateWithVariants(
            Long productId,
            String name,
            Category category,
            String description,
            List<ProductVariant> editedVariants) {
        updateWithVariants(productId, name, category, description, null, false, editedVariants);
    }

    @Transactional
    public void updateWithVariants(
            Long productId,
            String name,
            Category category,
            String description,
            String imageUrl,
            boolean removeImage,
            List<ProductVariant> editedVariants) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Produk tidak ditemukan"));

        List<ProductVariant> existingVariants =
                productVariantService.findByProductId(productId);

        Set<Long> existingIds = new HashSet<>();
        for (ProductVariant variant : existingVariants) {
            existingIds.add(variant.getId());
        }

        Set<String> combinations = new HashSet<>();

        for (ProductVariant variant : editedVariants) {
            if (variant.getId() == null
                    || !existingIds.contains(variant.getId())) {
                throw new IllegalArgumentException(
                        "Varian tidak valid untuk produk ini.");
            }

            if (variant.getSize() == null || variant.getColor() == null
                    || variant.getSize().isBlank()
                    || variant.getColor().isBlank()
                    || variant.getStock() == null || variant.getStock() < 0
                    || variant.getMinimumStock() == null
                    || variant.getMinimumStock() < 0
                    || variant.getPurchasePrice() == null
                    || variant.getPurchasePrice().signum() < 0
                    || variant.getSellingPrice() == null
                    || variant.getSellingPrice().signum() < 0) {
                throw new IllegalArgumentException(
                        "Data varian, harga, dan stok harus valid.");
            }

            String combination = variant.getColor().trim().toLowerCase(Locale.ROOT)
                    + "|" + variant.getSize().trim().toLowerCase(Locale.ROOT);

            if (!combinations.add(combination)) {
                throw new IllegalArgumentException(
                        "Kombinasi warna dan ukuran tidak boleh duplikat.");
            }
        }

        if (editedVariants.size() != existingVariants.size()) {
            throw new IllegalArgumentException(
                    "Semua varian yang ada harus disertakan saat edit.");
        }

        product.setName(name.trim());
        product.setCategory(category);
        product.setDescription(description);
        if (removeImage) {
            product.setImageUrl(null);
        } else if (imageUrl != null && !imageUrl.isBlank()) {
            product.setImageUrl(imageUrl);
        }
        productRepository.save(product);

        for (ProductVariant edited : editedVariants) {
            ProductVariant existing = existingVariants.stream()
                    .filter(v -> v.getId().equals(edited.getId()))
                    .findFirst()
                    .orElseThrow();

            existing.setSize(edited.getSize().trim());
            existing.setColor(edited.getColor().trim());
            existing.setPurchasePrice(edited.getPurchasePrice());
            existing.setSellingPrice(edited.getSellingPrice());
            existing.setStock(edited.getStock());
            existing.setMinimumStock(edited.getMinimumStock());

            productVariantService.save(existing);
        }
    }

    @Transactional
    public SaveResult saveWithVariants(Product product, List<ProductVariant> variants) {
        product.setName(product.getName().trim());
        List<Product> matchingProducts = productRepository
                .findByNameIgnoreCaseAndCategoryIdOrderByIdAsc(
                        product.getName(), product.getCategory().getId());

        boolean merged = !matchingProducts.isEmpty();
        Product savedProduct = merged ? matchingProducts.get(0) : save(product);
        if (merged) {
            if (savedProduct.getImageUrl() == null && product.getImageUrl() != null) {
                savedProduct.setImageUrl(product.getImageUrl());
                productRepository.save(savedProduct);
            }
            mergeExistingProducts(savedProduct, matchingProducts.subList(1, matchingProducts.size()));
        }

        List<ProductVariant> existingVariants =
                productVariantService.findByProductId(savedProduct.getId());
        for (ProductVariant variant : variants) {
            boolean duplicate = existingVariants.stream().anyMatch(existing ->
                    sameVariant(existing, variant));
            if (duplicate) {
                throw new IllegalArgumentException(
                        "Varian warna dan ukuran tersebut sudah ada pada produk ini.");
            }

            variant.setProduct(savedProduct);
            variant.setSku(productVariantService.generateSku(
                    savedProduct, variant.getColor(), variant.getSize()));
            existingVariants.add(productVariantService.save(variant));
        }

        return new SaveResult(savedProduct, merged);
    }

    private void mergeExistingProducts(Product canonical, List<Product> duplicates) {
        List<ProductVariant> canonicalVariants =
                productVariantService.findByProductId(canonical.getId());

        for (Product duplicate : duplicates) {
            List<ProductVariant> duplicateVariants =
                    productVariantService.findByProductId(duplicate.getId());
            for (ProductVariant duplicateVariant : duplicateVariants) {
                ProductVariant matchingVariant = canonicalVariants.stream()
                        .filter(existing -> sameVariant(existing, duplicateVariant))
                        .findFirst()
                        .orElse(null);

                if (matchingVariant != null) {
                    if (matchingVariant.getPurchasePrice().compareTo(
                            duplicateVariant.getPurchasePrice()) != 0
                            || matchingVariant.getSellingPrice().compareTo(
                            duplicateVariant.getSellingPrice()) != 0) {
                        throw new IllegalArgumentException(
                                "Produk duplikat memiliki harga berbeda pada varian "
                                        + duplicateVariant.getColor() + " "
                                        + duplicateVariant.getSize()
                                        + "; samakan harga sebelum digabung.");
                    }

                    matchingVariant.setStock(
                            matchingVariant.getStock() + duplicateVariant.getStock());
                    productVariantService.save(matchingVariant);
                    productVariantService.delete(duplicateVariant);
                } else {
                    duplicateVariant.setProduct(canonical);
                    productVariantService.save(duplicateVariant);
                    canonicalVariants.add(duplicateVariant);
                }
            }
            productRepository.delete(duplicate);
        }

        
    }

    private boolean sameVariant(ProductVariant first, ProductVariant second) {
        return first.getColor().trim().equalsIgnoreCase(second.getColor().trim())
                && first.getSize().trim().equalsIgnoreCase(second.getSize().trim());
    }

    public record SaveResult(Product product, boolean merged) {}

    private String generateCode() {
        return previewNextCode();
    }

    public String previewNextCode() {
        long number = productRepository.count() + 1;
        String code = formatCode(number);

        while (productRepository.existsByCode(code)) {
            code = formatCode(++number);
        }

        return code;
    }

    private String formatCode(long number) {
        return String.format(Locale.ROOT, "PRD-%04d", number);
    }


    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public Product deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Produk tidak ditemukan."));

        List<ProductVariant> variants =
                productVariantService.findByProductId(id);

        variants.forEach(productVariantService::delete);
        productRepository.delete(product);
        return product;
    }
}
