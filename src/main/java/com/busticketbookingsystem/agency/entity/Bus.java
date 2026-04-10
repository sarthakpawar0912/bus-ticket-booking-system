package com.busticketbookingsystem.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // ✅ Required for your Service layer
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bus_id")
    private Integer busId;

    // ✅ THIS was the missing line causing the red error!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private AgencyOffice office;

    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private String type;
}