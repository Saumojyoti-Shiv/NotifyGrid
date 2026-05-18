package com.bulksms.billing_service.dto;

import lombok.Data;

@Data
public class CampaignSentEvent {
    private Long campaignId;
    private String username;
    private Integer recipientCount;
}
