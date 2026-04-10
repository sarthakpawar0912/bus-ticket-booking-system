package com.busticketbookingsystem.trip.controller;

import com.busticketbookingsystem.trip.dto.RouteCreateRequestDto;
import com.busticketbookingsystem.trip.dto.RouteResponseDto;
import com.busticketbookingsystem.trip.dto.RouteUpdateRequestDto;
import com.busticketbookingsystem.trip.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    public ResponseEntity<RouteResponseDto> createRoute(@Valid @RequestBody RouteCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(request));
    }

    @GetMapping
    public List<RouteResponseDto> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    @GetMapping("/{routeId}")
    public RouteResponseDto getRouteById(@PathVariable Long routeId) {
        return routeService.getRouteById(routeId);
    }

    @PutMapping("/{routeId}")
    public RouteResponseDto updateRoute(@PathVariable Long routeId, @Valid @RequestBody RouteUpdateRequestDto request) {
        return routeService.updateRoute(routeId, request);
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long routeId) {
        routeService.deleteRoute(routeId);
        return ResponseEntity.noContent().build();
    }
}
