package com.busticketbookingsystem.booking.dto;

import lombok.Data;
import java.util.List;

@Data
public class BookingRequestDTO {

    private Long tripId;

    // An array of the seats the user clicked on the grid (e.g., [12, 13, 14])
    private List<Integer> seatNumbers;

    // You don't save this in your bookings table, but you NEED it from the frontend
    // so you can pass it to Member 5's Payment system later!
    private Long customerId;
}