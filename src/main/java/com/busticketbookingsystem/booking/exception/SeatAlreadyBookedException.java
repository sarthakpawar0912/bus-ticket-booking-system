package com.busticketbookingsystem.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// We return a 400 Bad Request (or 409 Conflict) because the user
// asked for something that is physically impossible to give them.
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SeatAlreadyBookedException extends RuntimeException {

    public SeatAlreadyBookedException(Integer seatNumber, Long tripId) {
        super("Seat number " + seatNumber + " is already booked for Trip ID " + tripId + ". Please select another seat.");
    }

    public SeatAlreadyBookedException(String message) {
        super(message);
    }
}