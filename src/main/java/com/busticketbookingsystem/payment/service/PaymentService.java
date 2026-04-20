package com.busticketbookingsystem.payment.service;

import com.busticketbookingsystem.booking.entity.Booking;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository,
                          CustomerRepository customerRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Process a single-booking payment. Amount must equal trip.fare exactly.
     * Rejects if a Payment row already exists for the booking (double-pay
     * guard — we cannot add a PAID status to BookingStatus without altering
     * the schema, so Payment-row existence is the sole gate).
     */
    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero.");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

        if (paymentRepository.findByBooking_BookingId(booking.getBookingId()).isPresent()) {
            throw new BadRequestException("Booking " + booking.getBookingId() + " is already paid.");
        }

        BigDecimal expected = booking.getTrip() != null ? booking.getTrip().getFare() : null;
        if (expected == null) {
            throw new BadRequestException("Trip fare is not configured for this booking.");
        }
        if (expected.compareTo(request.getAmount()) != 0) {
            throw new BadRequestException("Amount " + request.getAmount()
                    + " does not match expected fare " + expected
                    + " for booking " + booking.getBookingId() + ".");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        Payment payment = Payment.builder()
                .booking(booking)
                .customer(customer)
                .amount(request.getAmount())
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.Success)
                .build();
        Payment saved = paymentRepository.save(payment);
        log.info("Payment {} processed for booking {} (amount={})",
                saved.getPaymentId(), booking.getBookingId(), saved.getAmount());

        return mapToResponseDTO(saved, "Payment processed successfully");
    }

    /**
     * Process a group payment: one Payment row per Booking, sharing a single
     * paymentDate so the group is detectable downstream. Total charged =
     * sum(trip.fare) over the bookings. Throws if the client-supplied total
     * does not match the sum of per-seat fares.
     */
    @Transactional
    public List<PaymentResponseDTO> processPaymentsForBookings(List<Integer> bookingIds,
                                                               Integer customerId,
                                                               BigDecimal totalAmount) {
        if (bookingIds == null || bookingIds.isEmpty()) {
            throw new BadRequestException("At least one booking id is required.");
        }
        if (totalAmount == null || totalAmount.signum() <= 0) {
            throw new BadRequestException("Total amount must be greater than zero.");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        List<Booking> bookings = new ArrayList<>();
        BigDecimal expectedTotal = BigDecimal.ZERO;
        for (Integer bid : bookingIds) {
            Booking b = bookingRepository.findById(bid)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bid));
            if (paymentRepository.findByBooking_BookingId(bid).isPresent()) {
                throw new BadRequestException("Booking " + bid + " is already paid.");
            }
            BigDecimal fare = b.getTrip() != null ? b.getTrip().getFare() : null;
            if (fare == null) {
                throw new BadRequestException("Trip fare is not configured for booking " + bid + ".");
            }
            expectedTotal = expectedTotal.add(fare);
            bookings.add(b);
        }

        if (expectedTotal.compareTo(totalAmount) != 0) {
            throw new BadRequestException("Total amount " + totalAmount
                    + " does not match expected " + expectedTotal
                    + " (sum of fares for " + bookings.size() + " seat(s)).");
        }

        LocalDateTime sharedDate = LocalDateTime.now();
        List<PaymentResponseDTO> responses = new ArrayList<>();
        for (Booking b : bookings) {
            Payment payment = Payment.builder()
                    .booking(b)
                    .customer(customer)
                    .amount(b.getTrip().getFare())
                    .paymentDate(sharedDate)
                    .paymentStatus(PaymentStatus.Success)
                    .build();
            Payment saved = paymentRepository.save(payment);
            responses.add(mapToResponseDTO(saved, "Payment processed successfully"));
        }
        log.info("Group payment: {} booking(s) paid by customer {} (total={})",
                responses.size(), customerId, totalAmount);
        return responses;
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return mapToResponseDTO(payment, "Payment fetched successfully");
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByBookingId(Integer bookingId) {
        Payment payment = paymentRepository.findByBooking_BookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking id: " + bookingId));
        return mapToResponseDTO(payment, "Payment fetched successfully");
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByCustomerId(Integer customerId) {
        return paymentRepository.findByCustomer_CustomerId(customerId).stream()
                .map(p -> mapToResponseDTO(p, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAllWithDetails().stream()
                .map(p -> mapToResponseDTO(p, null))
                .toList();
    }

    private PaymentResponseDTO mapToResponseDTO(Payment payment, String message) {
        boolean validBooking = false;
        Integer bookingId = null;
        try {
            if (payment.getBooking() != null) {
                bookingId = payment.getBooking().getBookingId();
                payment.getBooking().getSeatNumber();
                validBooking = true;
            }
        } catch (Exception e) {
            validBooking = false;
        }
        return PaymentResponseDTO.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(bookingId)
                .customerId(payment.getCustomer() != null ? payment.getCustomer().getCustomerId() : null)
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .message(message)
                .hasValidBooking(validBooking)
                .build();
    }
}
