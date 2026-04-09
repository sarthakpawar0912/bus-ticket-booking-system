package com.busticketbookingsystem.payment.dto;

import com.busticketbookingsystem.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long paymentId;
    private Long bookingId;
    private Double amount;
    private PaymentStatus status;
    private String message;
}