package com.bulksms.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private String type; // LOW_BALANCE, CAMPAIGN_COMPLETED
    private String username;
    private String message;
}
