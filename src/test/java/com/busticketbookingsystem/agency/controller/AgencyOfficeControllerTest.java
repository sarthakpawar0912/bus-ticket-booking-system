package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.AgencyOfficeDTO;
import com.busticketbookingsystem.agency.service.AgencyOfficeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgencyOfficeController.class)
class AgencyOfficeControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AgencyOfficeService agencyOfficeService;
    @Autowired private ObjectMapper objectMapper;

    private AgencyOfficeDTO dto;

    @BeforeEach
    void setUp() {
        dto = AgencyOfficeDTO.builder()
                .officeId(1).agencyId(1).officeMail("a@b.com")
                .officeContactPersonName("Amit").officeContactNumber("9876543210")
                .officeAddressId(1).build();
    }

    @Test
    void createOffice_success() throws Exception {
        when(agencyOfficeService.create(any())).thenReturn(dto);
        mockMvc.perform(post("/api/offices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.officeId").value(1));
    }

    @Test
    void getAllOffices() throws Exception {
        when(agencyOfficeService.getAll()).thenReturn(List.of(dto));
        mockMvc.perform(get("/api/offices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getOfficeById() throws Exception {
        when(agencyOfficeService.getById(1)).thenReturn(dto);
        mockMvc.perform(get("/api/offices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.officeId").value(1));
    }

    @Test
    void getOfficesByAgencyId() throws Exception {
        when(agencyOfficeService.getByAgencyId(1)).thenReturn(List.of(dto));
        mockMvc.perform(get("/api/offices/agency/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].agencyId").value(1));
    }

    @Test
    void updateOffice() throws Exception {
        when(agencyOfficeService.update(eq(1), any())).thenReturn(dto);
        mockMvc.perform(put("/api/offices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }



    @Test
    void createOffice_validationFails() throws Exception {
        AgencyOfficeDTO bad = new AgencyOfficeDTO();
        mockMvc.perform(post("/api/offices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }
}
