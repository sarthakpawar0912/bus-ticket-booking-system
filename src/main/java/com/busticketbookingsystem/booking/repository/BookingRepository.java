package com.busticketbookingsystem.booking.repository;

import com.busticketbookingsystem.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Finds every seat row associated with a specific trip.
     * Member 3's frontend uses this to color the seats Red (Booked) or Green (Available).
     */
    List<Booking> findByTripId(Long tripId);

    /**
     * Finds a specific seat on a specific trip.
     * Your BookingService uses this to double-check if a seat is already in the database
     * before trying to lock it.
     */
    Optional<Booking> findByTripIdAndSeatNumber(Long tripId, Integer seatNumber);
}