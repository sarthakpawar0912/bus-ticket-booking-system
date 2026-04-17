package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.BusRequestDTO;
import com.busticketbookingsystem.agency.dto.BusResponseDTO;
import com.busticketbookingsystem.agency.service.AgencyOfficeService;
import com.busticketbookingsystem.agency.service.BusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BusController.class)
class BusControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private BusService busService;
    @MockBean private AgencyOfficeService agencyOfficeService;
    @Autowired private ObjectMapper objectMapper;

    private BusRequestDTO request;
    private BusResponseDTO response;

    @BeforeEach
    void setUp() {
        request = new BusRequestDTO();
        request.setOfficeId(1);
        request.setRegistrationNumber("MH01AB1234");
        request.setCapacity(40);
        request.setType("AC");

        response = BusResponseDTO.builder()
                .busId(1).officeId(1).registrationNumber("MH01AB1234")
                .capacity(40).type("AC").build();
    }

    @Test
    void addBus_success() throws Exception {
        when(busService.addBus(any())).thenReturn(response);
        mockMvc.perform(post("/api/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.busId").value(1));
    }

    @Test
    void addBus_validationFails() throws Exception {
        BusRequestDTO bad = new BusRequestDTO();
        bad.setCapacity(5); // below min=10
        mockMvc.perform(post("/api/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllBuses() throws Exception {
        when(busService.getAllBuses()).thenReturn(List.of(response));
        mockMvc.perform(get("/api/buses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getBusById() throws Exception {
        when(busService.getBusById(1)).thenReturn(response);
        mockMvc.perform(get("/api/buses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("MH01AB1234"));
    }

    @Test
    void getBusesByOffice() throws Exception {
        when(busService.getBusesByOfficeId(1)).thenReturn(List.of(response));
        mockMvc.perform(get("/api/buses/office/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].officeId").value(1));
    }

    @Test
    void updateBus() throws Exception {
        when(busService.updateBus(eq(1), any())).thenReturn(response);
        mockMvc.perform(put("/api/buses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }



    @Test
    void listBusesView() throws Exception {
        when(busService.getAllBuses()).thenReturn(List.of(response));
        mockMvc.perform(get("/view/buses"))
                .andExpect(status().isOk())
                .andExpect(view().name("bus/buses"))
                .andExpect(model().attributeExists("buses"));
    }

    @Test
    void showAddBusForm() throws Exception {
        when(agencyOfficeService.getAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/view/buses/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("bus/add-bus"))
                .andExpect(model().attributeExists("bus", "offices"));
    }

    @Test
    void saveBusView() throws Exception {
        when(busService.addBus(any())).thenReturn(response);
        mockMvc.perform(post("/view/buses/save")
                        .param("officeId", "1").param("registrationNumber", "MH01AB1234")
                        .param("capacity", "40").param("type", "AC"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/buses"));
    }

    @Test
    void showEditForm() throws Exception {
        when(busService.getBusById(1)).thenReturn(response);
        when(agencyOfficeService.getAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/view/buses/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("bus/update-bus"));
    }

    @Test
    void deleteBusView() throws Exception {
        doNothing().when(busService).deleteBus(1);
        mockMvc.perform(get("/view/buses/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/buses"));
    }
}
