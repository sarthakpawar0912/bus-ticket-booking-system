package com.busticketbookingsystem.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record TripSearchResponseDto(
        Long tripId,
        Long routeId,
        String source,
        String destination,
        LocalDate travelDate,
        LocalTime departureTime,
        LocalTime arrivalTime,
        BigDecimal fare,
        Integer availableSeats
) {
}
