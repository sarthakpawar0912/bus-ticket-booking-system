package com.busticketbookingsystem.payment.service;

import com.busticketbookingsystem.payment.dto.PaymentRequestDTO;
import com.busticketbookingsystem.payment.dto.PaymentResponseDTO;
import com.busticketbookingsystem.payment.entity.Payment;
import com.busticketbookingsystem.payment.entity.PaymentStatus;
import com.busticketbookingsystem.payment.repository.PaymentRepository;

import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.repository.BookingRepository;

import com.busticketbookingsystem.customer.entity.Customer;
import com.busticketbookingsystem.customer.repository.CustomerRepository;

import com.busticketbookingsystem.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;

    // 💳 PROCESS PAYMENT
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Payment payment = Payment.builder()
                .booking(booking)
                .customer(customer)
                .amount(booking.getTrip().getFare())
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.Success)
                .build();

        Payment saved = paymentRepository.save(payment);

        return PaymentResponseDTO.builder()
                .paymentId(saved.getPaymentId())
                .bookingId(booking.getBookingId())
                .customerId(customer.getCustomerId())
                .amount(saved.getAmount())
                .status(saved.getPaymentStatus().name())
                .paymentDate(saved.getPaymentDate())
                .build();
    }

    // 🔁 REFUND
    public PaymentResponseDTO refund(Integer bookingId) {

        Payment payment = paymentRepository.findByBooking_BookingId(bookingId);

        if (payment == null) {
            throw new ResourceNotFoundException("Payment not found");
        }

        payment.setPaymentStatus(PaymentStatus.Failed);
        Payment updated = paymentRepository.save(payment);

        return PaymentResponseDTO.builder()
                .paymentId(updated.getPaymentId())
                .bookingId(updated.getBooking().getBookingId())
                .customerId(updated.getCustomer().getCustomerId())
                .amount(updated.getAmount())
                .status(updated.getPaymentStatus().name())
                .paymentDate(updated.getPaymentDate())
                .build();
    }
}