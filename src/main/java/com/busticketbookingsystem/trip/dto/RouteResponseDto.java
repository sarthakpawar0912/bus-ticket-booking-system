package com.busticketbookingsystem.trip.dto;

public record RouteResponseDto(
        Long id,
        String source,
        String destination,
        Integer distanceKm,
        Integer estimatedDurationMinutes
) {
}
