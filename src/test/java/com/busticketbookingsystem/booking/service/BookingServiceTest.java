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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingService.
 * Tests initiateBooking, cancelBooking, getBookingsForTrip, getBookingById.
 */
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
    private BookingRequestDTO bookingRequest;

    @BeforeEach
    void setUp() {
        trip = Trip.builder()
                .tripId(1).availableSeats(30)
                .fare(new BigDecimal("500.00")).build();

        booking = Booking.builder()
                .bookingId(1).trip(trip).seatNumber(5)
                .status(BookingStatus.Booked).build();

        bookingRequest = BookingRequestDTO.builder()
                .tripId(1).seatNumbers(List.of(5)).customerId(10).build();
    }

    @Nested
    @DisplayName("initiateBooking() Tests")
    class InitiateBookingTests {

        @Test
        @DisplayName("POSITIVE: Should book a single new seat successfully")
        void initiateBooking_SingleNewSeat_Success() {
            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            // Seat 5 has no existing booking
            when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 5)).thenReturn(Optional.empty());
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(tripRepository.save(any(Trip.class))).thenReturn(trip);

            BookingResponseDTO result = bookingService.initiateBooking(bookingRequest);

            assertNotNull(result);
            assertEquals("Booking successful for 1 seat(s)", result.getMessage());
            assertEquals(1, result.getBookingIds().size());
            assertEquals(new BigDecimal("500.00"), result.getTotalFare());
            assertEquals(10, result.getCustomerId());

            // Available seats should be decremented
            assertEquals(29, trip.getAvailableSeats());
        }

        @Test
        @DisplayName("POSITIVE: Should book multiple seats at once")
        void initiateBooking_MultipleSeats_Success() {
            BookingRequestDTO multiSeatRequest = BookingRequestDTO.builder()
                    .tripId(1).seatNumbers(List.of(1, 2, 3)).customerId(10).build();

            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(bookingRepository.findByTrip_TripIdAndSeatNumber(eq(1), anyInt()))
                    .thenReturn(Optional.empty());

            // Return different booking IDs for each seat
            when(bookingRepository.save(any(Booking.class)))
                    .thenReturn(Booking.builder().bookingId(1).trip(trip).seatNumber(1).status(BookingStatus.Booked).build())
                    .thenReturn(Booking.builder().bookingId(2).trip(trip).seatNumber(2).status(BookingStatus.Booked).build())
                    .thenReturn(Booking.builder().bookingId(3).trip(trip).seatNumber(3).status(BookingStatus.Booked).build());
            when(tripRepository.save(any(Trip.class))).thenReturn(trip);

            BookingResponseDTO result = bookingService.initiateBooking(multiSeatRequest);

            assertEquals("Booking successful for 3 seat(s)", result.getMessage());
            assertEquals(3, result.getBookingIds().size());
            // Total fare should be 3 * 500 = 1500
            assertEquals(new BigDecimal("1500.00"), result.getTotalFare());
            // Available seats should be 30 - 3 = 27
            assertEquals(27, trip.getAvailableSeats());
        }

        @Test
        @DisplayName("POSITIVE: Should re-book a previously cancelled (Available) seat")
        void initiateBooking_RebookAvailableSeat() {
            // Seat 5 exists but has status Available (previously cancelled)
            Booking existingAvailable = Booking.builder()
                    .bookingId(100).trip(trip).seatNumber(5)
                    .status(BookingStatus.Available).build();

            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 5))
                    .thenReturn(Optional.of(existingAvailable));
            when(bookingRepository.save(any(Booking.class))).thenReturn(existingAvailable);
            when(tripRepository.save(any(Trip.class))).thenReturn(trip);

            BookingResponseDTO result = bookingService.initiateBooking(bookingRequest);

            assertNotNull(result);
            // The existing booking should be updated to Booked status
            assertEquals(BookingStatus.Booked, existingAvailable.getStatus());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException when seat is already booked")
        void initiateBooking_SeatAlreadyBooked_Throws() {
            // Seat 5 is already booked
            Booking alreadyBooked = Booking.builder()
                    .bookingId(100).trip(trip).seatNumber(5)
                    .status(BookingStatus.Booked).build();

            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 5))
                    .thenReturn(Optional.of(alreadyBooked));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> bookingService.initiateBooking(bookingRequest));

            assertEquals("Seat 5 is already booked for this trip.", ex.getMessage());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when trip not found")
        void initiateBooking_TripNotFound() {
            when(tripRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> bookingService.initiateBooking(bookingRequest));
        }
    }

    @Nested
    @DisplayName("cancelBooking() Tests")
    class CancelBookingTests {

        @Test
        @DisplayName("POSITIVE: Should cancel a booked booking successfully")
        void cancelBooking_Success() {
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(tripRepository.save(any(Trip.class))).thenReturn(trip);

            String result = bookingService.cancelBooking(1);

            assertEquals("Booking with id 1 has been cancelled successfully.", result);
            // Status should change to Available
            assertEquals(BookingStatus.Available, booking.getStatus());
            // Available seats should be incremented
            assertEquals(31, trip.getAvailableSeats());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when trying to cancel an already available booking")
        void cancelBooking_AlreadyAvailable_Throws() {
            Booking availableBooking = Booking.builder()
                    .bookingId(2).trip(trip).seatNumber(10)
                    .status(BookingStatus.Available).build();

            when(bookingRepository.findById(2)).thenReturn(Optional.of(availableBooking));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> bookingService.cancelBooking(2));

            assertEquals("Booking is already available (not booked).", ex.getMessage());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for non-existent booking")
        void cancelBooking_NotFound() {
            when(bookingRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> bookingService.cancelBooking(999));
        }
    }

    @Nested
    @DisplayName("getBookingsForTrip() Tests")
    class GetBookingsForTripTests {

        @Test
        @DisplayName("POSITIVE: Should return bookings for a trip")
        void getBookingsForTrip_ReturnsList() {
            when(bookingRepository.findByTrip_TripId(1)).thenReturn(List.of(booking));

            List<Booking> result = bookingService.getBookingsForTrip(1);

            assertEquals(1, result.size());
            assertEquals(5, result.get(0).getSeatNumber());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no bookings for trip")
        void getBookingsForTrip_Empty() {
            when(bookingRepository.findByTrip_TripId(99)).thenReturn(Collections.emptyList());

            assertTrue(bookingService.getBookingsForTrip(99).isEmpty());
        }
    }

    @Nested
    @DisplayName("getBookingById() Tests")
    class GetBookingByIdTests {

        @Test
        @DisplayName("POSITIVE: Should return booking for valid ID")
        void getBookingById_Success() {
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

            Booking result = bookingService.getBookingById(1);

            assertEquals(1, result.getBookingId());
            assertEquals(BookingStatus.Booked, result.getStatus());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for invalid ID")
        void getBookingById_NotFound() {
            when(bookingRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> bookingService.getBookingById(999));
        }
    }
}
