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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private BookingService bookingService;

    private Trip trip;
    private Booking booking;

    @BeforeEach
    void setUp() {
        trip = Trip.builder()
                .tripId(1)
                .departureTime(LocalDateTime.of(2026, 5, 1, 8, 0))
                .arrivalTime(LocalDateTime.of(2026, 5, 1, 11, 0))
                .availableSeats(40)
                .fare(new BigDecimal("500.00"))
                .tripDate(LocalDateTime.of(2026, 5, 1, 0, 0))
                .build();

        booking = Booking.builder()
                .bookingId(1)
                .trip(trip)
                .seatNumber(5)
                .status(BookingStatus.Booked)
                .build();
    }

    // ==================== initiateBooking ====================

    @Test
    void initiateBooking_success() {
        BookingRequestDTO request = BookingRequestDTO.builder()
                .tripId(1)
                .seatNumbers(List.of(5, 6))
                .customerId(1)
                .build();

        Booking savedBooking1 = Booking.builder().bookingId(1).trip(trip).seatNumber(5).status(BookingStatus.Booked).build();
        Booking savedBooking2 = Booking.builder().bookingId(2).trip(trip).seatNumber(6).status(BookingStatus.Booked).build();

        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 5)).thenReturn(Optional.empty());
        when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 6)).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking1, savedBooking2);
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        BookingResponseDTO result = bookingService.initiateBooking(request);

        assertNotNull(result);
        assertEquals(2, result.getBookingIds().size());
        assertEquals(new BigDecimal("1000.00"), result.getTotalFare());
        assertEquals(1, result.getCustomerId());
        verify(bookingRepository, times(2)).save(any(Booking.class));
    }

    @Test
    void initiateBooking_tripNotFound() {
        BookingRequestDTO request = BookingRequestDTO.builder()
                .tripId(999)
                .seatNumbers(List.of(1))
                .customerId(1)
                .build();

        when(tripRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.initiateBooking(request));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void initiateBooking_seatAlreadyBooked() {
        BookingRequestDTO request = BookingRequestDTO.builder()
                .tripId(1)
                .seatNumbers(List.of(5))
                .customerId(1)
                .build();

        Booking existingBooked = Booking.builder()
                .bookingId(10)
                .trip(trip)
                .seatNumber(5)
                .status(BookingStatus.Booked)
                .build();

        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 5)).thenReturn(Optional.of(existingBooked));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.initiateBooking(request));

        assertEquals("Seat 5 is already booked for this trip.", exception.getMessage());
    }

    @Test
    void initiateBooking_rebookAvailableSeat() {
        BookingRequestDTO request = BookingRequestDTO.builder()
                .tripId(1)
                .seatNumbers(List.of(5))
                .customerId(1)
                .build();

        Booking existingAvailable = Booking.builder()
                .bookingId(10)
                .trip(trip)
                .seatNumber(5)
                .status(BookingStatus.Available)
                .build();

        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 5)).thenReturn(Optional.of(existingAvailable));
        when(bookingRepository.save(any(Booking.class))).thenReturn(existingAvailable);
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        BookingResponseDTO result = bookingService.initiateBooking(request);

        assertNotNull(result);
        assertEquals(BookingStatus.Booked, existingAvailable.getStatus());
    }

    // ==================== cancelBooking ====================

    @Test
    void cancelBooking_success() {
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        String result = bookingService.cancelBooking(1);

        assertEquals("Booking with id 1 has been cancelled successfully.", result);
        assertEquals(BookingStatus.Available, booking.getStatus());
        assertEquals(41, trip.getAvailableSeats());
    }

    @Test
    void cancelBooking_notFound() {
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.cancelBooking(999));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelBooking_alreadyAvailable() {
        Booking availableBooking = Booking.builder()
                .bookingId(2)
                .trip(trip)
                .seatNumber(10)
                .status(BookingStatus.Available)
                .build();

        when(bookingRepository.findById(2)).thenReturn(Optional.of(availableBooking));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.cancelBooking(2));

        assertEquals("Booking is already available (not booked).", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    // ==================== getBookingsForTrip ====================

    @Test
    void getBookingsForTrip_success() {
        Booking booking2 = Booking.builder().bookingId(2).trip(trip).seatNumber(6).status(BookingStatus.Booked).build();
        when(bookingRepository.findByTrip_TripId(1)).thenReturn(List.of(booking, booking2));

        List<Booking> result = bookingService.getBookingsForTrip(1);

        assertEquals(2, result.size());
    }

    @Test
    void getBookingsForTrip_empty() {
        when(bookingRepository.findByTrip_TripId(999)).thenReturn(Collections.emptyList());

        List<Booking> result = bookingService.getBookingsForTrip(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void getBookingsForTrip_mixedStatuses() {
        Booking available = Booking.builder().bookingId(3).trip(trip).seatNumber(7).status(BookingStatus.Available).build();
        when(bookingRepository.findByTrip_TripId(1)).thenReturn(List.of(booking, available));

        List<Booking> result = bookingService.getBookingsForTrip(1);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getStatus() == BookingStatus.Booked));
        assertTrue(result.stream().anyMatch(b -> b.getStatus() == BookingStatus.Available));
    }

    // ==================== getBookingById ====================

    @Test
    void getBookingById_success() {
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        Booking result = bookingService.getBookingById(1);

        assertNotNull(result);
        assertEquals(1, result.getBookingId());
        assertEquals(5, result.getSeatNumber());
    }

    @Test
    void getBookingById_notFound() {
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.getBookingById(999));

        assertEquals("Booking not found with id: 999", exception.getMessage());
    }

    @Test
    void getBookingById_withNullId() {
        when(bookingRepository.findById(null)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.getBookingById(null));
    }
}
