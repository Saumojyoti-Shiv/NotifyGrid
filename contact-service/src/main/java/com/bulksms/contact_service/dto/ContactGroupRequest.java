package com.bulksms.contact_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class ContactGroupRequest {
    private String name;
    private String filterPrefix;
    private List<Long> contactIds;
}
