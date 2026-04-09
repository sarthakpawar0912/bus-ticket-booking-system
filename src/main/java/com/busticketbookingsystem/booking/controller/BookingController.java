package com.busticketbookingsystem.booking.controller;

import com.busticketbookingsystem.booking.dto.BookingRequestDTO;
import com.busticketbookingsystem.booking.dto.BookingResponseDTO;
import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * Endpoint for Member 3's frontend to load the seat grid.
     * GET /api/bookings/trip/1/seats
     */
    @GetMapping("/trip/{tripId}/seats")
    public ResponseEntity<List<Booking>> getTripSeats(@PathVariable Long tripId) {
        List<Booking> bookedSeats = bookingService.getBookedSeatsForTrip(tripId);
        return ResponseEntity.ok(bookedSeats);
    }

    /**
     * Endpoint for locking seats when user clicks "Proceed to Pay".
     * POST /api/bookings
     */
    @PostMapping
    public ResponseEntity<BookingResponseDTO> initiateBooking(@RequestBody BookingRequestDTO request) {
        BookingResponseDTO response = bookingService.initiateBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint for Member 5 to call when payment is successful.
     * POST /api/bookings/1/confirm
     */
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<String> confirmBooking(@PathVariable Long bookingId) {
        // Since seats are marked "Booked" upon initiation in this specific DB schema,
        // this endpoint serves as a successful webhook for Member 5 to link the transaction.
        return ResponseEntity.ok("Booking " + bookingId + " has been confirmed via Payment System.");
    }

    /**
     * Endpoint for cancelling a ticket.
     * POST /api/bookings/trip/1/cancel
     */
    @PostMapping("/trip/{tripId}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable Long tripId, @RequestBody List<Integer> seatNumbers) {
        String response = bookingService.cancelBooking(tripId, seatNumbers);
        return ResponseEntity.ok(response);
    }
}