package com.kasir.amana.Service;

import com.kasir.amana.DTO.CartSessionItem;
import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Repository.ProductVariantRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages cashier cart persistence in HTTP session.
 * Each authenticated cashier has an isolated cart stored in their session.
 */
@Service
public class CartSessionService {

    private static final String CART_SESSION_KEY = "CASHIER_CART";
    
    private final ProductVariantRepository variantRepository;

    public CartSessionService(ProductVariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    /**
     * Get cart items from session.
     * Returns empty list if no cart exists.
     */
    public List<CartSessionItem> getCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartSessionItem> cart = (List<CartSessionItem>) session.getAttribute(CART_SESSION_KEY);
        return cart != null ? cart : new ArrayList<>();
    }

    /**
     * Save cart items to session.
     */
    public void saveCart(HttpSession session, List<CartSessionItem> items) {
        if (items == null || items.isEmpty()) {
            session.removeAttribute(CART_SESSION_KEY);
        } else {
            // Validate and clean items
            List<CartSessionItem> validItems = items.stream()
                    .filter(item -> item != null 
                            && item.getVariantId() != null 
                            && item.getQuantity() != null 
                            && item.getQuantity() > 0)
                    .collect(Collectors.toList());
            
            session.setAttribute(CART_SESSION_KEY, validItems);
        }
    }

    /**
     * Add or update item in cart.
     * If item exists, quantity is added to existing quantity.
     */
    public void addToCart(HttpSession session, Long variantId, Integer quantity) {
        if (variantId == null || quantity == null || quantity <= 0) {
            return;
        }

        List<CartSessionItem> cart = getCart(session);
        
        Optional<CartSessionItem> existing = cart.stream()
                .filter(item -> item.getVariantId().equals(variantId))
                .findFirst();
        
        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            cart.add(new CartSessionItem(variantId, quantity));
        }
        
        saveCart(session, cart);
    }

    /**
     * Update item quantity in cart.
     */
    public void updateQuantity(HttpSession session, Long variantId, Integer quantity) {
        if (variantId == null) {
            return;
        }

        List<CartSessionItem> cart = getCart(session);
        
        if (quantity == null || quantity <= 0) {
            // Remove item if quantity is 0 or negative
            cart.removeIf(item -> item.getVariantId().equals(variantId));
        } else {
            Optional<CartSessionItem> existing = cart.stream()
                    .filter(item -> item.getVariantId().equals(variantId))
                    .findFirst();
            
            if (existing.isPresent()) {
                existing.get().setQuantity(quantity);
            } else {
                cart.add(new CartSessionItem(variantId, quantity));
            }
        }
        
        saveCart(session, cart);
    }

    /**
     * Remove item from cart.
     */
    public void removeFromCart(HttpSession session, Long variantId) {
        if (variantId == null) {
            return;
        }

        List<CartSessionItem> cart = getCart(session);
        cart.removeIf(item -> item.getVariantId().equals(variantId));
        saveCart(session, cart);
    }

    /**
     * Clear entire cart.
     */
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    /**
     * Get cart with enriched product data.
     * Always fetches current prices, stock, and active status from database.
     */
    @Transactional(readOnly = true)
    public List<CartItemWithDetails> getCartWithDetails(HttpSession session) {
        List<CartSessionItem> cart = getCart(session);
        if (cart.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> variantIds = cart.stream()
                .map(CartSessionItem::getVariantId)
                .collect(Collectors.toList());

        List<ProductVariant> variants = variantRepository.findAllById(variantIds);
        Map<Long, ProductVariant> variantMap = variants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        return cart.stream()
                .map(item -> {
                    ProductVariant variant = variantMap.get(item.getVariantId());
                    if (variant != null) {
                        return new CartItemWithDetails(
                                item.getVariantId(),
                                item.getQuantity(),
                                variant.getProduct().getName(),
                                variant.getProduct().getCode(),
                                variant.getSku(),
                                variant.getColor(),
                                variant.getSize(),
                                variant.getSellingPrice(),
                                variant.getStock(),
                                Boolean.TRUE.equals(variant.getActive()) 
                                        && Boolean.TRUE.equals(variant.getProduct().getActive())
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * DTO for cart item with full product details.
     */
    public record CartItemWithDetails(
            Long variantId,
            Integer quantity,
            String productName,
            String productCode,
            String sku,
            String color,
            String size,
            java.math.BigDecimal price,
            Integer stock,
            Boolean active
    ) {}
}
