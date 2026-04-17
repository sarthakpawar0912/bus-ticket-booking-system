package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.DriverRequestDTO;
import com.busticketbookingsystem.agency.dto.DriverResponseDTO;
import com.busticketbookingsystem.agency.service.AgencyOfficeService;
import com.busticketbookingsystem.agency.service.DriverService;
import com.busticketbookingsystem.customer.service.AddressService;
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

@WebMvcTest(DriverController.class)
class DriverControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private DriverService driverService;
    @MockBean private AgencyOfficeService agencyOfficeService;
    @MockBean private AddressService addressService;
    @Autowired private ObjectMapper objectMapper;

    private DriverRequestDTO request;
    private DriverResponseDTO response;

    @BeforeEach
    void setUp() {
        request = new DriverRequestDTO();
        request.setLicenseNumber("DL001");
        request.setName("Ramesh");
        request.setPhone("7777777777");
        request.setOfficeId(1);
        request.setAddressId(1);

        response = DriverResponseDTO.builder()
                .driverId(1).licenseNumber("DL001").name("Ramesh")
                .phone("7777777777").officeId(1).addressId(1).build();
    }

    @Test
    void addDriver_success() throws Exception {
        when(driverService.addDriver(any())).thenReturn(response);
        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(1));
    }

    @Test
    void addDriver_validationFails() throws Exception {
        DriverRequestDTO bad = new DriverRequestDTO();
        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllDrivers() throws Exception {
        when(driverService.getAllDrivers()).thenReturn(List.of(response));
        mockMvc.perform(get("/api/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getDriverById() throws Exception {
        when(driverService.getDriverById(1)).thenReturn(response);
        mockMvc.perform(get("/api/drivers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ramesh"));
    }

    @Test
    void getDriversByOffice() throws Exception {
        when(driverService.getDriversByOfficeId(1)).thenReturn(List.of(response));
        mockMvc.perform(get("/api/drivers/office/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateDriver() throws Exception {
        when(driverService.updateDriver(eq(1), any())).thenReturn(response);
        mockMvc.perform(put("/api/drivers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }


    @Test
    void listDriversView() throws Exception {
        when(driverService.getAllDrivers()).thenReturn(List.of(response));
        mockMvc.perform(get("/view/drivers"))
                .andExpect(status().isOk())
                .andExpect(view().name("driver/drivers"))
                .andExpect(model().attributeExists("drivers"));
    }

    @Test
    void showAddDriverForm() throws Exception {
        when(agencyOfficeService.getAll()).thenReturn(Collections.emptyList());
        when(addressService.getAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/view/drivers/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("driver/add-driver"))
                .andExpect(model().attributeExists("driver", "offices", "addresses"));
    }

    @Test
    void saveDriverView() throws Exception {
        when(driverService.addDriver(any())).thenReturn(response);
        mockMvc.perform(post("/view/drivers/save")
                        .param("licenseNumber", "DL001").param("name", "Ramesh")
                        .param("phone", "7777777777").param("officeId", "1")
                        .param("addressId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/drivers"));
    }


}
