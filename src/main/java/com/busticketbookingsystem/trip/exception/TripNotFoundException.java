package com.busticketbookingsystem.trip.exception;

public class TripNotFoundException extends RuntimeException {

    public TripNotFoundException(Long tripId) {
        super("Trip not found with id: " + tripId);
    }
}
