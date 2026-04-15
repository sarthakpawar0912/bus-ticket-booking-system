package busticketbookingsystem.payment.service;

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
import com.busticketbookingsystem.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

/**
 * Unit tests for PaymentService.
 * Covers processPayment, getPaymentById, getPaymentByBookingId,
 * getPaymentsByCustomerId, refundPayment, getAllPayments.
 */
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

    private Booking booking;
    private Customer customer;
    private Payment payment;
    private PaymentRequestDTO paymentRequest;

    @BeforeEach
    void setUp() {
        booking = Booking.builder()
                .bookingId(1).seatNumber(5)
                .status(BookingStatus.Booked).build();

        customer = Customer.builder()
                .customerId(1).name("Test Customer")
                .email("test@email.com").phone("9876543210").build();

        payment = Payment.builder()
                .paymentId(1).booking(booking).customer(customer)
                .amount(new BigDecimal("500.00"))
                .paymentDate(LocalDateTime.of(2026, 4, 15, 10, 0))
                .paymentStatus(PaymentStatus.Success).build();

        paymentRequest = new PaymentRequestDTO();
        paymentRequest.setBookingId(1);
        paymentRequest.setCustomerId(1);
        paymentRequest.setAmount(new BigDecimal("500.00"));
    }

    @Nested
    @DisplayName("processPayment() Tests")
    class ProcessPaymentTests {

        @Test
        @DisplayName("POSITIVE: Should process payment successfully")
        void processPayment_Success() {
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            PaymentResponseDTO result = paymentService.processPayment(paymentRequest);

            assertNotNull(result);
            assertEquals(1, result.getPaymentId());
            assertEquals(1, result.getBookingId());
            assertEquals(1, result.getCustomerId());
            assertEquals(new BigDecimal("500.00"), result.getAmount());
            assertEquals(PaymentStatus.Success, result.getPaymentStatus());
            assertEquals("Payment processed successfully", result.getMessage());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when booking not found")
        void processPayment_BookingNotFound() {
            when(bookingRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.processPayment(paymentRequest));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when customer not found")
        void processPayment_CustomerNotFound() {
            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
            when(customerRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.processPayment(paymentRequest));
        }
    }

    @Nested
    @DisplayName("getPaymentById() Tests")
    class GetPaymentByIdTests {

        @Test
        @DisplayName("POSITIVE: Should return payment for valid ID")
        void getPaymentById_Success() {
            when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));

            PaymentResponseDTO result = paymentService.getPaymentById(1);

            assertEquals(1, result.getPaymentId());
            assertEquals("Payment fetched successfully", result.getMessage());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw for invalid ID")
        void getPaymentById_NotFound() {
            when(paymentRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.getPaymentById(999));
        }
    }

    @Nested
    @DisplayName("getPaymentByBookingId() Tests")
    class GetPaymentByBookingIdTests {

        @Test
        @DisplayName("POSITIVE: Should return payment for valid booking ID")
        void getPaymentByBookingId_Success() {
            when(paymentRepository.findByBooking_BookingId(1)).thenReturn(Optional.of(payment));

            PaymentResponseDTO result = paymentService.getPaymentByBookingId(1);

            assertEquals(1, result.getPaymentId());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when no payment found for booking")
        void getPaymentByBookingId_NotFound() {
            when(paymentRepository.findByBooking_BookingId(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.getPaymentByBookingId(999));
        }
    }

    @Nested
    @DisplayName("getPaymentsByCustomerId() Tests")
    class GetPaymentsByCustomerIdTests {

        @Test
        @DisplayName("POSITIVE: Should return payments for a customer")
        void getPaymentsByCustomerId_ReturnsList() {
            when(paymentRepository.findByCustomer_CustomerId(1)).thenReturn(List.of(payment));

            List<PaymentResponseDTO> result = paymentService.getPaymentsByCustomerId(1);

            assertEquals(1, result.size());
            assertEquals(1, result.get(0).getPaymentId());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when customer has no payments")
        void getPaymentsByCustomerId_Empty() {
            when(paymentRepository.findByCustomer_CustomerId(99)).thenReturn(Collections.emptyList());

            assertTrue(paymentService.getPaymentsByCustomerId(99).isEmpty());
        }
    }

    @Nested
    @DisplayName("refundPayment() Tests")
    class RefundPaymentTests {

        @Test
        @DisplayName("POSITIVE: Should refund a successful payment")
        void refundPayment_Success() {
            when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            PaymentResponseDTO result = paymentService.refundPayment(1);

            assertEquals("Refund processed successfully", result.getMessage());
            // Status should change to Failed (as per the service logic)
            assertEquals(PaymentStatus.Failed, payment.getPaymentStatus());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException when payment status is not Success")
        void refundPayment_NotSuccessStatus_Throws() {
            // Change payment status to Failed (already refunded)
            payment.setPaymentStatus(PaymentStatus.Failed);

            when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.refundPayment(1));

            assertTrue(ex.getMessage().contains("Refund not allowed"));
            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for non-existent payment")
        void refundPayment_NotFound() {
            when(paymentRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.refundPayment(999));
        }
    }

    @Nested
    @DisplayName("getAllPayments() Tests")
    class GetAllPaymentsTests {

        @Test
        @DisplayName("POSITIVE: Should return all payments with details")
        void getAllPayments_ReturnsList() {
            when(paymentRepository.findAllWithDetails()).thenReturn(List.of(payment));

            List<PaymentResponseDTO> result = paymentService.getAllPayments();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no payments exist")
        void getAllPayments_Empty() {
            when(paymentRepository.findAllWithDetails()).thenReturn(Collections.emptyList());

            assertTrue(paymentService.getAllPayments().isEmpty());
        }
    }

    @Test
    @DisplayName("EDGE CASE: mapToResponseDTO should handle null booking gracefully")
    void mapToResponseDTO_NullBooking() {
        Payment paymentNoBooking = Payment.builder()
                .paymentId(2).booking(null).customer(customer)
                .amount(new BigDecimal("300.00"))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.Success).build();

        when(paymentRepository.findById(2)).thenReturn(Optional.of(paymentNoBooking));

        PaymentResponseDTO result = paymentService.getPaymentById(2);

        assertNotNull(result);
        assertNull(result.getBookingId());
        assertFalse(result.isHasValidBooking());
    }

    @Test
    @DisplayName("EDGE CASE: mapToResponseDTO should handle null customer gracefully")
    void mapToResponseDTO_NullCustomer() {
        Payment paymentNoCustomer = Payment.builder()
                .paymentId(3).booking(booking).customer(null)
                .amount(new BigDecimal("200.00"))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.Success).build();

        when(paymentRepository.findById(3)).thenReturn(Optional.of(paymentNoCustomer));

        PaymentResponseDTO result = paymentService.getPaymentById(3);

        assertNotNull(result);
        assertNull(result.getCustomerId());
    }
}
