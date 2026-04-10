package com.busticketbookingsystem.trip.service;

import com.busticketbookingsystem.trip.dto.TripCreateRequestDto;
import com.busticketbookingsystem.trip.dto.TripResponseDto;
import com.busticketbookingsystem.trip.dto.TripSearchResponseDto;
import com.busticketbookingsystem.trip.dto.TripUpdateRequestDto;

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