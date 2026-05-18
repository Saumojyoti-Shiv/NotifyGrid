package com.bulksms.contact_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "contact_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String filterPrefix;

    @ManyToMany
    @JoinTable(
        name = "group_contacts",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private List<Contact> contacts;
}
