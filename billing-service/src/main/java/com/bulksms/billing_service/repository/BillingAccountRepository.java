package com.bulksms.billing_service.repository;

import com.bulksms.billing_service.model.BillingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BillingAccountRepository extends JpaRepository<BillingAccount, Long> {
    Optional<BillingAccount> findByUsername(String username);
}
