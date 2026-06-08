package com.sms.contact_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;



public class ContactDTO {

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Mobile number must contain exactly 10 digits")
    private String mobile;

    @Email(message = "Invalid email format")
    private String email;

    private String groupName;
}
