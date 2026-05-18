package com.bulksms.notification_service.service;

import com.bulksms.notification_service.dto.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @RabbitListener(queues = "notification_queue")
    public void handleNotification(NotificationEvent event) {
        System.out.println("--------------------------------------------------");
        System.out.println("NOTIFICATION RECEIVED [" + event.getType() + "]");
        System.out.println("User: " + event.getUsername());
        System.out.println("Message: " + event.getMessage());
        System.out.println("--------------------------------------------------");
        
        // In a real system, you would call an Email API or Push Notification service here.
        if ("LOW_BALANCE".equals(event.getType())) {
            simulateEmail(event.getUsername(), "Low Balance Alert", event.getMessage());
        } else {
            simulateEmail(event.getUsername(), "Campaign Status", event.getMessage());
        }
    }

    private void simulateEmail(String to, String subject, String body) {
        System.out.println(">>> SIMULATING EMAIL TO: " + to);
        System.out.println(">>> SUBJECT: " + subject);
        System.out.println(">>> CONTENT: " + body);
    }
}
