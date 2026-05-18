package com.bulksms.campaign_service.dto;

import lombok.Data;

@Data
public class ContactDto {
    private Long id;
    private String name;
    private String phone;
    private String email;
}
