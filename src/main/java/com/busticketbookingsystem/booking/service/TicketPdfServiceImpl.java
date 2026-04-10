package com.busticketbookingsystem.booking.service;

import com.busticketbookingsystem.booking.dto.BookingResponseDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TicketPdfServiceImpl implements TicketPdfService {

    private final BookingService bookingService;

    // Inject your existing BookingService to get the ticket details
    public TicketPdfServiceImpl(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public byte[] generateTicketPdf(Integer bookingId) {
        // 1. Fetch the ticket details from the database
        BookingResponseDTO ticket = bookingService.getBookingById(bookingId);

        // 2. Prepare the memory stream to hold the PDF data
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 3. Create a new PDF Document
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // --- DESIGNING THE TICKET ---

            // Title Font
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("BUS TICKET RESERVATION", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" ")); // Blank line
            document.add(new Paragraph("---------------------------------------------------------"));

            // Normal Font for details
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // Add Ticket Details
            document.add(new Paragraph("Booking Reference (PNR): " + ticket.bookingId(), textFont));
            document.add(new Paragraph("Status: " + ticket.status(), textFont));
            document.add(new Paragraph("Trip ID: " + ticket.tripId(), textFont));
            document.add(new Paragraph("Seat Number: " + ticket.seatNumber(), textFont));
            document.add(new Paragraph("Total Fare: $" + ticket.fare(), textFont));

            document.add(new Paragraph("---------------------------------------------------------"));

            // Add timestamp
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            document.add(new Paragraph("Ticket Generated On: " + dtf.format(now), FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));

            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF ticket", e);
        }

        // Return the raw PDF bytes
        return out.toByteArray();
    }
}