package com.busticketbookingsystem.booking.service;

import com.busticketbookingsystem.booking.dto.BookingRequestDTO;
import com.busticketbookingsystem.booking.dto.BookingResponseDTO;
import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.entity.BookingStatus;
import com.busticketbookingsystem.booking.repository.BookingRepository;

import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.repository.TripRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TripRepository tripRepository;

    // 1. Fetch booked seats for the frontend grid
    public List<Booking> getBookedSeatsForTrip(Long tripId) {
        return bookingRepository.findByTripId(tripId);
    }

    // 2. Initiate the booking (Lock seats)
    @Transactional
    public BookingResponseDTO initiateBooking(BookingRequestDTO request) {
        // Fetch the trip to get the fare and check capacity
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found!"));

        if (trip.getAvailableSeats() < request.getSeatNumbers().size()) {
            throw new RuntimeException("Not enough available seats on this trip!");
        }

        List<Long> bookedIds = new ArrayList<>();

        for (Integer seatNum : request.getSeatNumbers()) {
            // Check if this specific seat is already mapped in the database
            Booking existingBooking = bookingRepository.findByTripIdAndSeatNumber(trip.getTripId(), seatNum).orElse(null);

            if (existingBooking != null && existingBooking.getStatus() == BookingStatus.Booked) {
                throw new RuntimeException("Seat " + seatNum + " is already booked!");
            }

            Booking booking;
            if (existingBooking != null) {
                // If it exists but was cancelled ("Available"), update it to "Booked"
                booking = existingBooking;
                booking.setStatus(BookingStatus.Booked);
            } else {
                // If it doesn't exist in the DB yet, create it
                booking = new Booking();
                booking.setTripId(trip.getTripId());
                booking.setSeatNumber(seatNum);
                booking.setStatus(BookingStatus.Booked);
            }

            Booking savedBooking = bookingRepository.save(booking);
            bookedIds.add(savedBooking.getBookingId());
        }

        // Deduct the booked seats from the Trip's available_seats column
        trip.setAvailableSeats(trip.getAvailableSeats() - request.getSeatNumbers().size());
        tripRepository.save(trip);

        // Calculate total fare based on the DB schema (fare * number of seats)
        Double totalFare = trip.getFare() * request.getSeatNumbers().size();

        return new BookingResponseDTO("Seats successfully locked. Proceed to payment.", bookedIds, totalFare);
    }

    // 3. Cancel a booking
    @Transactional
    public String cancelBooking(Long tripId, List<Integer> seatNumbers) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found!"));

        for (Integer seatNum : seatNumbers) {
            Booking booking = bookingRepository.findByTripIdAndSeatNumber(tripId, seatNum)
                    .orElseThrow(() -> new RuntimeException("Booking for seat " + seatNum + " not found!"));

            // Revert status to Available
            booking.setStatus(BookingStatus.Available);
            bookingRepository.save(booking);
        }

        // Add the seats back to the Trip's available capacity
        trip.setAvailableSeats(trip.getAvailableSeats() + seatNumbers.size());
        tripRepository.save(trip);

        return "Booking cancelled successfully. Seats are now available.";
    }
}