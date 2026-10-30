package com.kasir.amana.Service;

import com.kasir.amana.Model.TransactionItem;
import com.kasir.amana.Repository.TransactionItemRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TransactionItemService {
    private final TransactionItemRepository repository;

    public TransactionItemService(TransactionItemRepository repository) {
        this.repository = repository;
    }

    public List<TransactionItem> findAll() {
        return repository.findAll();
    }

    public List<TransactionItem> findByTransactionId(Long transactionId) {
        return repository.findByTransactionId(transactionId);
    }

    public TransactionItem save(TransactionItem item) {
        return repository.save(item);
    }
}
