package com.busticketbookingsystem.CONTROLLER;

import com.busticketbookingsystem.CUSTOMER.controller.AddressController;
import com.busticketbookingsystem.CUSTOMER.service.AddressService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService service;


    @Test
    void getAll_success() throws Exception {

        when(service.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk());
    }
}