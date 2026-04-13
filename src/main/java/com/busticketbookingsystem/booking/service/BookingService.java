package com.busticketbookingsystem.booking.service;

import com.busticketbookingsystem.booking.dto.BookingRequestDTO;
import com.busticketbookingsystem.booking.dto.BookingResponseDTO;
import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.entity.BookingStatus;
import com.busticketbookingsystem.booking.repository.BookingRepository;
import com.busticketbookingsystem.exception.BadRequestException;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;

    @Transactional
    public BookingResponseDTO initiateBooking(BookingRequestDTO request) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + request.getTripId()));

        List<Integer> bookingIds = new ArrayList<>();
        BigDecimal totalFare = BigDecimal.ZERO;

        for (Integer seatNumber : request.getSeatNumbers()) {
            Optional<Booking> existingBooking = bookingRepository.findByTrip_TripIdAndSeatNumber(
                    request.getTripId(), seatNumber);

            if (existingBooking.isPresent() && existingBooking.get().getStatus() == BookingStatus.Booked) {
                throw new BadRequestException("Seat " + seatNumber + " is already booked for this trip.");
            }

            Booking booking;
            if (existingBooking.isPresent()) {
                booking = existingBooking.get();
                booking.setStatus(BookingStatus.Booked);
            } else {
                booking = Booking.builder()
                        .trip(trip)
                        .seatNumber(seatNumber)
                        .status(BookingStatus.Booked)
                        .build();
            }

            Booking saved = bookingRepository.save(booking);
            bookingIds.add(saved.getBookingId());
            totalFare = totalFare.add(trip.getFare());

            trip.setAvailableSeats(trip.getAvailableSeats() - 1);
        }

        tripRepository.save(trip);

        return BookingResponseDTO.builder()
                .message("Booking successful for " + request.getSeatNumbers().size() + " seat(s)")
                .bookingIds(bookingIds)
                .totalFare(totalFare)
                .customerId(request.getCustomerId())
                .build();
    }

    @Transactional
    public String cancelBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.Available) {
            throw new BadRequestException("Booking is already available (not booked).");
        }

        booking.setStatus(BookingStatus.Available);
        bookingRepository.save(booking);

        Trip trip = booking.getTrip();
        trip.setAvailableSeats(trip.getAvailableSeats() + 1);
        tripRepository.save(trip);

        return "Booking with id " + bookingId + " has been cancelled successfully.";
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsForTrip(Integer tripId) {
        return bookingRepository.findByTrip_TripId(tripId);
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Integer id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }
}
