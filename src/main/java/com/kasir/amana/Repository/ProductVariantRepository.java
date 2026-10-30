
package com.kasir.amana.Repository;

import com.kasir.amana.Model.ProductVariant;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Override
    @EntityGraph(attributePaths = {"product"})
    List<ProductVariant> findAll();

    List<ProductVariant> findByProductId(Long productId);

    List<ProductVariant> findByProductIdIn(List<Long> productIds);

    @EntityGraph(attributePaths = {"product", "product.category"})
    List<ProductVariant> findByActiveTrue();

    boolean existsBySku(String sku);
}
