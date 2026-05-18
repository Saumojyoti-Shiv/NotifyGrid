package com.bulksms.delivery_report_service.service;

import com.bulksms.delivery_report_service.config.RabbitMQConfig;
import com.bulksms.delivery_report_service.dto.DeliveryReportEvent;
import com.bulksms.delivery_report_service.model.DeliveryLog;
import com.bulksms.delivery_report_service.repository.DeliveryLogRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeliveryReportListener {

    @Autowired
    private DeliveryLogRepository deliveryLogRepository;

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_QUEUE)
    public void receiveDeliveryReport(DeliveryReportEvent event) {
        System.out.println("--------------------------------------------------");
        System.out.println("RECEIVED DELIVERY REPORT FROM QUEUE");
        System.out.println("Campaign ID: " + event.getCampaignId());
        System.out.println("Recipient: " + event.getPhone());
        System.out.println("Status: " + event.getStatus());
        System.out.println("--------------------------------------------------");

        DeliveryLog log = new DeliveryLog();
        log.setCampaignId(event.getCampaignId());
        log.setPhone(event.getPhone());
        log.setStatus(event.getStatus());
        log.setTimestamp(event.getTimestamp());
        
        deliveryLogRepository.save(log);
    }
}
