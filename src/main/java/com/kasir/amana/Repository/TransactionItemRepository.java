
package com.kasir.amana.Repository;

import com.kasir.amana.Model.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {

    List<TransactionItem> findByTransactionId(Long transactionId);

}
