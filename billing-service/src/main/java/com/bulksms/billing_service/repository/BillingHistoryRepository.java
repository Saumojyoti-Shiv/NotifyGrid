package com.bulksms.billing_service.repository;

import com.bulksms.billing_service.model.BillingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BillingHistoryRepository extends JpaRepository<BillingHistory, Long> {
    List<BillingHistory> findByUsername(String username);
}
