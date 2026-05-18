package com.bulksms.campaign_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class GroupDto {
    private Long id;
    private String name;
    private String filterPrefix;
    private List<ContactDto> contacts;
}
