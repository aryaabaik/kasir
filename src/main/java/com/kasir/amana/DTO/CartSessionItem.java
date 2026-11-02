package com.kasir.amana.DTO;

import java.io.Serializable;

/**
 * Session-stored cart item.
 * Stores only variant ID and quantity.
 * Prices and product details are always fetched fresh from the database.
 */
public class CartSessionItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long variantId;
    private Integer quantity;

    public CartSessionItem() {
    }

    public CartSessionItem(Long variantId, Integer quantity) {
        this.variantId = variantId;
        this.quantity = quantity;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
