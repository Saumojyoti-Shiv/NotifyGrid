package com.bulksms.campaign_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String groupId;
    @Column(columnDefinition = "TEXT")
    private String message;
    private String schedule;
    private String status;
    private String sentAt;
    private String createdBy;
    private Integer recipientCount;
}
