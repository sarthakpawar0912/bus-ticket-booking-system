package com.busticketbookingsystem.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bus_id")
    private Integer busId;

    @ManyToOne
    @JoinColumn(name = "office_id")
    private AgencyOffice office;

    @Column(name = "registration_number")
    private String registrationNumber;

    private Integer capacity;

    private String type;
}