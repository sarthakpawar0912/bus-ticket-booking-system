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

        if (trip.getFare() == null) {
            throw new BadRequestException("Trip " + trip.getTripId() + " has no fare configured.");
        }

        List<Integer> bookingIds = new ArrayList<>();
        BigDecimal perSeatFare = trip.getFare();
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
            totalFare = totalFare.add(perSeatFare);

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

    @Transactional(readOnly = true)
    public List<Booking> getBookingsForTrip(Integer tripId) {
        return bookingRepository.findByTrip_TripId(tripId);
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Integer id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    /**
     * Rebuild the confirmation page context from the database when the original
     * POST flash attributes are gone (e.g. user opens the confirmation URL
     * directly). The schema does not link bookings to customers, so only the
     * single requested booking is represented here.
     */
    @Transactional(readOnly = true)
    public ConfirmationContext buildConfirmationContext(Integer bookingId) {
        Booking b = bookingRepository.findByIdWithTripDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        BigDecimal totalFare = b.getTrip() != null ? b.getTrip().getFare() : null;
        String fromCity = (b.getTrip() != null && b.getTrip().getRoute() != null)
                ? b.getTrip().getRoute().getFromCity() : null;
        String toCity = (b.getTrip() != null && b.getTrip().getRoute() != null)
                ? b.getTrip().getRoute().getToCity() : null;
        String tripDate = (b.getTrip() != null && b.getTrip().getTripDate() != null)
                ? b.getTrip().getTripDate().toLocalDate().toString() : null;

        return new ConfirmationContext(
                List.of(b.getBookingId()),
                List.of(b.getSeatNumber()),
                totalFare,
                null,
                null,
                fromCity,
                toCity,
                tripDate);
    }

    public record ConfirmationContext(
            List<Integer> bookingIds,
            List<Integer> seatNumbers,
            BigDecimal totalFare,
            Integer customerId,
            String customerName,
            String fromCity,
            String toCity,
            String tripDate
    ) {}
}
