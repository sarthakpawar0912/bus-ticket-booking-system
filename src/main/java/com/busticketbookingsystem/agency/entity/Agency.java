package com.busticketbookingsystem.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agency_id")
    private Integer agencyId;

    private String name;

    @Column(name = "contact_person_name")
    private String contactPersonName;

    private String email;

    private String phone;
}
