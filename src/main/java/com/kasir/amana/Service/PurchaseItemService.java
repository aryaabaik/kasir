
package com.kasir.amana.Service;

import com.kasir.amana.Model.PurchaseItem;
import com.kasir.amana.Repository.PurchaseItemRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PurchaseItemService {

    private final PurchaseItemRepository repository;

    public PurchaseItemService(PurchaseItemRepository repository) {
        this.repository = repository;
    }

    public List<PurchaseItem> findAll() {
        return repository.findAll();
    }

    public List<PurchaseItem> findByPurchaseId(Long purchaseId) {
        return repository.findByPurchaseId(purchaseId);
    }

    public PurchaseItem save(PurchaseItem item) {
        return repository.save(item);
    }
}
