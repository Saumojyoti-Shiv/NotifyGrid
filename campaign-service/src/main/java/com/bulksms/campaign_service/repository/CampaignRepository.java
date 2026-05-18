package com.bulksms.campaign_service.repository;

import com.bulksms.campaign_service.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
}
