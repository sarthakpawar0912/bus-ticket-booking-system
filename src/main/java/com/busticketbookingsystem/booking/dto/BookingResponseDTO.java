package com.busticketbookingsystem.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {

    private String message;

    // The list of primary keys generated in your bookings table
    private List<Long> generatedBookingIds;

    // The total bill (calculated by your service: fare * number of seats)
    private Double totalFare;

    // Sent back so the frontend can easily route to the payment page
    private Long customerId;
}