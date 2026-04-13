package com.busticketbookingsystem.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bus_id")
    private Integer busId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private AgencyOffice office;

    @Column(name = "registration_number", length = 20)
    private String registrationNumber;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "type", length = 30)
    private String type;
}
