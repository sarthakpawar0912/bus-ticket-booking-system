package com.busticketbookingsystem.payment.service;

import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.entity.BookingStatus;
import com.busticketbookingsystem.booking.repository.BookingRepository;
import com.busticketbookingsystem.customer.entity.Customer;
import com.busticketbookingsystem.customer.repository.CustomerRepository;
import com.busticketbookingsystem.exception.BadRequestException;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.payment.dto.PaymentRequestDTO;
import com.busticketbookingsystem.payment.dto.PaymentResponseDTO;
import com.busticketbookingsystem.payment.entity.Payment;
import com.busticketbookingsystem.payment.entity.PaymentStatus;
import com.busticketbookingsystem.payment.repository.PaymentRepository;
import com.busticketbookingsystem.trip.entity.Trip;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService. Covers every public method with positive,
 * negative, and edge-case scenarios, including the new
 * processPaymentsForBookings group flow and strict amount/double-pay
 * validation.
 *
 * Schema note: the double-pay guard is Payment-row existence for a booking
 * (bookings table has no PAID status). Tests reflect that.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks private PaymentService paymentService;

    private Trip trip;
    private Booking booking;
    private Customer customer;
    private Payment payment;
    private PaymentRequestDTO paymentRequest;

    @BeforeEach
    void setUp() {
        trip = Trip.builder()
                .tripId(1).availableSeats(30)
                .fare(new BigDecimal("500.00")).build();

        booking = Booking.builder()
                .bookingId(1).trip(trip).seatNumber(5)
                .status(BookingStatus.Booked).build();

        customer = Customer.builder()
                .customerId(1).name("Test Customer")
                .email("test@email.com").phone("9876543210").build();

        payment = Payment.builder()
                .paymentId(1).booking(booking).customer(customer)
                .amount(new BigDecimal("500.00"))
                .paymentDate(LocalDateTime.of(2026, 4, 15, 10, 0))
                .paymentStatus(PaymentStatus.Success).build();

        paymentRequest = new PaymentRequestDTO(1, 1, new BigDecimal("500.00"));
    }

    // ============================================================
    // processPayment()
    // ============================================================
    @Nested
    @DisplayName("processPayment()")
    class ProcessPayment {

        @Test
        @DisplayName("POSITIVE: processes payment and saves Payment row with correct fields")
        void success() {
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.empty());
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            PaymentResponseDTO result = paymentService.processPayment(paymentRequest);

            assertEquals(1, result.getPaymentId());
            assertEquals(1, result.getBookingId());
            assertEquals(1, result.getCustomerId());
            assertEquals(new BigDecimal("500.00"), result.getAmount());
            assertEquals(PaymentStatus.Success, result.getPaymentStatus());
            assertEquals("Payment processed successfully", result.getMessage());

            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(captor.capture());
            assertEquals(PaymentStatus.Success, captor.getValue().getPaymentStatus());
            assertEquals(customer, captor.getValue().getCustomer());
            assertEquals(booking, captor.getValue().getBooking());
        }

        @Test
        @DisplayName("NEGATIVE: null amount -> BadRequestException")
        void nullAmount() {
            paymentRequest.setAmount(null);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(paymentRequest));
            assertTrue(ex.getMessage().contains("greater than zero"));
            verifyNoInteractions(bookingRepository, paymentRepository, customerRepository);
        }

        @Test
        @DisplayName("NEGATIVE: zero amount -> BadRequestException")
        void zeroAmount() {
            paymentRequest.setAmount(BigDecimal.ZERO);

            assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(paymentRequest));
            verifyNoInteractions(bookingRepository, paymentRepository);
        }

        @Test
        @DisplayName("NEGATIVE: negative amount -> BadRequestException")
        void negativeAmount() {
            paymentRequest.setAmount(new BigDecimal("-100.00"));

            assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(paymentRequest));
            verifyNoInteractions(bookingRepository, paymentRepository);
        }

        @Test
        @DisplayName("NEGATIVE: booking not found -> ResourceNotFoundException")
        void bookingNotFound() {
            when(bookingRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.processPayment(paymentRequest));
            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("NEGATIVE: Payment row already exists for booking -> BadRequestException")
        void paymentAlreadyExists() {
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.of(payment));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(paymentRequest));
            assertTrue(ex.getMessage().contains("already paid"));
            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("NEGATIVE: booking's trip has null fare -> BadRequestException")
        void tripFareNull() {
            trip.setFare(null);
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.empty());

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(paymentRequest));
            assertTrue(ex.getMessage().contains("Trip fare is not configured"));
        }

        @Test
        @DisplayName("NEGATIVE: amount does not match trip.fare -> BadRequestException")
        void amountMismatch() {
            paymentRequest.setAmount(new BigDecimal("450.00"));
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.empty());

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(paymentRequest));
            assertTrue(ex.getMessage().contains("does not match"));
            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("EDGE: amount with different scale but same value (500 vs 500.00) is accepted")
        void amountScaleTolerance() {
            paymentRequest.setAmount(new BigDecimal("500"));
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.empty());
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            assertDoesNotThrow(() -> paymentService.processPayment(paymentRequest));
        }

        @Test
        @DisplayName("NEGATIVE: customer not found -> ResourceNotFoundException")
        void customerNotFound() {
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.empty());
            when(customerRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.processPayment(paymentRequest));
            verify(paymentRepository, never()).save(any());
        }
    }

    // ============================================================
    // processPaymentsForBookings()
    // ============================================================
    @Nested
    @DisplayName("processPaymentsForBookings()")
    class ProcessPaymentsForBookings {

        @Test
        @DisplayName("POSITIVE: single booking — one Payment saved")
        void singleBooking() {
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.empty());
            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(inv -> {
                        Payment p = inv.getArgument(0);
                        p.setPaymentId(1);
                        return p;
                    });

            List<PaymentResponseDTO> result = paymentService.processPaymentsForBookings(
                    List.of(1), 1, new BigDecimal("500.00"));

            assertEquals(1, result.size());
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }

        @Test
        @DisplayName("POSITIVE: 3 bookings — 3 Payment rows with the same paymentDate and per-seat amount")
        void threeBookingsSharedTimestamp() {
            Booking b1 = Booking.builder().bookingId(1).trip(trip).seatNumber(1)
                    .status(BookingStatus.Booked).build();
            Booking b2 = Booking.builder().bookingId(2).trip(trip).seatNumber(2)
                    .status(BookingStatus.Booked).build();
            Booking b3 = Booking.builder().bookingId(3).trip(trip).seatNumber(3)
                    .status(BookingStatus.Booked).build();

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(bookingRepository.findById(1)).thenReturn(Optional.of(b1));
            when(bookingRepository.findById(2)).thenReturn(Optional.of(b2));
            when(bookingRepository.findById(3)).thenReturn(Optional.of(b3));
            when(paymentRepository.findByBooking_BookingId(any())).thenReturn(Optional.empty());
            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(inv -> {
                        Payment p = inv.getArgument(0);
                        p.setPaymentId(p.getBooking().getBookingId() * 100);
                        return p;
                    });

            List<PaymentResponseDTO> result = paymentService.processPaymentsForBookings(
                    List.of(1, 2, 3), 1, new BigDecimal("1500.00"));

            assertEquals(3, result.size());
            verify(paymentRepository, times(3)).save(any(Payment.class));

            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository, times(3)).save(captor.capture());
            LocalDateTime first = captor.getAllValues().get(0).getPaymentDate();
            assertNotNull(first);
            for (Payment p : captor.getAllValues()) {
                assertEquals(first, p.getPaymentDate());
                assertEquals(new BigDecimal("500.00"), p.getAmount());
            }
        }

        @Test
        @DisplayName("NEGATIVE: null bookingIds -> BadRequestException")
        void nullBookingIds() {
            BigDecimal amt = new BigDecimal("500");
            assertThrows(BadRequestException.class,
                    () -> paymentService.processPaymentsForBookings(null, 1, amt));
            verifyNoInteractions(customerRepository, bookingRepository, paymentRepository);
        }

        @Test
        @DisplayName("NEGATIVE: empty bookingIds -> BadRequestException")
        void emptyBookingIds() {
            List<Integer> ids = List.of();
            BigDecimal amt = new BigDecimal("500");
            assertThrows(BadRequestException.class,
                    () -> paymentService.processPaymentsForBookings(ids, 1, amt));
        }

        @Test
        @DisplayName("NEGATIVE: null total amount -> BadRequestException")
        void nullAmount() {
            List<Integer> ids = List.of(1);
            assertThrows(BadRequestException.class,
                    () -> paymentService.processPaymentsForBookings(ids, 1, null));
        }

        @Test
        @DisplayName("NEGATIVE: zero total amount -> BadRequestException")
        void zeroAmount() {
            List<Integer> ids = List.of(1);
            assertThrows(BadRequestException.class,
                    () -> paymentService.processPaymentsForBookings(ids, 1, BigDecimal.ZERO));
        }

        @Test
        @DisplayName("NEGATIVE: negative total amount -> BadRequestException")
        void negativeAmount() {
            List<Integer> ids = List.of(1);
            BigDecimal amt = new BigDecimal("-10");
            assertThrows(BadRequestException.class,
                    () -> paymentService.processPaymentsForBookings(ids, 1, amt));
        }

        @Test
        @DisplayName("NEGATIVE: customer not found -> ResourceNotFoundException")
        void customerNotFound() {
            when(customerRepository.findById(99)).thenReturn(Optional.empty());
            List<Integer> ids = List.of(1);
            BigDecimal amt = new BigDecimal("500.00");

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.processPaymentsForBookings(ids, 99, amt));
            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("NEGATIVE: one booking in the list not found -> ResourceNotFoundException")
        void bookingInListNotFound() {
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.empty());
            when(bookingRepository.findById(2)).thenReturn(Optional.empty());
            List<Integer> ids = List.of(1, 2);
            BigDecimal amt = new BigDecimal("1000.00");

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.processPaymentsForBookings(ids, 1, amt));
            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("NEGATIVE: Payment row already exists for one of the bookings -> BadRequestException")
        void paymentAlreadyExistsForOne() {
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.of(payment));
            List<Integer> ids = List.of(1);
            BigDecimal amt = new BigDecimal("500.00");

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPaymentsForBookings(ids, 1, amt));
            assertTrue(ex.getMessage().contains("already paid"));
            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("NEGATIVE: booking has no trip fare -> BadRequestException")
        void bookingTripFareNull() {
            Trip noFare = Trip.builder().tripId(2).fare(null).build();
            Booking b = Booking.builder().bookingId(1).trip(noFare).seatNumber(5)
                    .status(BookingStatus.Booked).build();

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(bookingRepository.findById(1)).thenReturn(Optional.of(b));
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.empty());
            List<Integer> ids = List.of(1);
            BigDecimal amt = new BigDecimal("500.00");

            assertThrows(BadRequestException.class,
                    () -> paymentService.processPaymentsForBookings(ids, 1, amt));
        }

        @Test
        @DisplayName("NEGATIVE: total amount does not equal sum of fares -> BadRequestException")
        void totalMismatch() {
            Booking b1 = Booking.builder().bookingId(1).trip(trip).seatNumber(1)
                    .status(BookingStatus.Booked).build();
            Booking b2 = Booking.builder().bookingId(2).trip(trip).seatNumber(2)
                    .status(BookingStatus.Booked).build();

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(bookingRepository.findById(1)).thenReturn(Optional.of(b1));
            when(bookingRepository.findById(2)).thenReturn(Optional.of(b2));
            when(paymentRepository.findByBooking_BookingId(any())).thenReturn(Optional.empty());
            List<Integer> ids = List.of(1, 2);
            BigDecimal amt = new BigDecimal("900.00");

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPaymentsForBookings(ids, 1, amt));
            assertTrue(ex.getMessage().contains("does not match"));
            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("EDGE: total matches expected — scale differences (1000 vs 1000.00) accepted")
        void totalScaleTolerance() {
            Booking b1 = Booking.builder().bookingId(1).trip(trip).seatNumber(1)
                    .status(BookingStatus.Booked).build();
            Booking b2 = Booking.builder().bookingId(2).trip(trip).seatNumber(2)
                    .status(BookingStatus.Booked).build();

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(bookingRepository.findById(1)).thenReturn(Optional.of(b1));
            when(bookingRepository.findById(2)).thenReturn(Optional.of(b2));
            when(paymentRepository.findByBooking_BookingId(any())).thenReturn(Optional.empty());
            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> paymentService.processPaymentsForBookings(
                    List.of(1, 2), 1, new BigDecimal("1000")));
        }
    }

    // ============================================================
    // getPaymentById()
    // ============================================================
    @Nested
    @DisplayName("getPaymentById()")
    class GetPaymentById {

        @Test
        @DisplayName("POSITIVE: returns payment for valid ID")
        void success() {
            when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));

            PaymentResponseDTO result = paymentService.getPaymentById(1);

            assertEquals(1, result.getPaymentId());
            assertEquals("Payment fetched successfully", result.getMessage());
            assertTrue(result.isHasValidBooking());
        }

        @Test
        @DisplayName("NEGATIVE: throws for invalid ID")
        void notFound() {
            when(paymentRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.getPaymentById(999));
        }

        @Test
        @DisplayName("EDGE: null booking on Payment — response has hasValidBooking=false, null bookingId")
        void nullBooking() {
            Payment noBooking = Payment.builder()
                    .paymentId(2).booking(null).customer(customer)
                    .amount(new BigDecimal("300.00"))
                    .paymentDate(LocalDateTime.now())
                    .paymentStatus(PaymentStatus.Success).build();
            when(paymentRepository.findById(2)).thenReturn(Optional.of(noBooking));

            PaymentResponseDTO result = paymentService.getPaymentById(2);

            assertNull(result.getBookingId());
            assertFalse(result.isHasValidBooking());
        }

        @Test
        @DisplayName("EDGE: null customer on Payment — response has null customerId")
        void nullCustomer() {
            Payment noCustomer = Payment.builder()
                    .paymentId(3).booking(booking).customer(null)
                    .amount(new BigDecimal("200.00"))
                    .paymentDate(LocalDateTime.now())
                    .paymentStatus(PaymentStatus.Success).build();
            when(paymentRepository.findById(3)).thenReturn(Optional.of(noCustomer));

            PaymentResponseDTO result = paymentService.getPaymentById(3);

            assertNull(result.getCustomerId());
        }
    }

    // ============================================================
    // getPaymentByBookingId()
    // ============================================================
    @Nested
    @DisplayName("getPaymentByBookingId()")
    class GetPaymentByBookingId {

        @Test
        @DisplayName("POSITIVE: returns payment when booking has one")
        void success() {
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.of(payment));

            PaymentResponseDTO result = paymentService.getPaymentByBookingId(1);

            assertEquals(1, result.getPaymentId());
        }

        @Test
        @DisplayName("NEGATIVE: throws when no payment exists for booking")
        void notFound() {
            when(paymentRepository.findByBooking_BookingId(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.getPaymentByBookingId(999));
        }
    }

    // ============================================================
    // getPaymentsByCustomerId()
    // ============================================================
    @Nested
    @DisplayName("getPaymentsByCustomerId()")
    class GetPaymentsByCustomerId {

        @Test
        @DisplayName("POSITIVE: returns all payments for customer")
        void returnsList() {
            when(paymentRepository.findByCustomer_CustomerId(1)).thenReturn(List.of(payment));

            List<PaymentResponseDTO> result = paymentService.getPaymentsByCustomerId(1);

            assertEquals(1, result.size());
            assertEquals(1, result.get(0).getPaymentId());
        }

        @Test
        @DisplayName("EDGE: returns empty list when customer has none")
        void empty() {
            when(paymentRepository.findByCustomer_CustomerId(99)).thenReturn(Collections.emptyList());

            assertTrue(paymentService.getPaymentsByCustomerId(99).isEmpty());
        }
    }

    // ============================================================
    // getAllPayments()
    // ============================================================
    @Nested
    @DisplayName("getAllPayments()")
    class GetAllPayments {

        @Test
        @DisplayName("POSITIVE: returns every payment with joined details")
        void returnsList() {
            when(paymentRepository.findAllWithDetails()).thenReturn(List.of(payment));

            List<PaymentResponseDTO> result = paymentService.getAllPayments();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("EDGE: returns empty list when none exist")
        void empty() {
            when(paymentRepository.findAllWithDetails()).thenReturn(Collections.emptyList());

            assertTrue(paymentService.getAllPayments().isEmpty());
        }
    }
}
