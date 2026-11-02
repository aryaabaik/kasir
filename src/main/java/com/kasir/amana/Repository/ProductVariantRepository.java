
package com.kasir.amana.Repository;

import com.kasir.amana.Model.ProductVariant;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Override
    @EntityGraph(attributePaths = {"product"})
    List<ProductVariant> findAll();

    List<ProductVariant> findByProductId(Long productId);

    List<ProductVariant> findByProductIdIn(List<Long> productIds);

    @EntityGraph(attributePaths = {"product", "product.category"})
    List<ProductVariant> findByActiveTrue();

    boolean existsBySku(String sku);

    /**
     * Finds a ProductVariant by ID with pessimistic write lock to prevent concurrent modifications.
     * Use this method when updating stock to prevent overselling or race conditions.
     * 
     * @param id The variant ID
     * @return Optional containing the locked variant
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdForUpdate(@Param("id") Long id);
}
