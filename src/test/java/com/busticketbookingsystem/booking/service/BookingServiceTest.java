package com.busticketbookingsystem.booking.service;

import com.busticketbookingsystem.booking.dto.BookingRequestDTO;
import com.busticketbookingsystem.booking.dto.BookingResponseDTO;
import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.entity.BookingStatus;
import com.busticketbookingsystem.booking.repository.BookingRepository;
import com.busticketbookingsystem.exception.BadRequestException;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.trip.entity.Route;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingService. Covers every public method with positive,
 * negative, and edge-case scenarios.
 *
 * Schema note: bookings table has columns (booking_id, trip_id, seat_number,
 * status ENUM('Available','Booked')). There is NO customer_id column and
 * NO PAID enum value — tests reflect that.
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private TripRepository tripRepository;

    @InjectMocks private BookingService bookingService;

    private Trip trip;
    private Booking booking;
    private BookingRequestDTO bookingRequest;

    @BeforeEach
    void setUp() {
        Route route = Route.builder().routeId(1).fromCity("Mumbai").toCity("Pune").build();
        trip = Trip.builder()
                .tripId(1).availableSeats(30)
                .fare(new BigDecimal("500.00"))
                .route(route)
                .tripDate(LocalDateTime.of(2026, 5, 1, 8, 0))
                .build();

        booking = Booking.builder()
                .bookingId(1).trip(trip).seatNumber(5)
                .status(BookingStatus.Booked).build();

        bookingRequest = BookingRequestDTO.builder()
                .tripId(1).seatNumbers(List.of(5)).customerId(10).build();
    }

    // ============================================================
    // initiateBooking()
    // ============================================================
    @Nested
    @DisplayName("initiateBooking()")
    class InitiateBooking {

        @Test
        @DisplayName("POSITIVE: books a single new seat and decrements availableSeats")
        void singleNewSeat() {
            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 5)).thenReturn(Optional.empty());
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

            BookingResponseDTO result = bookingService.initiateBooking(bookingRequest);

            assertEquals("Booking successful for 1 seat(s)", result.getMessage());
            assertEquals(1, result.getBookingIds().size());
            assertEquals(new BigDecimal("500.00"), result.getTotalFare());
            assertEquals(10, result.getCustomerId());
            assertEquals(29, trip.getAvailableSeats());

            ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
            verify(bookingRepository).save(captor.capture());
            assertEquals(BookingStatus.Booked, captor.getValue().getStatus());
            verify(tripRepository).save(trip);
        }

        @Test
        @DisplayName("POSITIVE: books 3 seats, total fare = 3 x perSeat, decrements availableSeats by 3")
        void multipleSeats() {
            BookingRequestDTO multi = BookingRequestDTO.builder()
                    .tripId(1).seatNumbers(List.of(1, 2, 3)).customerId(10).build();

            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(bookingRepository.findByTrip_TripIdAndSeatNumber(eq(1), anyInt()))
                    .thenReturn(Optional.empty());
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> {
                        Booking b = inv.getArgument(0);
                        b.setBookingId(b.getSeatNumber() * 10);
                        return b;
                    });

            BookingResponseDTO result = bookingService.initiateBooking(multi);

            assertEquals(3, result.getBookingIds().size());
            assertEquals(new BigDecimal("1500.00"), result.getTotalFare());
            assertEquals(27, trip.getAvailableSeats());
            verify(bookingRepository, times(3)).save(any(Booking.class));
        }

        @Test
        @DisplayName("POSITIVE: re-books a seat that was previously cancelled (status = Available)")
        void rebookAvailableSeat() {
            Booking existing = Booking.builder()
                    .bookingId(100).trip(trip).seatNumber(5)
                    .status(BookingStatus.Available).build();

            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 5))
                    .thenReturn(Optional.of(existing));
            when(bookingRepository.save(existing)).thenReturn(existing);

            BookingResponseDTO result = bookingService.initiateBooking(bookingRequest);

            assertNotNull(result);
            assertEquals(BookingStatus.Booked, existing.getStatus());
        }

        @Test
        @DisplayName("NEGATIVE: throws ResourceNotFoundException when trip does not exist")
        void tripNotFound() {
            when(tripRepository.findById(1)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> bookingService.initiateBooking(bookingRequest));
            assertTrue(ex.getMessage().contains("Trip not found"));
        }

        @Test
        @DisplayName("NEGATIVE: throws BadRequestException when trip.fare is null")
        void tripFareNull() {
            trip.setFare(null);
            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> bookingService.initiateBooking(bookingRequest));
            assertTrue(ex.getMessage().contains("no fare configured"));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("NEGATIVE: throws BadRequestException when seat is already Booked")
        void seatAlreadyBooked() {
            Booking existing = Booking.builder()
                    .bookingId(100).trip(trip).seatNumber(5)
                    .status(BookingStatus.Booked).build();

            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 5))
                    .thenReturn(Optional.of(existing));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> bookingService.initiateBooking(bookingRequest));
            assertEquals("Seat 5 is already booked for this trip.", ex.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("EDGE: partial failure on 2nd seat — trip is not saved when any seat fails")
        void partialFailureOnSecondSeat() {
            BookingRequestDTO multi = BookingRequestDTO.builder()
                    .tripId(1).seatNumbers(List.of(1, 2)).customerId(10).build();

            Booking seat2Taken = Booking.builder()
                    .bookingId(999).trip(trip).seatNumber(2)
                    .status(BookingStatus.Booked).build();

            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 1)).thenReturn(Optional.empty());
            when(bookingRepository.findByTrip_TripIdAndSeatNumber(1, 2)).thenReturn(Optional.of(seat2Taken));
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> {
                        Booking b = inv.getArgument(0);
                        b.setBookingId(42);
                        return b;
                    });

            assertThrows(BadRequestException.class,
                    () -> bookingService.initiateBooking(multi));
            // Seat 1 was saved before the failure. In production the
            // @Transactional rollback would undo it; here we at least confirm
            // seat 2 was never saved and trip.save() was never invoked.
            verify(bookingRepository, times(1)).save(any(Booking.class));
            verify(tripRepository, never()).save(any());
        }
    }

    // ============================================================
    // getBookingsForTrip()
    // ============================================================
    @Nested
    @DisplayName("getBookingsForTrip()")
    class GetBookingsForTrip {

        @Test
        @DisplayName("POSITIVE: returns bookings for a trip")
        void returnsList() {
            when(bookingRepository.findByTrip_TripId(1)).thenReturn(List.of(booking));

            List<Booking> result = bookingService.getBookingsForTrip(1);

            assertEquals(1, result.size());
            assertEquals(5, result.get(0).getSeatNumber());
        }

        @Test
        @DisplayName("EDGE: returns empty list when no bookings exist for the trip")
        void emptyList() {
            when(bookingRepository.findByTrip_TripId(99)).thenReturn(Collections.emptyList());

            assertTrue(bookingService.getBookingsForTrip(99).isEmpty());
        }
    }

    // ============================================================
    // getBookingById()
    // ============================================================
    @Nested
    @DisplayName("getBookingById()")
    class GetBookingById {

        @Test
        @DisplayName("POSITIVE: returns booking for a valid ID")
        void found() {
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

            Booking result = bookingService.getBookingById(1);

            assertEquals(1, result.getBookingId());
            assertEquals(BookingStatus.Booked, result.getStatus());
        }

        @Test
        @DisplayName("NEGATIVE: throws ResourceNotFoundException for invalid ID")
        void notFound() {
            when(bookingRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> bookingService.getBookingById(999));
        }
    }

    // ============================================================
    // buildConfirmationContext()
    // ============================================================
    @Nested
    @DisplayName("buildConfirmationContext()")
    class BuildConfirmationContext {

        @Test
        @DisplayName("POSITIVE: returns single-booking context with trip data populated")
        void singleBooking() {
            when(bookingRepository.findByIdWithTripDetails(1)).thenReturn(Optional.of(booking));

            BookingService.ConfirmationContext ctx = bookingService.buildConfirmationContext(1);

            assertEquals(List.of(1), ctx.bookingIds());
            assertEquals(List.of(5), ctx.seatNumbers());
            assertEquals(new BigDecimal("500.00"), ctx.totalFare());
            assertEquals("Mumbai", ctx.fromCity());
            assertEquals("Pune", ctx.toCity());
            assertNotNull(ctx.tripDate());
            assertNull(ctx.customerId());    // schema has no customer FK on bookings
            assertNull(ctx.customerName());
        }

        @Test
        @DisplayName("EDGE: booking with null trip — totalFare and trip fields are null, no NPE")
        void nullTrip() {
            Booking legacy = Booking.builder().bookingId(1).trip(null)
                    .seatNumber(5).status(BookingStatus.Booked).build();

            when(bookingRepository.findByIdWithTripDetails(1)).thenReturn(Optional.of(legacy));

            BookingService.ConfirmationContext ctx = bookingService.buildConfirmationContext(1);

            assertEquals(List.of(1), ctx.bookingIds());
            assertEquals(List.of(5), ctx.seatNumbers());
            assertNull(ctx.totalFare());
            assertNull(ctx.fromCity());
            assertNull(ctx.toCity());
            assertNull(ctx.tripDate());
        }

        @Test
        @DisplayName("EDGE: booking with trip but no route — city fields null, fare still populated")
        void nullRoute() {
            Trip tripNoRoute = Trip.builder().tripId(2)
                    .fare(new BigDecimal("300.00")).route(null).build();
            Booking b = Booking.builder().bookingId(1).trip(tripNoRoute)
                    .seatNumber(5).status(BookingStatus.Booked).build();

            when(bookingRepository.findByIdWithTripDetails(1)).thenReturn(Optional.of(b));

            BookingService.ConfirmationContext ctx = bookingService.buildConfirmationContext(1);

            assertEquals(new BigDecimal("300.00"), ctx.totalFare());
            assertNull(ctx.fromCity());
            assertNull(ctx.toCity());
        }

        @Test
        @DisplayName("NEGATIVE: throws ResourceNotFoundException for invalid booking ID")
        void notFound() {
            when(bookingRepository.findByIdWithTripDetails(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> bookingService.buildConfirmationContext(999));
        }
    }
}
