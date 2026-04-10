package com.busticketbookingsystem.booking.service;

public interface TicketPdfService {

    /**
     * Generates a PDF ticket as a byte array for a specific booking.
     */
    byte[] generateTicketPdf(Integer bookingId);
}