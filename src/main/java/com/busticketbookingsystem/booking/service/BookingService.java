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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;

    @Transactional
    public BookingResponseDTO initiateBooking(BookingRequestDTO request) {

        validateSeatNumbers(request.getSeatNumbers());

        Trip trip = getValidatedTrip(request.getTripId(), request.getSeatNumbers().size());

        List<Integer> bookingIds = new ArrayList<>();
        BigDecimal totalFare = BigDecimal.ZERO;

        for (Integer seatNumber : request.getSeatNumbers()) {
            Booking booking = createOrUpdateBooking(request, trip, seatNumber);
            bookingIds.add(booking.getBookingId());
            totalFare = totalFare.add(trip.getFare());

            trip.setAvailableSeats(trip.getAvailableSeats() - 1);
        }

        tripRepository.save(trip);

        log.info("Booked {} seat(s) on trip {} for customer {} — booking ids {}",
                request.getSeatNumbers().size(),
                trip.getTripId(),
                request.getCustomerId(),
                bookingIds);

        return buildResponse(request, bookingIds, totalFare);
    }

    // ================== VALIDATION METHODS ==================

    private void validateSeatNumbers(List<Integer> seatNumbers) {
        Set<Integer> uniqueSeats = new HashSet<>(seatNumbers);

        if (uniqueSeats.size() != seatNumbers.size()) {
            throw new BadRequestException("Duplicate seat numbers in request.");
        }

        for (Integer seat : seatNumbers) {
            if (seat == null || seat <= 0) {
                throw new BadRequestException("Seat number must be a positive integer.");
            }
        }
    }

    private Trip getValidatedTrip(Integer tripId, int requestedSeats) {
        Trip trip = tripRepository.findByIdForUpdate(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        if (trip.getTripDate() != null && trip.getTripDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot book a trip that has already departed.");
        }

        if (trip.getFare() == null) {
            throw new BadRequestException("Trip " + trip.getTripId() + " has no fare configured.");
        }

        Integer available = trip.getAvailableSeats();
        if (available == null || available < requestedSeats) {
            throw new BadRequestException("Only " + (available == null ? 0 : available)
                    + " seat(s) available; cannot book " + requestedSeats + ".");
        }

        return trip;
    }

    // ================== CORE LOGIC ==================

    private Booking createOrUpdateBooking(BookingRequestDTO request, Trip trip, Integer seatNumber) {

        Optional<Booking> existingBooking =
                bookingRepository.findByTrip_TripIdAndSeatNumber(request.getTripId(), seatNumber);

        if (existingBooking.isPresent() &&
                existingBooking.get().getStatus() == BookingStatus.Booked) {
            throw new BadRequestException("Seat " + seatNumber + " is already booked for this trip.");
        }

        Booking booking = existingBooking.orElseGet(() ->
                Booking.builder()
                        .trip(trip)
                        .seatNumber(seatNumber)
                        .status(BookingStatus.Booked)
                        .build()
        );

        booking.setStatus(BookingStatus.Booked);

        return bookingRepository.save(booking);
    }

    private BookingResponseDTO buildResponse(BookingRequestDTO request,
                                             List<Integer> bookingIds,
                                             BigDecimal totalFare) {

        return BookingResponseDTO.builder()
                .message("Booking successful for " + request.getSeatNumbers().size() + " seat(s)")
                .bookingIds(bookingIds)
                .totalFare(totalFare)
                .customerId(request.getCustomerId())
                .build();
    }

    // ================== OTHER METHODS (UNCHANGED) ==================

    @Transactional(readOnly = true)
    public List<Booking> getBookingsForTrip(Integer tripId) {
        return bookingRepository.findByTrip_TripId(tripId);
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Integer id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

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