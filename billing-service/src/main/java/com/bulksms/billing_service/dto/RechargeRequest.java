package com.bulksms.billing_service.dto;

import lombok.Data;

@Data
public class RechargeRequest {
    private String username;
    private Integer amount;
}
