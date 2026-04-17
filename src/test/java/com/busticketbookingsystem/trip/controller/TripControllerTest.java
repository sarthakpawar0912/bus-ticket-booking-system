package com.busticketbookingsystem.trip.controller;

import com.busticketbookingsystem.agency.service.BusService;
import com.busticketbookingsystem.agency.service.DriverService;
import com.busticketbookingsystem.customer.service.AddressService;
import com.busticketbookingsystem.trip.dto.TripDTO;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.service.RouteService;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripController.class)
class TripControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private TripService tripService;
    @MockBean private RouteService routeService;
    @MockBean private BusService busService;
    @MockBean private AddressService addressService;
    @MockBean private DriverService driverService;
    @Autowired private ObjectMapper objectMapper;

    private TripDTO dto;
    private Trip trip;

    @BeforeEach
    void setUp() {
        dto = TripDTO.builder()
                .tripId(1).routeId(1).busId(1)
                .boardingAddressId(1).droppingAddressId(2)
                .departureTime(LocalDateTime.of(2026, 12, 25, 14, 0))
                .arrivalTime(LocalDateTime.of(2026, 12, 25, 17, 0))
                .driver1Id(1).driver2Id(2)
                .availableSeats(40).fare(new BigDecimal("500"))
                .tripDate(LocalDateTime.of(2026, 12, 25, 14, 0))
                .build();

        trip = new Trip();
        trip.setTripId(1);
        trip.setAvailableSeats(40);
        trip.setFare(new BigDecimal("500"));
    }

    @Test
    void createTrip() throws Exception {
        when(tripService.create(any(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(trip);
        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllTrips() throws Exception {
        when(tripService.getAllTrips()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk());
    }

    @Test
    void getTripById() throws Exception {
        when(tripService.getById(1)).thenReturn(trip);
        mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId").value(1));
    }

    @Test
    void updateTrip() throws Exception {
        when(tripService.update(anyInt(), any())).thenReturn(trip);
        mockMvc.perform(put("/api/trips/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTrip() throws Exception {
        doNothing().when(tripService).delete(1);
        mockMvc.perform(delete("/api/trips/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void searchTrips() throws Exception {
        when(tripService.searchTrips("Mumbai", "Pune")).thenReturn(List.of(trip));
        mockMvc.perform(get("/api/trips/search").param("from", "Mumbai").param("to", "Pune"))
                .andExpect(status().isOk());
    }

    @Test
    void listTripsView() throws Exception {
        when(tripService.getAllTrips()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/view/trips"))
                .andExpect(status().isOk())
                .andExpect(view().name("trip/trips"))
                .andExpect(model().attributeExists("trips"));
    }

    @Test
    void showAddTripForm() throws Exception {
        when(routeService.getAll()).thenReturn(Collections.emptyList());
        when(busService.getAllBuses()).thenReturn(Collections.emptyList());
        when(addressService.getAll()).thenReturn(Collections.emptyList());
        when(driverService.getAllDrivers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/view/trips/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("trip/add-trip"))
                .andExpect(model().attributeExists("routes", "buses", "addresses", "drivers"));
    }

    @Test
    void saveTripView_success() throws Exception {
        when(tripService.create(any(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(trip);
        mockMvc.perform(post("/view/trips/save")
                        .param("routeId", "1").param("busId", "1")
                        .param("boardingAddressId", "1").param("droppingAddressId", "2")
                        .param("departureTime", "2026-12-25T14:00")
                        .param("arrivalTime", "2026-12-25T17:00")
                        .param("driver1Id", "1").param("driver2Id", "2")
                        .param("availableSeats", "40").param("fare", "500")
                        .param("tripDate", "2026-12-25T14:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/trips"));
    }

    @Test
    void deleteTripView() throws Exception {
        doNothing().when(tripService).delete(1);
        mockMvc.perform(get("/view/trips/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/trips"));
    }
}
