package com.busticketbookingsystem.payment.dto;

import lombok.Data;

@Data
public class PaymentRequestDTO {

    private Long bookingId;
    private Double amount;
}