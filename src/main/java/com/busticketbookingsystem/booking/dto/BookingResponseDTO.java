package com.busticketbookingsystem.booking.dto;

import java.math.BigDecimal;

/**
 * DTO for sending the booking confirmation and payment details back to the user.
 */
public record BookingResponseDTO(
        Integer bookingId,
        Long tripId,
        Integer seatNumber,
        String status,

        // We include the Fare here so the frontend knows exactly how much
        // to charge the user's credit card in the next step!
        BigDecimal fare
) {}