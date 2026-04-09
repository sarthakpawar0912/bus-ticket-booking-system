package com.busticketbookingsystem.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponseDTO {

    private Integer paymentId;
    private Integer bookingId;
    private Integer customerId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime paymentDate;
}