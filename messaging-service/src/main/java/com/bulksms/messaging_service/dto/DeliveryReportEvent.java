package com.bulksms.messaging_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryReportEvent {
    private Long campaignId;
    private String phone;
    private String status;
    private String timestamp;
}
