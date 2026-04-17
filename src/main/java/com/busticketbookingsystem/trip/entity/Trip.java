package com.busticketbookingsystem.trip.entity;

import com.busticketbookingsystem.agency.entity.Bus;
import com.busticketbookingsystem.agency.entity.Driver;
import com.busticketbookingsystem.customer.entity.Address;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Integer tripId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boarding_address_id")
    private Address boardingAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dropping_address_id")
    private Address droppingAddress;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver1_driver_id")
    private Driver driver1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver2_driver_id")
    private Driver driver2;

    @Column(name = "available_seats")
    private Integer availableSeats;

    @Column(name = "fare", precision = 10, scale = 2)
    private BigDecimal fare;

    @Column(name = "trip_date")
    private LocalDateTime tripDate;
}
