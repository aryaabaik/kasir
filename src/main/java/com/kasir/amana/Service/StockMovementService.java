
package com.kasir.amana.Service;

import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Model.StockMovement;
import com.kasir.amana.Model.User;
import com.kasir.amana.Repository.ProductVariantRepository;
import com.kasir.amana.Repository.StockMovementRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockMovementService {

    private final StockMovementRepository repository;
    private final ProductVariantRepository variantRepository;

    public StockMovementService(
            StockMovementRepository repository,
            ProductVariantRepository variantRepository) {
        this.repository = repository;
        this.variantRepository = variantRepository;
    }

    public List<StockMovement> findAllLatest() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public List<StockMovement> findByVariantId(Long variantId) {
        return repository.findByVariantIdOrderByCreatedAtDesc(variantId);
    }

    public StockMovement save(StockMovement movement) {
        return repository.save(movement);
    }

    @Transactional
    public StockMovement adjustStock(
            Long variantId,
            User user,
            String movementType,
            Integer quantity,
            String notes) {

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "Pengguna yang melakukan perubahan stok tidak valid.");
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Jumlah stok harus lebih dari 0.");
        }

        if (!"IN".equals(movementType)
                && !"OUT".equals(movementType)) {
            throw new IllegalArgumentException(
                    "Jenis perubahan stok tidak valid.");
        }

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Varian produk tidak ditemukan."));

        int stockBefore = variant.getStock();

        if ("OUT".equals(movementType) && quantity > stockBefore) {
            throw new IllegalArgumentException(
                    "Stok tidak mencukupi.");
        }

        int stockAfter = "IN".equals(movementType)
                ? Math.addExact(stockBefore, quantity)
                : stockBefore - quantity;

        variant.setStock(stockAfter);
        variantRepository.save(variant);

        StockMovement movement = new StockMovement();
        movement.setVariant(variant);
        movement.setUser(user);
        movement.setMovementType(movementType);
        movement.setQuantityChange(
                "IN".equals(movementType) ? quantity : -quantity);
        movement.setStockBefore(stockBefore);
        movement.setStockAfter(stockAfter);
        movement.setNotes(notes);

        return repository.save(movement);
    }
}
