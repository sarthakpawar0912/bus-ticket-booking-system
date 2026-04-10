package com.busticketbookingsystem.booking.controller;

import com.busticketbookingsystem.booking.dto.BookingRequestDTO;
import com.busticketbookingsystem.booking.dto.BookingResponseDTO;
import com.busticketbookingsystem.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    // Constructor Injection
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * POST /api/bookings
     * Initiates a new seat reservation.
     */
    @PostMapping
    public ResponseEntity<BookingResponseDTO> reserveSeat(@Valid @RequestBody BookingRequestDTO request) {
        // Hand off to the Service layer to do the heavy lifting
        BookingResponseDTO response = bookingService.reserveSeat(request);

        // Return 201 CREATED status along with the ticket details
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/bookings/{bookingId}
     * Fetches a specific ticket's details.
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Integer bookingId) {
        // Fetch the booking details
        BookingResponseDTO response = bookingService.getBookingById(bookingId);

        // Return 200 OK status along with the ticket details
        return ResponseEntity.ok(response);
    }
}