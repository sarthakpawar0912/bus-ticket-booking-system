package com.busticketbookingsystem.trip.exception;

public class RouteNotFoundException extends RuntimeException {

    public RouteNotFoundException(Long routeId) {
        super("Route not found with id: " + routeId);
    }
}
