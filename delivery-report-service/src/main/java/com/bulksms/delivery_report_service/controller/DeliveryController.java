package com.bulksms.delivery_report_service.controller;

import com.bulksms.delivery_report_service.model.DeliveryLog;
import com.bulksms.delivery_report_service.repository.DeliveryLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired
    private DeliveryLogRepository deliveryLogRepository;

    @GetMapping("/campaign/{campaignId}")
    public List<DeliveryLog> getLogsByCampaign(@PathVariable Long campaignId) {
        return deliveryLogRepository.findByCampaignId(campaignId);
    }

    @GetMapping
    public List<DeliveryLog> getAllLogs() {
        return deliveryLogRepository.findAll();
    }
}
