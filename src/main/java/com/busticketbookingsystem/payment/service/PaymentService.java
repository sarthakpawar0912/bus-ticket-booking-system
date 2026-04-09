package com.busticketbookingsystem.payment.service;

import com.busticketbookingsystem.payment.dto.PaymentRequestDTO;
import com.busticketbookingsystem.payment.dto.PaymentResponseDTO;
import com.busticketbookingsystem.payment.entity.Payment;
import com.busticketbookingsystem.payment.entity.PaymentStatus;
import com.busticketbookingsystem.payment.exception.*;
import com.busticketbookingsystem.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }


    public PaymentResponseDTO makePayment(PaymentRequestDTO request) {

        if (request.getAmount() <= 0) {
            throw new InvalidPaymentException("Amount must be greater than zero");
        }

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .paymentTime(LocalDateTime.now())
                .build();

        // Simulate payment success
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(UUID.randomUUID().toString());

        Payment saved = paymentRepository.save(payment);

        return new PaymentResponseDTO(
                saved.getPaymentId(),
                saved.getBookingId(),
                saved.getAmount(),
                saved.getStatus(),
                "Payment Successful"
        );
    }


    public PaymentResponseDTO getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        return new PaymentResponseDTO(
                payment.getPaymentId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getStatus(),
                "Payment fetched successfully"
        );
    }


    public PaymentResponseDTO refund(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RefundNotAllowedException("Refund not allowed");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        return new PaymentResponseDTO(
                payment.getPaymentId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getStatus(),
                "Refund successful"
        );
    }
}