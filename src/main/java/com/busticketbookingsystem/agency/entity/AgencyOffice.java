package com.busticketbookingsystem.agency.entity;

import com.busticketbookingsystem.customer.entity.Address;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agency_offices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "office_id")
    private Integer officeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agency agency;

    @Column(name = "office_mail", length = 100)
    private String officeMail;

    @Column(name = "office_contact_person_name", length = 50)
    private String officeContactPersonName;

    @Column(name = "office_contact_number", columnDefinition = "CHAR(10)")
    private String officeContactNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_address_id")
    private Address officeAddress;
}
