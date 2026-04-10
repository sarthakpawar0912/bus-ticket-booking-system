package com.busticketbookingsystem.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// This annotation automatically tells Spring to return a 404 Not Found
// if this exception is ever thrown and not caught.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Integer bookingId) {
        super("Booking not found with ID: " + bookingId);
    }

    public BookingNotFoundException(String message) {
        super(message);
    }
}