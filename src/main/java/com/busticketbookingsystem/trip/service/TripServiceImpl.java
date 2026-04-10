package com.busticketbookingsystem.trip.service;

import com.busticketbookingsystem.agency.entity.Bus;
import com.busticketbookingsystem.agency.entity.Driver;
import com.busticketbookingsystem.agency.repository.BusRepository;
import com.busticketbookingsystem.agency.repository.DriverRepository;
import com.busticketbookingsystem.trip.dto.TripCreateRequestDto;
import com.busticketbookingsystem.trip.dto.TripResponseDto;
import com.busticketbookingsystem.trip.dto.TripSearchResponseDto;
import com.busticketbookingsystem.trip.dto.TripUpdateRequestDto;
import com.busticketbookingsystem.trip.entity.Route;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.entity.TripStatus;
import com.busticketbookingsystem.trip.exception.RouteNotFoundException;
import com.busticketbookingsystem.trip.exception.TripNotFoundException;
import com.busticketbookingsystem.trip.exception.TripValidationException;
import com.busticketbookingsystem.trip.repository.RouteRepository;
import com.busticketbookingsystem.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final DriverRepository driverRepository;

    public TripServiceImpl(
            TripRepository tripRepository,
            RouteRepository routeRepository,
            BusRepository busRepository,
            DriverRepository driverRepository
    ) {
        this.tripRepository = tripRepository;
        this.routeRepository = routeRepository;
        this.busRepository = busRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public TripResponseDto createTrip(TripCreateRequestDto request) {
        validateTripRequest(request.travelDate(), request.departureTime(), request.arrivalTime(), request.availableSeats(), request.busId(), request.driverId(), null);

        Trip trip = new Trip();
        applyRequest(trip, request.routeId(), request.busId(), request.driverId(), request.travelDate(),
                request.departureTime(), request.arrivalTime(), request.fare(), request.availableSeats(), TripStatus.SCHEDULED);

        return toResponse(tripRepository.save(trip));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponseDto> getAllTrips() {
        return tripRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponseDto getTripById(Long tripId) {
        return toResponse(getTripEntity(tripId));
    }

    @Override
    public TripResponseDto updateTrip(Long tripId, TripUpdateRequestDto request) {
        Trip trip = getTripEntity(tripId);
        validateTripRequest(request.travelDate(), request.departureTime(), request.arrivalTime(), request.availableSeats(), request.busId(), request.driverId(), tripId);

        applyRequest(trip, request.routeId(), request.busId(), request.driverId(), request.travelDate(),
                request.departureTime(), request.arrivalTime(), request.fare(), request.availableSeats(), request.status());

        return toResponse(tripRepository.save(trip));
    }

    @Override
    public void deleteTrip(Long tripId) {
        tripRepository.delete(getTripEntity(tripId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripSearchResponseDto> searchTrips(String source, String destination, LocalDate travelDate, Long routeId, String busType) {
        return tripRepository.searchTrips(normalize(source), normalize(destination), travelDate, routeId, normalize(busType), TripStatus.SCHEDULED)
                .stream()
                .map(this::toSearchResponse)
                .toList();
    }

    private void applyRequest(
            Trip trip,
            Long routeId,
            Integer busId,
            Integer driverId,
            LocalDate travelDate,
            LocalTime departureTime,
            LocalTime arrivalTime,
            BigDecimal fare,
            Integer availableSeats,
            TripStatus status
    ) {
        Route route = routeRepository.findById(routeId).orElseThrow(() -> new RouteNotFoundException(routeId));
        Bus bus = busRepository.findById(busId).orElseThrow(() -> new TripValidationException("Bus not found with id: " + busId));
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new TripValidationException("Driver not found with id: " + driverId));

        if (availableSeats > bus.getCapacity()) {
            throw new TripValidationException("Available seats cannot exceed bus capacity");
        }
        if (fare == null || fare.signum() < 0) {
            throw new TripValidationException("Fare must be zero or greater");
        }
        if (status == null) {
            throw new TripValidationException("Trip status is required");
        }

        trip.setRoute(route);
        trip.setBus(bus);
        trip.setDriver(driver);
        trip.setTravelDate(travelDate);
        trip.setDepartureTime(departureTime);
        trip.setArrivalTime(arrivalTime);
        trip.setFare(fare);
        trip.setAvailableSeats(availableSeats);
        trip.setStatus(status);
    }

    private void validateTripRequest(
            LocalDate travelDate,
            LocalTime departureTime,
            LocalTime arrivalTime,
            Integer availableSeats,
            Integer busId,
            Integer driverId,
            Long tripId
    ) {
        if (travelDate == null) {
            throw new TripValidationException("Travel date is required");
        }
        if (departureTime == null || arrivalTime == null) {
            throw new TripValidationException("Departure time and arrival time are required");
        }
        if (!arrivalTime.isAfter(departureTime)) {
            throw new TripValidationException("Arrival time must be after departure time");
        }
        if (availableSeats == null || availableSeats < 0) {
            throw new TripValidationException("Available seats must be zero or greater");
        }
        if (busId == null) {
            throw new TripValidationException("Bus id is required");
        }
        if (driverId == null) {
            throw new TripValidationException("Driver id is required");
        }

        boolean busConflict = tripId == null
                ? tripRepository.existsByBus_BusIdAndTravelDateAndDepartureTime(busId, travelDate, departureTime)
                : tripRepository.existsBusScheduleConflict(tripId, busId, travelDate, departureTime);
        if (busConflict) {
            throw new TripValidationException("Bus is already assigned to another trip at the same date and departure time");
        }

        boolean driverConflict = tripId == null
                ? tripRepository.existsByDriver_DriverIdAndTravelDateAndDepartureTime(driverId, travelDate, departureTime)
                : tripRepository.existsDriverScheduleConflict(tripId, driverId, travelDate, departureTime);
        if (driverConflict) {
            throw new TripValidationException("Driver is already assigned to another trip at the same date and departure time");
        }
    }

    private Trip getTripEntity(Long tripId) {
        return tripRepository.findById(tripId).orElseThrow(() -> new TripNotFoundException(tripId));
    }

    private TripResponseDto toResponse(Trip trip) {
        return new TripResponseDto(
                trip.getId(),
                trip.getRoute().getId(),
                trip.getRoute().getSource(),
                trip.getRoute().getDestination(),
                trip.getBus().getBusId(),
                trip.getBus().getRegistrationNumber(),
                trip.getBus().getType(),
                trip.getDriver().getDriverId(),
                trip.getDriver().getName(),
                trip.getTravelDate(),
                trip.getDepartureTime(),
                trip.getArrivalTime(),
                trip.getFare(),
                trip.getAvailableSeats(),
                trip.getStatus()
        );
    }

    private TripSearchResponseDto toSearchResponse(Trip trip) {
        return new TripSearchResponseDto(
                trip.getId(),
                trip.getRoute().getId(),
                trip.getRoute().getSource(),
                trip.getRoute().getDestination(),
                trip.getBus().getBusId(),
                trip.getBus().getType(),
                trip.getTravelDate(),
                trip.getDepartureTime(),
                trip.getArrivalTime(),
                trip.getFare(),
                trip.getAvailableSeats()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
