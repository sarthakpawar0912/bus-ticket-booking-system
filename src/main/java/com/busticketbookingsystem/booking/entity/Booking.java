package com.busticketbookingsystem.booking.entity;

import com.busticketbookingsystem.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    // Links to the Trip.java you uploaded
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    // Matches your ENUM in MySQL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('Available', 'Booked') DEFAULT 'Available'")
    private BookingStatus status;
}