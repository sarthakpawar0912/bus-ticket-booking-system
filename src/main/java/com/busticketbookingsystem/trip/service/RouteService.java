package com.busticketbookingsystem.trip.service;

import java.util.List;

public interface RouteService {

    RouteResponseDto createRoute(RouteCreateRequestDto request);

    List<RouteResponseDto> getAllRoutes();

    RouteResponseDto getRouteById(Long routeId);

    RouteResponseDto updateRoute(Long routeId, RouteUpdateRequestDto request);

    void deleteRoute(Long routeId);
}
