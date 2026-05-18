package com.bulksms.campaign_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessagePayload {
    private Long campaignId;
    private String phone;
    private String message;
}
