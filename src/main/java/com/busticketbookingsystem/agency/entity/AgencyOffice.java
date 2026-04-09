package com.busticketbookingsystem.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agency_offices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // ✅ Added Builder pattern
public class AgencyOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "office_id")
    private Integer officeId;

    @ManyToOne(fetch = FetchType.LAZY) // ✅ Optimized performance
    @JoinColumn(name = "agency_id")
    private Agency agency;

    @Column(name = "office_mail")
    private String officeMail;

    @Column(name = "office_contact_person_name")
    private String officeContactPersonName;

    @Column(name = "office_contact_number")
    private String officeContactNumber;

    @Column(name = "office_address_id")
    private Integer officeAddressId;
}