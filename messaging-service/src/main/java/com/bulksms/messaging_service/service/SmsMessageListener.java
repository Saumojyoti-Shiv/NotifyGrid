package com.bulksms.messaging_service.service;

import com.bulksms.messaging_service.config.RabbitMQConfig;
import com.bulksms.messaging_service.dto.DeliveryReportEvent;
import com.bulksms.messaging_service.dto.SmsMessagePayload;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class SmsMessageListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final Random random = new Random();

    @RabbitListener(queues = RabbitMQConfig.SMS_QUEUE)
    public void receiveMessage(SmsMessagePayload payload) {
        // FR-MS3: Implement automatic retry logic for failed sends
        int maxRetries = 3;
        int attempts = 0;
        boolean success = false;
        
        while (attempts < maxRetries && !success) {
            attempts++;
            System.out.println("Processing SMS for " + payload.getPhone() + " (Attempt " + attempts + ")");
            
            // 95% success rate simulation
            if (random.nextDouble() > 0.05) {
                success = true;
                System.out.println("Status: SIMULATED SEND DELIVERED");
            } else {
                System.err.println("Status: SIMULATED SEND FAILED (Retrying...)");
                if (attempts < maxRetries) {
                    try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            }
        }

        String finalStatus = success ? "delivered" : "failed";
        System.out.println("--------------------------------------------------");
        
        // Emit Delivery Report Event
        DeliveryReportEvent event = new DeliveryReportEvent(
            payload.getCampaignId(),
            payload.getPhone(),
            finalStatus,
            LocalDateTime.now().toString()
        );
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.DELIVERY_EXCHANGE,
            RabbitMQConfig.DELIVERY_ROUTING_KEY,
            event
        );
    }
}
