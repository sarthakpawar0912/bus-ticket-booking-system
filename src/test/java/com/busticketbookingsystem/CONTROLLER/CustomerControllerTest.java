package com.busticketbookingsystem.CONTROLLER;

import com.busticketbookingsystem.customer.controller.CustomerController;
import com.busticketbookingsystem.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // ✅ FIX
import org.springframework.boot.test.mock.mockito.MockBean; // ✅ FIX
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService service;

    @Test
    void getAll_success() throws Exception {

        when(service.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk());
    }
}