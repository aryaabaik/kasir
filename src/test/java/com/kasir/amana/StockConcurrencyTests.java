package com.kasir.amana;

import com.kasir.amana.Model.Category;
import com.kasir.amana.Model.Product;
import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Model.User;
import com.kasir.amana.Repository.CategoryRepository;
import com.kasir.amana.Repository.ProductRepository;
import com.kasir.amana.Repository.ProductVariantRepository;
import com.kasir.amana.Repository.UserRepository;
import com.kasir.amana.Service.CheckoutService;
import com.kasir.amana.Service.StockMovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class StockConcurrencyTests {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ProductVariant testVariant;
    private User testCashier;

    @BeforeEach
    void setup() {
        // Create test category
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test category for testing");
        category = categoryRepository.saveAndFlush(category);

        // Create test product
        Product product = new Product();
        product.setName("Test Product");
        product.setCode("TEST-001");
        product.setCategory(category);
        product.setActive(true);
        product = productRepository.saveAndFlush(product);

        // Create test variant with initial stock of 10
        testVariant = new ProductVariant();
        testVariant.setProduct(product);
        testVariant.setSku("TEST-SKU-001");
        testVariant.setSize("M");
        testVariant.setColor("Blue");
        testVariant.setPurchasePrice(BigDecimal.valueOf(50000));
        testVariant.setSellingPrice(BigDecimal.valueOf(100000));
        testVariant.setStock(10);
        testVariant.setMinimumStock(5);
        testVariant.setActive(true);
        testVariant = variantRepository.saveAndFlush(testVariant);

        // Create test cashier
        testCashier = new User();
        testCashier.setUsername("testcashier");
        testCashier.setPasswordHash(passwordEncoder.encode("password"));
        testCashier.setFullName("Test Cashier");
        testCashier.setRole("CASHIER");
        testCashier.setActive(true);
        testCashier = userRepository.saveAndFlush(testCashier);
    }

    @Test
    void checkoutWithInsufficientStockShouldFail() {
        // Attempt to checkout more than available stock
        List<CheckoutService.CheckoutItem> items = List.of(
                new CheckoutService.CheckoutItem(testVariant.getId(), 15)
        );

        assertThatThrownBy(() ->
                checkoutService.checkout(testCashier.getUsername(), items, BigDecimal.valueOf(1500000))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stok tidak mencukupi");

        // Verify stock was not modified
        ProductVariant variant = variantRepository.findById(testVariant.getId()).orElseThrow();
        assertThat(variant.getStock()).isEqualTo(10);
    }

    @Test
    void checkoutWithExactStockShouldSucceed() {
        // Checkout all available stock
        List<CheckoutService.CheckoutItem> items = List.of(
                new CheckoutService.CheckoutItem(testVariant.getId(), 10)
        );

        checkoutService.checkout(testCashier.getUsername(), items, BigDecimal.valueOf(1000000));

        // Verify stock was reduced to zero
        ProductVariant variant = variantRepository.findById(testVariant.getId()).orElseThrow();
        assertThat(variant.getStock()).isEqualTo(0);
    }

    @Test
    void checkoutWithValidStockShouldReduceStock() {
        // Checkout 3 items
        List<CheckoutService.CheckoutItem> items = List.of(
                new CheckoutService.CheckoutItem(testVariant.getId(), 3)
        );

        checkoutService.checkout(testCashier.getUsername(), items, BigDecimal.valueOf(300000));

        // Verify stock was reduced correctly
        ProductVariant variant = variantRepository.findById(testVariant.getId()).orElseThrow();
        assertThat(variant.getStock()).isEqualTo(7);
    }

    @Test
    void stockMovementOutWithInsufficientStockShouldFail() {
        // Attempt to remove more stock than available
        assertThatThrownBy(() ->
                stockMovementService.adjustStock(
                        testVariant.getId(),
                        testCashier,
                        "OUT",
                        15,
                        "Test removal"
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stok tidak mencukupi");

        // Verify stock was not modified
        ProductVariant variant = variantRepository.findById(testVariant.getId()).orElseThrow();
        assertThat(variant.getStock()).isEqualTo(10);
    }

    @Test
    void stockMovementInShouldIncreaseStock() {
        // Add stock
        stockMovementService.adjustStock(
                testVariant.getId(),
                testCashier,
                "IN",
                5,
                "Restock"
        );

        // Verify stock was increased
        ProductVariant variant = variantRepository.findById(testVariant.getId()).orElseThrow();
        assertThat(variant.getStock()).isEqualTo(15);
    }

    @Test
    void stockMovementOutShouldDecreaseStock() {
        // Remove stock
        stockMovementService.adjustStock(
                testVariant.getId(),
                testCashier,
                "OUT",
                3,
                "Damaged goods"
        );

        // Verify stock was decreased
        ProductVariant variant = variantRepository.findById(testVariant.getId()).orElseThrow();
        assertThat(variant.getStock()).isEqualTo(7);
    }

    @Test
    void multipleCheckoutItemsShouldAllSucceedOrAllFail() {
        // Create second variant with low stock
        ProductVariant variant2 = new ProductVariant();
        variant2.setProduct(testVariant.getProduct());
        variant2.setSku("TEST-SKU-002");
        variant2.setSize("L");
        variant2.setColor("Red");
        variant2.setPurchasePrice(BigDecimal.valueOf(50000));
        variant2.setSellingPrice(BigDecimal.valueOf(100000));
        variant2.setStock(2);
        variant2.setMinimumStock(5);
        variant2.setActive(true);
        variant2 = variantRepository.saveAndFlush(variant2);

        // Attempt checkout where second item has insufficient stock
        List<CheckoutService.CheckoutItem> items = List.of(
                new CheckoutService.CheckoutItem(testVariant.getId(), 5),
                new CheckoutService.CheckoutItem(variant2.getId(), 5) // Not enough stock
        );

        assertThatThrownBy(() ->
                checkoutService.checkout(testCashier.getUsername(), items, BigDecimal.valueOf(1000000))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stok tidak mencukupi");

        // Verify neither variant's stock was modified (transaction rollback)
        ProductVariant v1 = variantRepository.findById(testVariant.getId()).orElseThrow();
        ProductVariant v2 = variantRepository.findById(variant2.getId()).orElseThrow();
        assertThat(v1.getStock()).isEqualTo(10);
        assertThat(v2.getStock()).isEqualTo(2);
    }

    @Test
    void checkoutWithInactiveProductShouldFail() {
        // Deactivate product
        Product product = testVariant.getProduct();
        product.setActive(false);
        productRepository.saveAndFlush(product);

        List<CheckoutService.CheckoutItem> items = List.of(
                new CheckoutService.CheckoutItem(testVariant.getId(), 1)
        );

        assertThatThrownBy(() ->
                checkoutService.checkout(testCashier.getUsername(), items, BigDecimal.valueOf(100000))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tidak aktif");

        // Verify stock was not modified
        ProductVariant variant = variantRepository.findById(testVariant.getId()).orElseThrow();
        assertThat(variant.getStock()).isEqualTo(10);
    }

    @Test
    void stockMovementWithInvalidQuantityShouldFail() {
        assertThatThrownBy(() ->
                stockMovementService.adjustStock(
                        testVariant.getId(),
                        testCashier,
                        "IN",
                        0,
                        "Invalid"
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jumlah stok harus lebih dari 0");

        assertThatThrownBy(() ->
                stockMovementService.adjustStock(
                        testVariant.getId(),
                        testCashier,
                        "IN",
                        -5,
                        "Invalid"
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jumlah stok harus lebih dari 0");
    }

    @Test
    void stockMovementWithInvalidTypeShouldFail() {
        assertThatThrownBy(() ->
                stockMovementService.adjustStock(
                        testVariant.getId(),
                        testCashier,
                        "INVALID",
                        5,
                        "Test"
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jenis perubahan stok tidak valid");
    }
}
