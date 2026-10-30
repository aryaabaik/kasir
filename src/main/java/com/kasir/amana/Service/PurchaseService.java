package com.kasir.amana.Service;

import com.kasir.amana.Model.Purchase;
import com.kasir.amana.Repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseService {
    private final PurchaseRepository repository;

    public PurchaseService(PurchaseRepository repository) {
        this.repository = repository;
    }

    public List<Purchase> findAll() {
        return repository.findAll();
    }

    public Optional<Purchase> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Purchase> findByPurchaseNumber(String number) {
        return repository.findByPurchaseNumber(number);
    }

    public List<Purchase> findLatestPurchases() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Purchase save(Purchase purchase) {
        return repository.save(purchase);
    }
}
