
package com.kasir.amana.Repository;

import com.kasir.amana.Model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByInvoiceNumber(String invoiceNumber);

    boolean existsByCashier_Id(Long cashierId);
    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.cashier
        ORDER BY t.createdAt DESC
        """)
    List<Transaction> findAllByOrderByCreatedAtDesc();

    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.cashier
        WHERE t.createdAt BETWEEN :start AND :end
        ORDER BY t.createdAt DESC
        """)
    List<Transaction> findAllByCreatedAtBetweenOrderByCreatedAtDesc(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.cashier
        WHERE t.cashier.username = :username
        ORDER BY t.createdAt DESC
        """)
    List<Transaction> findByCashier_UsernameOrderByCreatedAtDesc(
        @Param("username") String username
    );

    long countByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT COALESCE(SUM(t.subtotal), 0)
        FROM Transaction t
        WHERE t.createdAt BETWEEN :start AND :end
        """)
    BigDecimal sumSubtotalBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT t FROM Transaction t
    JOIN FETCH t.cashier
    WHERE t.invoiceNumber = :invoiceNumber
    """)
    Optional<Transaction> findReceiptByInvoiceNumber(
            @Param("invoiceNumber") String invoiceNumber
    );
}
