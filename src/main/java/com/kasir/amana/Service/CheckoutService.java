
package com.kasir.amana.Service;

import com.kasir.amana.Model.*;
import com.kasir.amana.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
public class CheckoutService {

    private final TransactionRepository transactionRepository;
    private final TransactionItemRepository itemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    public CheckoutService(
            TransactionRepository transactionRepository,
            TransactionItemRepository itemRepository,
            PaymentRepository paymentRepository,
            ProductVariantRepository variantRepository,
            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.itemRepository = itemRepository;
        this.paymentRepository = paymentRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Transaction checkout(
            String username,
            List<CheckoutItem> items,
            BigDecimal amountReceived) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Kasir tidak valid.");
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Keranjang masih kosong.");
        }

        if (amountReceived == null || amountReceived.signum() < 0) {
            throw new IllegalArgumentException("Jumlah pembayaran tidak valid.");
        }

        User cashier = userRepository.findByUsername(username)
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Akun kasir tidak ditemukan atau tidak aktif."));

        Transaction transaction = new Transaction();
        transaction.setInvoiceNumber(
                "INV-" + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + "-" + UUID.randomUUID().toString().substring(0, 6)
                        .toUpperCase());
        transaction.setCashier(cashier);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setStatus("COMPLETED");
        transaction.setDiscount(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;

        // Validasi semua item terlebih dahulu.
        for (CheckoutItem item : items) {
            if (item == null || item.variantId() == null
                    || item.quantity() == null || item.quantity() <= 0) {
                throw new IllegalArgumentException("Varian dan jumlah barang harus valid.");
            }

            ProductVariant variant = variantRepository.findById(item.variantId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Varian produk tidak ditemukan."));

            if (!Boolean.TRUE.equals(variant.getActive())
                    || !Boolean.TRUE.equals(variant.getProduct().getActive())) {
                throw new IllegalArgumentException(
                        "Produk " + variant.getProduct().getName() + " tidak aktif.");
            }

            if (variant.getStock() == null
                    || item.quantity() > variant.getStock()) {
                throw new IllegalArgumentException(
                        "Stok tidak mencukupi untuk "
                                + variant.getProduct().getName()
                                + " (" + variant.getColor()
                                + ", " + variant.getSize() + ").");
            }

            total = total.add(
                    variant.getSellingPrice()
                            .multiply(BigDecimal.valueOf(item.quantity())));
        }

        if (amountReceived.compareTo(total) < 0) {
            throw new IllegalArgumentException("Uang pembayaran kurang.");
        }

        transaction.setSubtotal(total);
        transaction = transactionRepository.saveAndFlush(transaction);

        for (CheckoutItem item : items) {
            ProductVariant variant = variantRepository.findById(item.variantId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Varian produk tidak ditemukan."));

            int stockBefore = variant.getStock();
            int stockAfter = stockBefore - item.quantity();

            if (stockAfter < 0) {
                throw new IllegalArgumentException("Stok tidak mencukupi.");
            }

            variant.setStock(stockAfter);
            variantRepository.save(variant);

            TransactionItem transactionItem = new TransactionItem();
            transactionItem.setTransaction(transaction);
            transactionItem.setVariant(variant);
            transactionItem.setProductName(variant.getProduct().getName());
            transactionItem.setSize(variant.getSize());
            transactionItem.setColor(variant.getColor());
            transactionItem.setQuantity(item.quantity());
            transactionItem.setUnitPrice(variant.getSellingPrice());
            transactionItem.setDiscount(BigDecimal.ZERO);
            transactionItem.setSubtotal(
                    variant.getSellingPrice()
                            .multiply(BigDecimal.valueOf(item.quantity())));

            itemRepository.save(transactionItem);
        }

        Payment payment = new Payment();
        payment.setTransaction(transaction);
        payment.setMethod("CASH");
        payment.setAmount(total);
        payment.setAmountReceived(amountReceived);
        payment.setChangeAmount(amountReceived.subtract(total));
        payment.setStatus("PAID");
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        return transaction;
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> findReceiptTransaction(String invoiceNumber) {
        return transactionRepository.findReceiptByInvoiceNumber(invoiceNumber);
    }

    @Transactional(readOnly = true)
    public List<TransactionItem> findReceiptItems(Long transactionId) {
        return itemRepository.findByTransactionId(transactionId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findCashierTransactions(String username) {
        return transactionRepository
                .findByCashier_UsernameOrderByCreatedAtDesc(username);
    }

    public record CheckoutItem(Long variantId, Integer quantity) {}
}
