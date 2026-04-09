package com.busticketbookingsystem.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Integer driverId;

    // Must be unique! Two drivers cannot share a license number
    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    // LAZY fetching so we don't accidentally load the whole office every time we look up a driver
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private AgencyOffice office;

    // Kept as an Integer (just like the Customer template) so Member 2 doesn't have to import Member 5's Address entity
    @Column(name = "address_id")
    private Integer addressId;
}