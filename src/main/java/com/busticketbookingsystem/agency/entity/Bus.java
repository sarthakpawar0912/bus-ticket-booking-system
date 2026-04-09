package com.busticketbookingsystem.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // ✅ Added Builder pattern for cleaner object creation
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bus_id")
    private Integer busId;

    @ManyToOne(fetch = FetchType.LAZY) // ✅ Added LAZY fetching for better performance
    @JoinColumn(name = "office_id")
    private AgencyOffice office;

    @Column(name = "registration_number", nullable = false, unique = true) // ✅ Added database-level safety
    private String registrationNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private String type;
}