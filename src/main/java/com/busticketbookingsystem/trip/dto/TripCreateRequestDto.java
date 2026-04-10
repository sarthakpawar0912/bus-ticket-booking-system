package com.busticketbookingsystem.trip.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record TripCreateRequestDto(
        @NotNull Long routeId,
        @NotNull Integer busId,
        @NotNull Integer driverId,
        @NotNull @FutureOrPresent LocalDate travelDate,
        @NotNull LocalTime departureTime,
        @NotNull LocalTime arrivalTime,
        @NotNull @DecimalMin("0.0") BigDecimal fare,
        @NotNull @Min(0) Integer availableSeats
) {
}
