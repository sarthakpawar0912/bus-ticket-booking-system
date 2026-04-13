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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

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

    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

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

        return mapToResponseDTO(saved, "Payment processed successfully");
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

    @Transactional
    public PaymentResponseDTO refundPayment(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        if (payment.getPaymentStatus() != PaymentStatus.Success) {
            throw new BadRequestException("Refund not allowed for payment with status: " + payment.getPaymentStatus());
        }

        payment.setPaymentStatus(PaymentStatus.Failed);
        paymentRepository.save(payment);

        return mapToResponseDTO(payment, "Refund processed successfully");
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
                // Force load to check if it actually exists
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
