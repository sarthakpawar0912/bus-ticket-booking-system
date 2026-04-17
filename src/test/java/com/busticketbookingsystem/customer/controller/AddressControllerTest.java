package com.busticketbookingsystem.customer.controller;

import com.busticketbookingsystem.customer.entity.Address;
import com.busticketbookingsystem.customer.service.AddressService;
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

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AddressService addressService;
    @Autowired private ObjectMapper objectMapper;

    private Address address;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .addressId(1).address("Station Road").city("Mumbai")
                .state("MH").zipCode("400001").build();
    }

    @Test
    void createAddress() throws Exception {
        when(addressService.create(any())).thenReturn(address);
        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(1));
    }

    @Test
    void getAllAddresses() throws Exception {
        when(addressService.getAll()).thenReturn(List.of(address));
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAddressById() throws Exception {
        when(addressService.getById(1)).thenReturn(address);
        mockMvc.perform(get("/api/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Mumbai"));
    }

    @Test
    void updateAddress() throws Exception {
        when(addressService.update(eq(1), any())).thenReturn(address);
        mockMvc.perform(put("/api/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address)))
                .andExpect(status().isOk());
    }



    @Test
    void listAddressesView() throws Exception {
        when(addressService.getAll()).thenReturn(List.of(address));
        mockMvc.perform(get("/view/addresses"))
                .andExpect(status().isOk())
                .andExpect(view().name("address/addresses"))
                .andExpect(model().attributeExists("addresses"));
    }

    @Test
    void showAddForm() throws Exception {
        mockMvc.perform(get("/view/addresses/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("address/add-address"))
                .andExpect(model().attributeExists("address"));
    }

    @Test
    void saveAddressView() throws Exception {
        when(addressService.create(any())).thenReturn(address);
        mockMvc.perform(post("/view/addresses/save")
                        .param("address", "Station Road").param("city", "Mumbai")
                        .param("state", "MH").param("zipCode", "400001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/addresses"));
    }

}
