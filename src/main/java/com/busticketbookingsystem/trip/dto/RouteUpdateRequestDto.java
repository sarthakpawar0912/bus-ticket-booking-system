package com.busticketbookingsystem.trip.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RouteUpdateRequestDto(
        @NotBlank String source,
        @NotBlank String destination,
        @NotNull @Min(0) Integer distanceKm,
        @NotNull @Min(0) Integer estimatedDurationMinutes
) {
}
