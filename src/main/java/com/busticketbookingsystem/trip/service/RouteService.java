package com.busticketbookingsystem.trip.service;

import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.trip.entity.Route;
import com.busticketbookingsystem.trip.repository.RouteRepository;
import com.busticketbookingsystem.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;

    public Route create(Route route) {
        return routeRepository.save(route);
    }

    public List<Route> getAll() {
        return routeRepository.findAll();
    }

    public Route getById(Integer id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
    }

    @Transactional
    public Route update(Integer id, Route updatedRoute) {
        Route route = getById(id);
        if (updatedRoute.getFromCity() != null) route.setFromCity(updatedRoute.getFromCity());
        if (updatedRoute.getToCity() != null) route.setToCity(updatedRoute.getToCity());
        if (updatedRoute.getBreakPoints() != null) route.setBreakPoints(updatedRoute.getBreakPoints());
        if (updatedRoute.getDuration() != null) route.setDuration(updatedRoute.getDuration());
        return routeRepository.save(route);
    }

    public List<Route> searchRoutes(String fromCity, String toCity) {
        return routeRepository.findByFromCityIgnoreCaseAndToCityIgnoreCase(fromCity, toCity);
    }
}
