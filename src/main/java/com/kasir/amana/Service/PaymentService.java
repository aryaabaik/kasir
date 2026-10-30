package com.kasir.amana.Service;

import com.kasir.amana.Model.Payment;
import com.kasir.amana.Repository.PaymentRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public List<Payment> findAll() {
        return repository.findAll();
    }

    public List<Payment> findByTransactionId(Long transactionId) {
        return repository.findByTransactionId(transactionId);
    }

    public Payment save(Payment payment) {
        return repository.save(payment);
    }

}