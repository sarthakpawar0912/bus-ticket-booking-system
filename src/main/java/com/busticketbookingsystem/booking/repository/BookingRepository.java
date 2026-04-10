package com.busticketbookingsystem.booking.repository;

import com.busticketbookingsystem.booking.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    /**
     * 🌟 THE HERO OF THE MODULE: PESSIMISTIC LOCKING 🌟
     * * This query finds a specific seat for a specific trip.
     * The @Lock(LockModeType.PESSIMISTIC_WRITE) annotation tells MySQL:
     * "Lock this specific row. Do not let anyone else read or update it
     * until my current transaction is completely finished."
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.trip.id = :tripId AND b.seatNumber = :seatNumber")
    Optional<Booking> findByTripIdAndSeatNumberForUpdate(
            @Param("tripId") Long tripId,
            @Param("seatNumber") Integer seatNumber
    );

    /**
     * Utility method to count how many seats are currently available for a trip.
     * (Useful if you want to double-check the 'available_seats' column in the trips table).
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.trip.id = :tripId AND b.status = 'Available'")
    Integer countAvailableSeatsByTripId(@Param("tripId") Long tripId);
}