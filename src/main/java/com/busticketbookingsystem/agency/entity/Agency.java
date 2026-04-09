package com.busticketbookingsystem.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // ✅ Added Builder pattern
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agency_id")
    private Integer agencyId;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_person_name", nullable = false)
    private String contactPersonName;

    @Column(nullable = false, unique = true) // ✅ Emails must be unique
    private String email;

    @Column(nullable = false, unique = true) // ✅ Phones must be unique
    private String phone;
}