package com.busticketbookingsystem.payment.service;

import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.entity.BookingStatus;
import com.busticketbookingsystem.booking.repository.BookingRepository;
import com.busticketbookingsystem.customer.entity.Address;
import com.busticketbookingsystem.customer.entity.Customer;
import com.busticketbookingsystem.customer.repository.CustomerRepository;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.payment.dto.PaymentRequestDTO;
import com.busticketbookingsystem.payment.dto.PaymentResponseDTO;
import com.busticketbookingsystem.payment.entity.Payment;
import com.busticketbookingsystem.payment.entity.PaymentStatus;
import com.busticketbookingsystem.payment.repository.PaymentRepository;
import com.busticketbookingsystem.trip.entity.Trip;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Trip trip;
    private Booking booking;
    private Customer customer;
    private Payment payment;

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

        Address address = Address.builder()
                .addressId(1)
                .address("123 Main St")
                .city("Pune")
                .state("MH")
                .zipCode("411001")
                .build();

        customer = Customer.builder()
                .customerId(1)
                .name("Sarthak")
                .email("sarthak@test.com")
                .phone("9876543210")
                .address(address)
                .build();

        payment = Payment.builder()
                .paymentId(1)
                .booking(booking)
                .customer(customer)
                .amount(new BigDecimal("500.00"))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.Success)
                .build();
    }

    // ==================== processPayment ====================

    @Test
    void processPayment_success() {
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .bookingId(1)
                .customerId(1)
                .amount(new BigDecimal("500.00"))
                .build();

        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponseDTO result = paymentService.processPayment(request);

        assertNotNull(result);
        assertEquals(1, result.getPaymentId());
        assertEquals(1, result.getBookingId());
        assertEquals(1, result.getCustomerId());
        assertEquals(new BigDecimal("500.00"), result.getAmount());
        assertEquals("Success", result.getPaymentStatus());
        assertEquals("Payment processed successfully.", result.getMessage());
    }

    @Test
    void processPayment_bookingNotFound() {
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .bookingId(999)
                .customerId(1)
                .amount(new BigDecimal("500.00"))
                .build();

        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.processPayment(request));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_customerNotFound() {
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .bookingId(1)
                .customerId(999)
                .amount(new BigDecimal("500.00"))
                .build();

        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.processPayment(request));

        verify(paymentRepository, never()).save(any());
    }

    // ==================== getPaymentById ====================

    @Test
    void getPaymentById_success() {
        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));

        PaymentResponseDTO result = paymentService.getPaymentById(1);

        assertNotNull(result);
        assertEquals(1, result.getPaymentId());
        assertNull(result.getMessage());
    }

    @Test
    void getPaymentById_notFound() {
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentById(999));

        assertEquals("Payment not found with id: 999", exception.getMessage());
    }

    @Test
    void getPaymentById_withNullId() {
        when(paymentRepository.findById(null)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentById(null));
    }

    // ==================== getPaymentByBookingId ====================

    @Test
    void getPaymentByBookingId_success() {
        when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.of(payment));

        PaymentResponseDTO result = paymentService.getPaymentByBookingId(1);

        assertNotNull(result);
        assertEquals(1, result.getBookingId());
    }

    @Test
    void getPaymentByBookingId_notFound() {
        when(paymentRepository.findByBooking_BookingId(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentByBookingId(999));
    }

    @Test
    void getPaymentByBookingId_returnsNullMessage() {
        when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.of(payment));

        PaymentResponseDTO result = paymentService.getPaymentByBookingId(1);

        assertNull(result.getMessage());
    }

    // ==================== getPaymentsByCustomerId ====================

    @Test
    void getPaymentsByCustomerId_success() {
        when(paymentRepository.findByCustomer_CustomerId(1)).thenReturn(List.of(payment));

        List<PaymentResponseDTO> result = paymentService.getPaymentsByCustomerId(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCustomerId());
    }

    @Test
    void getPaymentsByCustomerId_noPayments() {
        when(paymentRepository.findByCustomer_CustomerId(999)).thenReturn(Collections.emptyList());

        List<PaymentResponseDTO> result = paymentService.getPaymentsByCustomerId(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPaymentsByCustomerId_multiplePayments() {
        Payment payment2 = Payment.builder()
                .paymentId(2)
                .booking(booking)
                .customer(customer)
                .amount(new BigDecimal("300.00"))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.Success)
                .build();
        when(paymentRepository.findByCustomer_CustomerId(1)).thenReturn(List.of(payment, payment2));

        List<PaymentResponseDTO> result = paymentService.getPaymentsByCustomerId(1);

        assertEquals(2, result.size());
    }

    // ==================== refundPayment ====================

    @Test
    void refundPayment_success() {
        Payment successPayment = Payment.builder()
                .paymentId(1)
                .booking(booking)
                .customer(customer)
                .amount(new BigDecimal("500.00"))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.Success)
                .build();

        Payment refundedPayment = Payment.builder()
                .paymentId(1)
                .booking(booking)
                .customer(customer)
                .amount(new BigDecimal("500.00"))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.Failed)
                .build();

        when(paymentRepository.findById(1)).thenReturn(Optional.of(successPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(refundedPayment);

        PaymentResponseDTO result = paymentService.refundPayment(1);

        assertNotNull(result);
        assertEquals("Failed", result.getPaymentStatus());
        assertEquals("Refund processed successfully.", result.getMessage());
    }

    @Test
    void refundPayment_notFound() {
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.refundPayment(999));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void refundPayment_alreadyFailed() {
        Payment failedPayment = Payment.builder()
                .paymentId(1)
                .booking(booking)
                .customer(customer)
                .amount(new BigDecimal("500.00"))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.Failed)
                .build();

        Payment savedPayment = Payment.builder()
                .paymentId(1)
                .booking(booking)
                .customer(customer)
                .amount(new BigDecimal("500.00"))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.Failed)
                .build();

        when(paymentRepository.findById(1)).thenReturn(Optional.of(failedPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponseDTO result = paymentService.refundPayment(1);

        // Service does not guard against double-refund, so it still processes
        assertEquals("Failed", result.getPaymentStatus());
        assertEquals("Refund processed successfully.", result.getMessage());
    }

    // ==================== getAllPayments ====================

    @Test
    void getAllPayments_success() {
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        List<PaymentResponseDTO> result = paymentService.getAllPayments();

        assertEquals(1, result.size());
        assertEquals("Success", result.get(0).getPaymentStatus());
    }

    @Test
    void getAllPayments_emptyList() {
        when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

        List<PaymentResponseDTO> result = paymentService.getAllPayments();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllPayments_multiplePayments() {
        Payment payment2 = Payment.builder()
                .paymentId(2)
                .booking(booking)
                .customer(customer)
                .amount(new BigDecimal("300.00"))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.Failed)
                .build();
        when(paymentRepository.findAll()).thenReturn(List.of(payment, payment2));

        List<PaymentResponseDTO> result = paymentService.getAllPayments();

        assertEquals(2, result.size());
    }
}
