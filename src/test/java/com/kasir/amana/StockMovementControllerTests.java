package com.kasir.amana;

import com.kasir.amana.Controller.Admin.StockMovementController;
import com.kasir.amana.Model.Product;
import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Repository.UserRepository;
import com.kasir.amana.Service.ProductVariantService;
import com.kasir.amana.Service.StockMovementService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StockMovementControllerTests {

    @Test
    void indexProvidesOnlyActiveProductVariantsForStockAdjustments() {
        Product activeProduct = product(true);
        Product inactiveProduct = product(false);
        ProductVariant activeVariant = variant(activeProduct, true);
        ProductVariant inactiveVariant = variant(activeProduct, false);
        ProductVariant inactiveProductVariant = variant(inactiveProduct, true);

        ProductVariantService variantService = mock(ProductVariantService.class);
        StockMovementService movementService = mock(StockMovementService.class);
        when(variantService.findAll()).thenReturn(
                List.of(activeVariant, inactiveVariant, inactiveProductVariant));
        when(movementService.findAllLatest()).thenReturn(List.of());

        StockMovementController controller = new StockMovementController(
                movementService, variantService, mock(UserRepository.class));
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.index(model)).isEqualTo("admin/stock/index");
        assertThat(model.get("activeVariants")).isEqualTo(List.of(activeVariant));
        assertThat(model.get("variants"))
                .isEqualTo(List.of(activeVariant, inactiveVariant, inactiveProductVariant));
    }

    private Product product(boolean active) {
        Product product = new Product();
        product.setActive(active);
        return product;
    }

    private ProductVariant variant(Product product, boolean active) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setActive(active);
        return variant;
    }
}
