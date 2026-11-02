package com.kasir.amana;

import com.kasir.amana.DTO.CartSessionItem;
import com.kasir.amana.Model.Category;
import com.kasir.amana.Model.Product;
import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Repository.CategoryRepository;
import com.kasir.amana.Repository.ProductRepository;
import com.kasir.amana.Repository.ProductVariantRepository;
import com.kasir.amana.Service.CartSessionService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class CartSessionTests {

    @Autowired
    private CartSessionService cartSessionService;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private ProductVariant testVariant1;
    private ProductVariant testVariant2;
    private HttpSession session1;
    private HttpSession session2;

    @BeforeEach
    void setup() {
        // Create test category
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test category");
        category = categoryRepository.saveAndFlush(category);

        // Create test product
        Product product = new Product();
        product.setName("Test Product");
        product.setCode("TEST-001");
        product.setCategory(category);
        product.setActive(true);
        product = productRepository.saveAndFlush(product);

        // Create test variants
        testVariant1 = new ProductVariant();
        testVariant1.setProduct(product);
        testVariant1.setSku("TEST-SKU-001");
        testVariant1.setSize("M");
        testVariant1.setColor("Blue");
        testVariant1.setPurchasePrice(BigDecimal.valueOf(50000));
        testVariant1.setSellingPrice(BigDecimal.valueOf(100000));
        testVariant1.setStock(10);
        testVariant1.setMinimumStock(5);
        testVariant1.setActive(true);
        testVariant1 = variantRepository.saveAndFlush(testVariant1);

        testVariant2 = new ProductVariant();
        testVariant2.setProduct(product);
        testVariant2.setSku("TEST-SKU-002");
        testVariant2.setSize("L");
        testVariant2.setColor("Red");
        testVariant2.setPurchasePrice(BigDecimal.valueOf(50000));
        testVariant2.setSellingPrice(BigDecimal.valueOf(120000));
        testVariant2.setStock(5);
        testVariant2.setMinimumStock(5);
        testVariant2.setActive(true);
        testVariant2 = variantRepository.saveAndFlush(testVariant2);

        // Create separate sessions for testing isolation
        session1 = new MockHttpSession();
        session2 = new MockHttpSession();
    }

    @Test
    void emptyCartShouldReturnEmptyList() {
        List<CartSessionItem> cart = cartSessionService.getCart(session1);
        assertThat(cart).isEmpty();
    }

    @Test
    void shouldAddItemToCart() {
        cartSessionService.addToCart(session1, testVariant1.getId(), 2);
        
        List<CartSessionItem> cart = cartSessionService.getCart(session1);
        assertThat(cart).hasSize(1);
        assertThat(cart.get(0).getVariantId()).isEqualTo(testVariant1.getId());
        assertThat(cart.get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldAddQuantityToExistingItem() {
        cartSessionService.addToCart(session1, testVariant1.getId(), 2);
        cartSessionService.addToCart(session1, testVariant1.getId(), 3);
        
        List<CartSessionItem> cart = cartSessionService.getCart(session1);
        assertThat(cart).hasSize(1);
        assertThat(cart.get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldAddMultipleDifferentItems() {
        cartSessionService.addToCart(session1, testVariant1.getId(), 2);
        cartSessionService.addToCart(session1, testVariant2.getId(), 3);
        
        List<CartSessionItem> cart = cartSessionService.getCart(session1);
        assertThat(cart).hasSize(2);
    }

    @Test
    void shouldUpdateItemQuantity() {
        cartSessionService.addToCart(session1, testVariant1.getId(), 2);
        cartSessionService.updateQuantity(session1, testVariant1.getId(), 5);
        
        List<CartSessionItem> cart = cartSessionService.getCart(session1);
        assertThat(cart).hasSize(1);
        assertThat(cart.get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldRemoveItemWhenQuantitySetToZero() {
        cartSessionService.addToCart(session1, testVariant1.getId(), 2);
        cartSessionService.updateQuantity(session1, testVariant1.getId(), 0);
        
        List<CartSessionItem> cart = cartSessionService.getCart(session1);
        assertThat(cart).isEmpty();
    }

    @Test
    void shouldRemoveSpecificItem() {
        cartSessionService.addToCart(session1, testVariant1.getId(), 2);
        cartSessionService.addToCart(session1, testVariant2.getId(), 3);
        
        cartSessionService.removeFromCart(session1, testVariant1.getId());
        
        List<CartSessionItem> cart = cartSessionService.getCart(session1);
        assertThat(cart).hasSize(1);
        assertThat(cart.get(0).getVariantId()).isEqualTo(testVariant2.getId());
    }

    @Test
    void shouldClearEntireCart() {
        cartSessionService.addToCart(session1, testVariant1.getId(), 2);
        cartSessionService.addToCart(session1, testVariant2.getId(), 3);
        
        cartSessionService.clearCart(session1);
        
        List<CartSessionItem> cart = cartSessionService.getCart(session1);
        assertThat(cart).isEmpty();
    }

    @Test
    void differentSessionsShouldHaveIsolatedCarts() {
        // Add items to session1
        cartSessionService.addToCart(session1, testVariant1.getId(), 2);
        
        // Add different items to session2
        cartSessionService.addToCart(session2, testVariant2.getId(), 5);
        
        // Verify isolation
        List<CartSessionItem> cart1 = cartSessionService.getCart(session1);
        List<CartSessionItem> cart2 = cartSessionService.getCart(session2);
        
        assertThat(cart1).hasSize(1);
        assertThat(cart1.get(0).getVariantId()).isEqualTo(testVariant1.getId());
        
        assertThat(cart2).hasSize(1);
        assertThat(cart2.get(0).getVariantId()).isEqualTo(testVariant2.getId());
    }

    @Test
    void shouldGetCartWithEnrichedDetails() {
        cartSessionService.addToCart(session1, testVariant1.getId(), 2);
        cartSessionService.addToCart(session1, testVariant2.getId(), 3);
        
        List<CartSessionService.CartItemWithDetails> detailedCart = 
                cartSessionService.getCartWithDetails(session1);
        
        assertThat(detailedCart).hasSize(2);
        
        CartSessionService.CartItemWithDetails item1 = detailedCart.stream()
                .filter(item -> item.variantId().equals(testVariant1.getId()))
                .findFirst()
                .orElseThrow();
        
        assertThat(item1.quantity()).isEqualTo(2);
        assertThat(item1.productName()).isEqualTo("Test Product");
        assertThat(item1.color()).isEqualTo("Blue");
        assertThat(item1.size()).isEqualTo("M");
        assertThat(item1.price()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(item1.stock()).isEqualTo(10);
        assertThat(item1.active()).isTrue();
    }

    @Test
    void shouldFetchCurrentPriceAndStockFromDatabase() {
        // Add item to cart
        cartSessionService.addToCart(session1, testVariant1.getId(), 2);
        
        // Simulate price change in database
        testVariant1.setSellingPrice(BigDecimal.valueOf(150000));
        testVariant1.setStock(8);
        variantRepository.saveAndFlush(testVariant1);
        
        // Get cart with details should reflect current database values
        List<CartSessionService.CartItemWithDetails> detailedCart = 
                cartSessionService.getCartWithDetails(session1);
        
        assertThat(detailedCart).hasSize(1);
        assertThat(detailedCart.get(0).price()).isEqualByComparingTo(BigDecimal.valueOf(150000));
        assertThat(detailedCart.get(0).stock()).isEqualTo(8);
    }

    @Test
    void shouldFilterOutInactiveVariantsInDetailedCart() {
        // Add item to cart
        cartSessionService.addToCart(session1, testVariant1.getId(), 2);
        
        // Deactivate variant
        testVariant1.setActive(false);
        variantRepository.saveAndFlush(testVariant1);
        
        // Get cart with details
        List<CartSessionService.CartItemWithDetails> detailedCart = 
                cartSessionService.getCartWithDetails(session1);
        
        assertThat(detailedCart).hasSize(1);
        assertThat(detailedCart.get(0).active()).isFalse();
    }

    @Test
    void shouldSaveMultipleItemsAtOnce() {
        List<CartSessionItem> items = List.of(
                new CartSessionItem(testVariant1.getId(), 2),
                new CartSessionItem(testVariant2.getId(), 3)
        );
        
        cartSessionService.saveCart(session1, items);
        
        List<CartSessionItem> retrievedCart = cartSessionService.getCart(session1);
        assertThat(retrievedCart).hasSize(2);
    }

    @Test
    void shouldIgnoreNullOrInvalidItemsWhenSaving() {
        List<CartSessionItem> items = List.of(
                new CartSessionItem(testVariant1.getId(), 2),
                new CartSessionItem(null, 3),  // Invalid: null variantId
                new CartSessionItem(testVariant2.getId(), 0),  // Invalid: zero quantity
                new CartSessionItem(testVariant2.getId(), -1)  // Invalid: negative quantity
        );
        
        cartSessionService.saveCart(session1, items);
        
        List<CartSessionItem> retrievedCart = cartSessionService.getCart(session1);
        assertThat(retrievedCart).hasSize(1);
        assertThat(retrievedCart.get(0).getVariantId()).isEqualTo(testVariant1.getId());
    }
}
