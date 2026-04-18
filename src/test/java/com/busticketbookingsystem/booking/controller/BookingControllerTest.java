package com.busticketbookingsystem.booking.controller;

import com.busticketbookingsystem.booking.dto.BookingRequestDTO;
import com.busticketbookingsystem.booking.dto.BookingResponseDTO;
import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.entity.BookingStatus;
import com.busticketbookingsystem.booking.service.BookingService;
import com.busticketbookingsystem.booking.service.TicketPdfService;
import com.busticketbookingsystem.customer.dto.CustomerResponseDTO;
import com.busticketbookingsystem.customer.service.CustomerService;
import com.busticketbookingsystem.trip.service.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private BookingService bookingService;
    @MockBean private TicketPdfService ticketPdfService;
    @MockBean private TripService tripService;
    @MockBean private CustomerService customerService;
    @Autowired private ObjectMapper objectMapper;

    private BookingRequestDTO request;
    private BookingResponseDTO response;
    private Booking booking;

    @BeforeEach
    void setUp() {
        request = BookingRequestDTO.builder()
                .tripId(1).seatNumbers(List.of(1, 2)).customerId(10).build();

        response = BookingResponseDTO.builder()
                .message("Booking successful for 2 seat(s)")
                .bookingIds(List.of(101, 102))
                .totalFare(new BigDecimal("1000"))
                .customerId(10).build();

        booking = Booking.builder()
                .bookingId(101).seatNumber(1).status(BookingStatus.Booked).build();
    }

    @Test
    void initiateBooking_success() throws Exception {
        when(bookingService.initiateBooking(any())).thenReturn(response);
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingIds.length()").value(2))
                .andExpect(jsonPath("$.totalFare").value(1000));
    }

    @Test
    void initiateBooking_validationFails() throws Exception {
        BookingRequestDTO bad = BookingRequestDTO.builder().build();
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsForTrip() throws Exception {
        when(bookingService.getBookingsForTrip(1)).thenReturn(List.of(booking));
        mockMvc.perform(get("/api/bookings/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(101))
                .andExpect(jsonPath("$[0].status").value("Booked"));
    }

    @Test
    void apiGetBookingById() throws Exception {
        when(bookingService.getBookingById(101)).thenReturn(booking);
        mockMvc.perform(get("/api/bookings/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(101));
    }



    @Test
    void downloadTicketPdf() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(ticketPdfService.generateTicketPdf(101)).thenReturn(pdfBytes);
        mockMvc.perform(get("/api/bookings/101/ticket"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void downloadGroupBookingTicket() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(ticketPdfService.generateGroupBookingTicket(any())).thenReturn(pdfBytes);
        mockMvc.perform(get("/api/bookings/group-ticket").param("bookingIds", "101,102,103"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void listAvailableTripsView() throws Exception {
        when(tripService.getAllTrips()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/view/bookings"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/trips"))
                .andExpect(model().attributeExists("trips"));
    }

    @Test
    void showSeatSelection_onError_redirects() throws Exception {
        when(tripService.getById(anyInt())).thenThrow(new RuntimeException("Trip not found"));
        mockMvc.perform(get("/view/bookings/trip/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/bookings"));
    }

    @Test
    void showConfirmation_withoutFlash() throws Exception {
        BookingService.ConfirmationContext ctx = new BookingService.ConfirmationContext(
                List.of(101), List.of(5), new BigDecimal("500.00"),
                null, null, "Mumbai", "Pune", "2026-05-01");
        when(bookingService.buildConfirmationContext(101)).thenReturn(ctx);
        mockMvc.perform(get("/view/bookings/confirmation/101"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/confirmation"))
                .andExpect(model().attributeExists("booking"));
    }

    @Test
    void bookSeats_onError_redirects() throws Exception {
        when(bookingService.initiateBooking(any())).thenThrow(new RuntimeException("Seat taken"));
        mockMvc.perform(post("/view/bookings/book")
                        .param("tripId", "1").param("seatNumbers", "1", "2")
                        .param("customerId", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/bookings/trip/1"));
    }

    @Test
    void bookSeats_success_redirectsToConfirmation() throws Exception {
        when(bookingService.initiateBooking(any())).thenReturn(response);
        CustomerResponseDTO cust = new CustomerResponseDTO(10, "Sarthak", "s@e.com", "9876543210", "Mumbai");
        when(customerService.getById(10)).thenReturn(cust);
        when(tripService.getById(1)).thenThrow(new RuntimeException("skip trip enrich"));

        mockMvc.perform(post("/view/bookings/book")
                        .param("tripId", "1").param("seatNumbers", "1", "2")
                        .param("customerId", "10"))
                .andExpect(status().is3xxRedirection());
    }
}
