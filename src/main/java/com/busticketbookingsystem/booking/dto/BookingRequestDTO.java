package com.busticketbookingsystem.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for receiving a seat reservation request from the frontend.
 */
public record BookingRequestDTO(

        @NotNull(message = "Trip ID cannot be null")
        Long tripId,

        @NotNull(message = "Seat number cannot be null")
        @Min(value = 1, message = "Seat number must be at least 1")
        Integer seatNumber,

        @NotNull(message = "Customer ID cannot be null")
        Integer customerId
) {}