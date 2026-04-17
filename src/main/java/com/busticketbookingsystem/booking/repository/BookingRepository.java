package com.busticketbookingsystem.booking.repository;

import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByTrip_TripId(Integer tripId);

    List<Booking> findByTrip_TripIdAndStatus(Integer tripId, BookingStatus status);

    Optional<Booking> findByTrip_TripIdAndSeatNumber(Integer tripId, Integer seatNumber);

    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.trip t " +
           "LEFT JOIN FETCH t.route " +
           "LEFT JOIN FETCH t.bus " +
           "LEFT JOIN FETCH t.boardingAddress " +
           "LEFT JOIN FETCH t.droppingAddress " +
           "WHERE b.bookingId = :bookingId")
    Optional<Booking> findByIdWithTripDetails(@Param("bookingId") Integer bookingId);
}
