package com.busticketbookingsystem.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // ✅ Restored: Crucial for your Service layer to convert DTOs into this Entity
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agency_id")
    private Integer agencyId;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_person_name", nullable = false)
    private String contactPersonName;

    @Column(nullable = false, unique = true) // ✅ Prevents duplicate emails at the database level
    private String email;

    @Column(nullable = false, unique = true) // ✅ Prevents duplicate phones at the database level
    private String phone;
}