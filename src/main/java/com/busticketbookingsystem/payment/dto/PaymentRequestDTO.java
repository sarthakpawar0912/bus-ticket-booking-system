package com.busticketbookingsystem.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDTO {

    @NotNull(message = "Booking ID is required")
    private Integer bookingId;

    @NotNull(message = "Customer ID is required")
    private Integer customerId;
}