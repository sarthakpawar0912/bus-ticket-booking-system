package com.busticketbookingsystem.trip.dto;

import com.busticketbookingsystem.trip.entity.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record TripResponseDto(
        Long id,
        Long routeId,
        String source,
        String destination,
        Long busId,
        Long driverId,
        LocalDate travelDate,
        LocalTime departureTime,
        LocalTime arrivalTime,
        BigDecimal fare,
        Integer availableSeats,
        TripStatus status
) {
}
