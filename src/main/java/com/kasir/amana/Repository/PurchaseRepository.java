
package com.kasir.amana.Repository;

import com.kasir.amana.Model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Optional<Purchase> findByPurchaseNumber(String purchaseNumber);

    List<Purchase> findAllByOrderByCreatedAtDesc();

}
