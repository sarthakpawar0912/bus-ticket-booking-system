package com.busticketbookingsystem.trip.service;

import com.busticketbookingsystem.trip.entity.Route;
import com.busticketbookingsystem.trip.exception.RouteNotFoundException;
import com.busticketbookingsystem.trip.exception.TripValidationException;
import com.busticketbookingsystem.trip.repository.RouteRepository;
import com.busticketbookingsystem.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;

    public RouteServiceImpl(RouteRepository routeRepository, TripRepository tripRepository) {
        this.routeRepository = routeRepository;
        this.tripRepository = tripRepository;
    }

    @Override
    public RouteResponseDto createRoute(RouteCreateRequestDto request) {
        validateRouteRequest(request.source(), request.destination(), request.distanceKm(), request.estimatedDurationMinutes());
        String source = normalize(request.source());
        String destination = normalize(request.destination());
        if (routeRepository.existsBySourceIgnoreCaseAndDestinationIgnoreCase(source, destination)) {
            throw new TripValidationException("Route already exists for the given source and destination");
        }

        Route route = new Route();
        route.setSource(source);
        route.setDestination(destination);
        route.setDistanceKm(request.distanceKm());
        route.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        return toResponse(routeRepository.save(route));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponseDto> getAllRoutes() {
        return routeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponseDto getRouteById(Long routeId) {
        return toResponse(getRouteEntity(routeId));
    }

    @Override
    public RouteResponseDto updateRoute(Long routeId, RouteUpdateRequestDto request) {
        validateRouteRequest(request.source(), request.destination(), request.distanceKm(), request.estimatedDurationMinutes());
        Route route = getRouteEntity(routeId);
        String source = normalize(request.source());
        String destination = normalize(request.destination());

        routeRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(source, destination)
                .filter(existing -> !existing.getId().equals(routeId))
                .ifPresent(existing -> {
                    throw new TripValidationException("Another route already exists for the given source and destination");
                });

        route.setSource(source);
        route.setDestination(destination);
        route.setDistanceKm(request.distanceKm());
        route.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        return toResponse(routeRepository.save(route));
    }

    @Override
    public void deleteRoute(Long routeId) {
        Route route = getRouteEntity(routeId);
        if (tripRepository.existsByRoute_Id(routeId)) {
            throw new TripValidationException("Route cannot be deleted because trips are scheduled on it");
        }
        routeRepository.delete(route);
    }

    private Route getRouteEntity(Long routeId) {
        return routeRepository.findById(routeId).orElseThrow(() -> new RouteNotFoundException(routeId));
    }

    private RouteResponseDto toResponse(Route route) {
        return new RouteResponseDto(
                route.getId(),
                route.getSource(),
                route.getDestination(),
                route.getDistanceKm(),
                route.getEstimatedDurationMinutes()
        );
    }

    private void validateRouteRequest(String source, String destination, Integer distanceKm, Integer estimatedDurationMinutes) {
        String normalizedSource = normalize(source);
        String normalizedDestination = normalize(destination);
        if (normalizedSource == null || normalizedDestination == null) {
            throw new TripValidationException("Source and destination are required");
        }
        if (normalizedSource.equalsIgnoreCase(normalizedDestination)) {
            throw new TripValidationException("Source and destination must be different");
        }
        if (distanceKm == null || distanceKm < 0) {
            throw new TripValidationException("Distance must be zero or greater");
        }
        if (estimatedDurationMinutes == null || estimatedDurationMinutes < 0) {
            throw new TripValidationException("Estimated duration must be zero or greater");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
