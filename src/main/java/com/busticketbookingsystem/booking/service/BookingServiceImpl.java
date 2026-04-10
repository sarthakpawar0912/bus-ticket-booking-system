package com.busticketbookingsystem.booking.service;

import com.busticketbookingsystem.booking.dto.BookingRequestDTO;
import com.busticketbookingsystem.booking.dto.BookingResponseDTO;
import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.entity.BookingStatus;
import com.busticketbookingsystem.booking.exception.BookingNotFoundException;
import com.busticketbookingsystem.booking.exception.SeatAlreadyBookedException;
import com.busticketbookingsystem.booking.repository.BookingRepository;
import com.busticketbookingsystem.customer.repository.CustomerRepository;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.exception.TripNotFoundException;
import com.busticketbookingsystem.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final CustomerRepository customerRepository;

    // Constructor Injection (Best Practice)
    public BookingServiceImpl(
            BookingRepository bookingRepository,
            TripRepository tripRepository,
            CustomerRepository customerRepository) {
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional // CRITICAL: This ensures the lock is held until the entire method finishes
    public BookingResponseDTO reserveSeat(BookingRequestDTO request) {

        // 1. Verify the Customer exists (Optional, but good for data integrity)
        boolean customerExists = customerRepository.existsById(request.customerId());
        if (!customerExists) {
            throw new IllegalArgumentException("Customer not found with ID: " + request.customerId());
        }

        // 2. Verify the Trip exists
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new TripNotFoundException(request.tripId()));

        // 3. 🌟 THE PESSIMISTIC LOCK 🌟
        // Fetch the specific seat for this trip and LOCK it so no one else can touch it.
        Booking seat = bookingRepository.findByTripIdAndSeatNumberForUpdate(trip.getId(), request.seatNumber())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seat " + request.seatNumber() + " does not exist for Trip " + trip.getId()));

        // 4. Check if someone else just bought it
        if (seat.getStatus() == BookingStatus.Booked) {
            throw new SeatAlreadyBookedException(request.seatNumber(), trip.getId());
        }

        // 5. Reserve the seat!
        seat.setStatus(BookingStatus.Booked);

        // 6. Update the available seats count in the Trip table
        if (trip.getAvailableSeats() > 0) {
            trip.setAvailableSeats(trip.getAvailableSeats() - 1);
            tripRepository.save(trip);
        } else {
            throw new IllegalStateException("Trip is completely full!");
        }

        // 7. Save the updated seat to the database
        Booking savedBooking = bookingRepository.save(seat);

        // 8. Return the "Receipt" to the frontend
        return toResponseDto(savedBooking, trip);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        return toResponseDto(booking, booking.getTrip());
    }

    // --- Helper Method ---
    // Converts the raw database entity into the clean DTO for the frontend
    private BookingResponseDTO toResponseDto(Booking booking, Trip trip) {
        return new BookingResponseDTO(
                booking.getBookingId(),
                trip.getId(),
                booking.getSeatNumber(),
                booking.getStatus().name(),
                trip.getFare() // Passing the price directly from the Trip table
        );
    }
}