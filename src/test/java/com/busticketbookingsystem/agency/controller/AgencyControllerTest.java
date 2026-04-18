package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.AgencyRequestDTO;
import com.busticketbookingsystem.agency.dto.AgencyResponseDTO;
import com.busticketbookingsystem.agency.dto.OfficeRequestDTO;
import com.busticketbookingsystem.agency.dto.OfficeResponseDTO;
import com.busticketbookingsystem.agency.service.AgencyService;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@WebMvcTest(AgencyController.class)
class AgencyControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AgencyService agencyService;
    @Autowired private ObjectMapper objectMapper;

    private AgencyRequestDTO request;
    private AgencyResponseDTO response;
    private OfficeRequestDTO officeRequest;
    private OfficeResponseDTO officeResponse;

    @BeforeEach
    void setUp() {
        request = new AgencyRequestDTO();
        request.setName("Red Bus");
        request.setContactPersonName("Sarthak");
        request.setEmail("red@bus.com");
        request.setPhone("9876543210");

        response = AgencyResponseDTO.builder()
                .agencyId(1).name("Red Bus").contactPersonName("Sarthak")
                .email("red@bus.com").phone("9876543210").build();

        officeRequest = new OfficeRequestDTO();
        officeRequest.setAgencyId(1);
        officeRequest.setOfficeMail("mumbai@red.com");
        officeRequest.setOfficeContactPersonName("Amit");
        officeRequest.setOfficeContactNumber("9999999999");
        officeRequest.setOfficeAddressId(1);

        officeResponse = OfficeResponseDTO.builder()
                .officeId(1).agencyId(1).officeAddressId(1)
                .officeMail("mumbai@red.com").officeContactPersonName("Amit")
                .officeContactNumber("9999999999").build();
    }

    @Test
    @DisplayName("POST /api/agencies returns 201")
    void createAgency_success() throws Exception {
        when(agencyService.createAgency(any())).thenReturn(response);
        mockMvc.perform(post("/api/agencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.agencyId").value(1))
                .andExpect(jsonPath("$.name").value("Red Bus"));
    }

    @Test
    @DisplayName("POST /api/agencies with invalid data returns 400")
    void createAgency_validationFails() throws Exception {
        AgencyRequestDTO bad = new AgencyRequestDTO(); // all fields null
        mockMvc.perform(post("/api/agencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/agencies returns list")
    void getAllAgencies() throws Exception {
        when(agencyService.getAllAgencies()).thenReturn(List.of(response));
        mockMvc.perform(get("/api/agencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].agencyId").value(1));
    }

    @Test
    @DisplayName("GET /api/agencies/{id} returns agency")
    void getAgencyById() throws Exception {
        when(agencyService.getAgencyById(1)).thenReturn(response);
        mockMvc.perform(get("/api/agencies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Red Bus"));
    }

    @Test
    @DisplayName("GET /api/agencies/{id} returns 404 when not found")
    void getAgencyById_notFound() throws Exception {
        when(agencyService.getAgencyById(999)).thenThrow(new ResourceNotFoundException("Agency not found"));
        mockMvc.perform(get("/api/agencies/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/agencies/{id} updates")
    void updateAgency() throws Exception {
        when(agencyService.updateAgency(eq(1), any())).thenReturn(response);
        mockMvc.perform(put("/api/agencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agencyId").value(1));
    }



    @Test
    @DisplayName("GET /api/agencies/offices returns list")
    void getAllOffices() throws Exception {
        when(agencyService.getAllOffices()).thenReturn(List.of(officeResponse));
        mockMvc.perform(get("/api/agencies/offices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].officeId").value(1));
    }

    @Test
    @DisplayName("POST /api/agencies/offices creates office")
    void createOffice() throws Exception {
        when(agencyService.createOffice(any())).thenReturn(officeResponse);
        mockMvc.perform(post("/api/agencies/offices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(officeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.officeId").value(1));
    }

    @Test
    @DisplayName("PUT /api/agencies/offices/{id} updates office")
    void updateOffice() throws Exception {
        when(agencyService.updateOffice(eq(1), any())).thenReturn(officeResponse);
        mockMvc.perform(put("/api/agencies/offices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(officeRequest)))
                .andExpect(status().isOk());
    }



    @Test
    @DisplayName("GET /view/agencies renders template")
    void listAgenciesView() throws Exception {
        when(agencyService.getAllAgencies()).thenReturn(List.of(response));
        mockMvc.perform(get("/view/agencies"))
                .andExpect(status().isOk())
                .andExpect(view().name("agency/agencies"))
                .andExpect(model().attributeExists("agencies"));
    }

    @Test
    @DisplayName("GET /view/agencies/add renders form")
    void showAddForm() throws Exception {
        mockMvc.perform(get("/view/agencies/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("agency/add-agency"))
                .andExpect(model().attributeExists("agency"));
    }

    @Test
    @DisplayName("POST /view/agencies/save redirects")
    void saveAgencyView() throws Exception {
        when(agencyService.createAgency(any())).thenReturn(response);
        mockMvc.perform(post("/view/agencies/save")
                        .param("name", "Red Bus")
                        .param("contactPersonName", "Sarthak")
                        .param("email", "red@bus.com")
                        .param("phone", "9876543210"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/agencies"));
    }

    @Test
    @DisplayName("GET /view/agencies/edit/{id} renders edit form")
    void showEditForm() throws Exception {
        when(agencyService.getAgencyById(1)).thenReturn(response);
        mockMvc.perform(get("/view/agencies/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("agency/update-agency"))
                .andExpect(model().attributeExists("agency"));
    }

    @Test
    @DisplayName("POST /view/agencies/update/{id} redirects")
    void updateAgencyView() throws Exception {
        when(agencyService.updateAgency(eq(1), any())).thenReturn(response);
        mockMvc.perform(post("/view/agencies/update/1")
                        .param("name", "Blue").param("contactPersonName", "X")
                        .param("email", "b@b.com").param("phone", "9000000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/agencies"));
    }
}
