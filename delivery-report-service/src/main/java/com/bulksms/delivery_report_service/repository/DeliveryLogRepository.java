package com.bulksms.delivery_report_service.repository;

import com.bulksms.delivery_report_service.model.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {
    List<DeliveryLog> findByCampaignId(Long campaignId);
}
