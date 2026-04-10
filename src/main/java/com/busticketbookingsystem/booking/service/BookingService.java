package com.busticketbookingsystem.booking.service;

import com.busticketbookingsystem.booking.dto.BookingRequestDTO;
import com.busticketbookingsystem.booking.dto.BookingResponseDTO;

public interface BookingService {

    /**
     * Attempts to reserve a specific seat on a specific trip.
     * Uses pessimistic locking to prevent double-booking.
     */
    BookingResponseDTO reserveSeat(BookingRequestDTO request);

    /**
     * Fetches the details of a specific booking/ticket.
     */
    BookingResponseDTO getBookingById(Integer bookingId);
}