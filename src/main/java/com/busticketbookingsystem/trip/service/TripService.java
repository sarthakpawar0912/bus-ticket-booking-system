package com.busticketbookingsystem.trip.service;

import java.time.LocalDate;
import java.util.List;

public interface TripService {

    TripResponseDto createTrip(TripCreateRequestDto request);

    List<TripResponseDto> getAllTrips();

    TripResponseDto getTripById(Long tripId);

    TripResponseDto updateTrip(Long tripId, TripUpdateRequestDto request);

    void deleteTrip(Long tripId);

    List<TripSearchResponseDto> searchTrips(String source, String destination, LocalDate travelDate, Long routeId, String busType);
}