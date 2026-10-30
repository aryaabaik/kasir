package com.kasir.amana.Repository;

import com.kasir.amana.Model.StockMovement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long> {

    @EntityGraph(attributePaths = {
            "variant",
            "variant.product",
            "user"
    })
    List<StockMovement> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {
            "variant",
            "variant.product",
            "user"
    })
    List<StockMovement> findByVariantIdOrderByCreatedAtDesc(
            Long variantId);

}