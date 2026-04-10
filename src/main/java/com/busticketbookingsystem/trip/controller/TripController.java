package com.busticketbookingsystem.trip.controller;

import com.busticketbookingsystem.trip.dto.TripCreateRequestDto;
import com.busticketbookingsystem.trip.dto.TripResponseDto;
import com.busticketbookingsystem.trip.dto.TripSearchResponseDto;
import com.busticketbookingsystem.trip.dto.TripUpdateRequestDto;
import com.busticketbookingsystem.trip.service.TripService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<TripResponseDto> createTrip(@Valid @RequestBody TripCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.createTrip(request));
    }

    @GetMapping
    public List<TripResponseDto> getAllTrips() {
        return tripService.getAllTrips();
    }

    @GetMapping("/{tripId}")
    public TripResponseDto getTripById(@PathVariable Long tripId) {
        return tripService.getTripById(tripId);
    }

    @PutMapping("/{tripId}")
    public TripResponseDto updateTrip(@PathVariable Long tripId, @Valid @RequestBody TripUpdateRequestDto request) {
        return tripService.updateTrip(tripId, request);
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long tripId) {
        tripService.deleteTrip(tripId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<TripSearchResponseDto> searchTrips(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) String busType
    ) {
        return tripService.searchTrips(source, destination, travelDate, routeId, busType);
    }
}
