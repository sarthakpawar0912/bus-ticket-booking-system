package com.busticketbookingsystem.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agency_offices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // ✅ Crucial for converting RequestDTOs into this Entity
public class AgencyOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "office_id")
    private Integer officeId;

    // ✅ LAZY fetch prevents dragging the entire Agency object out of the DB unless explicitly requested
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agency agency;

    @Column(name = "office_mail")
    private String officeMail;

    @Column(name = "office_contact_person_name")
    private String officeContactPersonName;

    @Column(name = "office_contact_number")
    private String officeContactNumber;

    // Kept as an Integer to easily link with Member 5's Address table without importing their entity
    @Column(name = "office_address_id")
    private Integer officeAddressId;
}