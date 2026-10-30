package com.kasir.amana;

import com.kasir.amana.Model.Category;
import com.kasir.amana.Model.Product;
import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Repository.ProductRepository;
import com.kasir.amana.Service.ProductService;
import com.kasir.amana.Service.ProductVariantService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTests {

        @Test
        void searchProductsUsesEmptyKeywordWhenKeywordIsMissing() {
                ProductRepository productRepository = mock(ProductRepository.class);
                ProductVariantService variantService = mock(ProductVariantService.class);
                ProductService productService = productService(productRepository, variantService);

                when(productRepository.searchProducts("", null, null)).thenReturn(List.of());

                List<Product> products = productService.searchProducts(null, null, null);

                assertThat(products).isEmpty();
                verify(productRepository).searchProducts("", null, null);
        }

    @Test
    void mergesSameNameProductsAndAddsNewVariantToOriginalProduct() {
        ProductRepository productRepository = mock(ProductRepository.class);
        ProductVariantService variantService = mock(ProductVariantService.class);
        ProductService productService = productService(productRepository, variantService);

        Category category = new Category();
        category.setId(1L);
        Product original = product("Oversized Tee", "PRD-0001", 1L, category);
        Product duplicate = product("oversized tee", "PRD-0002", 2L, category);

        ProductVariant originalVariant = variant(original, "Hitam", "L", 5);
        ProductVariant duplicateVariant = variant(duplicate, "Putih", "XL", 3);
        ProductVariant duplicateStock = variant(duplicate, "Hitam", "L", 2);
        List<ProductVariant> originalVariants = new ArrayList<>(List.of(originalVariant));
        List<ProductVariant> duplicateVariants =
                new ArrayList<>(List.of(duplicateVariant, duplicateStock));
        ProductVariant requestedVariant = variant(null, "Biru", "M", 4);

        when(productRepository.findByNameIgnoreCaseAndCategoryIdOrderByIdAsc(
                "Oversized Tee", 1L)).thenReturn(List.of(original, duplicate));
        when(variantService.findByProductId(1L)).thenReturn(originalVariants);
        when(variantService.findByProductId(2L)).thenReturn(duplicateVariants);
        when(variantService.generateSku(original, "Biru", "M"))
                .thenReturn("PRD-0001-BIRU-M");
        when(variantService.save(any(ProductVariant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductService.SaveResult result = productService.saveWithVariants(
                product("  Oversized Tee ", null, null, category),
                List.of(requestedVariant));

        assertThat(result.merged()).isTrue();
        assertThat(result.product()).isSameAs(original);
        assertThat(originalVariants).hasSize(3);
        assertThat(originalVariant.getStock()).isEqualTo(7);
        assertThat(duplicateVariant.getProduct()).isSameAs(original);
        assertThat(requestedVariant.getSku()).isEqualTo("PRD-0001-BIRU-M");
        verify(variantService).delete(duplicateStock);
        verify(productRepository).delete(duplicate);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deletesProductAndVariantsWhenThereIsNoHistory() {
        ProductRepository productRepository = mock(ProductRepository.class);
        ProductVariantService variantService = mock(ProductVariantService.class);
        ProductService productService =
                new ProductService(productRepository, variantService);
        Product product = product("Kaos", "PRD-0001", 1L, new Category());
        ProductVariant variant = variant(product, "Hitam", "M", 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantService.findByProductId(1L)).thenReturn(List.of(variant));

        Product deletedProduct = productService.deleteProduct(1L);

        assertThat(deletedProduct).isSameAs(product);
        verify(variantService).delete(variant);
        verify(productRepository).delete(product);
    }

    private Product product(String name, String code, Long id, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setCode(code);
        product.setId(id);
        product.setCategory(category);
        return product;
    }

    private ProductService productService(
            ProductRepository productRepository,
            ProductVariantService variantService) {
        return new ProductService(productRepository, variantService);
    }

    private ProductVariant variant(Product product, String color, String size, int stock) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColor(color);
        variant.setSize(size);
        variant.setStock(stock);
        variant.setPurchasePrice(new BigDecimal("10.00"));
        variant.setSellingPrice(new BigDecimal("20.00"));
        return variant;
    }
}
