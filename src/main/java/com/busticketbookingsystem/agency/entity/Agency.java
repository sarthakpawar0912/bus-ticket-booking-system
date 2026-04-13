package com.busticketbookingsystem.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agency_id")
    private Integer agencyId;

    @Column(name = "name")
    private String name;

    @Column(name = "contact_person_name", length = 30)
    private String contactPersonName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 15)
    private String phone;
}
