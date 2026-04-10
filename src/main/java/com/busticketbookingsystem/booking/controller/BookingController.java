package com.busticketbookingsystem.booking.controller;

import com.busticketbookingsystem.booking.dto.BookingRequestDTO;
import com.busticketbookingsystem.booking.dto.BookingResponseDTO;
import com.busticketbookingsystem.booking.service.BookingService;

// 🌟 NEW IMPORTS FOR PDF GENERATION
import com.busticketbookingsystem.booking.service.TicketPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final TicketPdfService ticketPdfService; // 🌟 NEW: Declare the PDF Service

    // 🌟 NEW: Updated Constructor Injection to include both services
    public BookingController(BookingService bookingService, TicketPdfService ticketPdfService) {
        this.bookingService = bookingService;
        this.ticketPdfService = ticketPdfService;
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

    /**
     * 🌟 NEW ENDPOINT: GET /api/bookings/{bookingId}/pdf
     * Downloads the ticket as a PDF file!
     */
    @GetMapping("/{bookingId}/pdf")
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable Integer bookingId) {

        // 1. Generate the PDF byte array from our new service
        byte[] pdfBytes = ticketPdfService.generateTicketPdf(bookingId);

        // 2. Set up the HTTP Headers to tell the browser this is a downloadable file
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "BusTicket_" + bookingId + ".pdf");

        // 3. Return the raw bytes back to the user
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}