package com.kasir.amana.Service;

import com.kasir.amana.Model.Transaction;
import com.kasir.amana.Repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository repository;

    public record RecentTransactionDto(
            String invoiceNumber,
            String cashierUsername,
            BigDecimal subtotal,
            String status,
            LocalDateTime createdAt
    ) {
        public String getInvoiceNumber() { return invoiceNumber; }
        public String getCashierUsername() { return cashierUsername; }
        public BigDecimal getSubtotal() { return subtotal; }
        public String getStatus() { return status; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    public record DailySalesDto(
            int day,
            LocalDate date,
            BigDecimal totalSales,
            long transactionCount,
            double heightPercent,
            boolean isToday,
            boolean isFuture
    ) {
        public int getDay() { return day; }
        public LocalDate getDate() { return date; }
        public BigDecimal getTotalSales() { return totalSales; }
        public long getTransactionCount() { return transactionCount; }
        public double getHeightPercent() { return heightPercent; }
        public boolean isToday() { return isToday; }
        public boolean isFuture() { return isFuture; }
    }

    public TransactionService(TransactionRepository repository) {
        this.repository  = repository;
    }

    public List<Transaction> findAll() {
        return repository.findAll();
    }

    public Optional<Transaction> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Transaction> findByInvoiceNumber(String invoiceNumber) {
        return repository.findByInvoiceNumber(invoiceNumber);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findLatestTransactions() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<RecentTransactionDto> findRecentTransactionDtos(int limit) {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .limit(limit)
                .map(t -> new RecentTransactionDto(
                        t.getInvoiceNumber(),
                        t.getCashier() != null ? t.getCashier().getUsername() : "-",
                        t.getSubtotal(),
                        t.getStatus(),
                        t.getCreatedAt()
                ))
                .toList();
    }

    public Transaction save(Transaction transaction) {
        return repository.save(transaction);
    }
    
    public List<Transaction> findTransactionsBetween(
            LocalDateTime start,
            LocalDateTime end) {
        return repository
                .findAllByCreatedAtBetweenOrderByCreatedAtDesc(
                        start, end);
    }

    public long countTransactionsBetween(
            LocalDateTime start,
            LocalDateTime end) {
        return repository.countByCreatedAtBetween(start, end);
    }

    public BigDecimal sumSubtotalBetween(
            LocalDateTime start,
            LocalDateTime end) {
        return repository.sumSubtotalBetween(start, end);
    }

    @Transactional(readOnly = true)
    public List<DailySalesDto> findDailySalesForMonth(YearMonth yearMonth) {
        LocalDate today = LocalDate.now();
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Transaction> transactions = repository.findAllByCreatedAtBetweenOrderByCreatedAtDesc(
                start.atStartOfDay(),
                end.atTime(23, 59, 59, 999999999)
        );

        Map<LocalDate, BigDecimal> salesMap = new HashMap<>();
        Map<LocalDate, Long> countMap = new HashMap<>();

        for (Transaction t : transactions) {
            LocalDate date = t.getCreatedAt().toLocalDate();
            BigDecimal subtotal = t.getSubtotal() != null ? t.getSubtotal() : BigDecimal.ZERO;
            salesMap.merge(date, subtotal, BigDecimal::add);
            countMap.merge(date, 1L, Long::sum);
        }

        BigDecimal maxDailySales = salesMap.values().stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        List<DailySalesDto> result = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            BigDecimal daySales = salesMap.getOrDefault(date, BigDecimal.ZERO);
            long count = countMap.getOrDefault(date, 0L);
            boolean isToday = date.equals(today);
            boolean isFuture = date.isAfter(today);

            double heightPercent = 0.0;
            if (maxDailySales.compareTo(BigDecimal.ZERO) > 0 && daySales.compareTo(BigDecimal.ZERO) > 0) {
                double ratio = daySales.divide(maxDailySales, 4, RoundingMode.HALF_UP).doubleValue();
                // Scale from 8% to 100% so non-zero sales always have a discernible bar
                heightPercent = Math.round((8.0 + (ratio * 92.0)) * 10.0) / 10.0;
            }

            result.add(new DailySalesDto(day, date, daySales, count, heightPercent, isToday, isFuture));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByCashierUsername(String username) {
        return repository.findByCashier_UsernameOrderByCreatedAtDesc(username);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumCashierSalesBetween(String username, LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactions = repository.findByCashier_UsernameOrderByCreatedAtDesc(username);
        return transactions.stream()
                .filter(t -> t.getCreatedAt() != null 
                        && !t.getCreatedAt().isBefore(start) 
                        && !t.getCreatedAt().isAfter(end))
                .map(t -> t.getSubtotal() != null ? t.getSubtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public long countCashierTransactionsBetween(String username, LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactions = repository.findByCashier_UsernameOrderByCreatedAtDesc(username);
        return transactions.stream()
                .filter(t -> t.getCreatedAt() != null 
                        && !t.getCreatedAt().isBefore(start) 
                        && !t.getCreatedAt().isAfter(end))
                .count();
    }

    @Transactional(readOnly = true)
    public List<Transaction> findRecentCashierTransactions(String username, int limit) {
        List<Transaction> transactions = repository.findByCashier_UsernameOrderByCreatedAtDesc(username);
        return transactions.stream().limit(limit).toList();
    }

}
