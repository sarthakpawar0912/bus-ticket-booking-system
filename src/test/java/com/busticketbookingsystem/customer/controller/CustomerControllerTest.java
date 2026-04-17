package com.busticketbookingsystem.customer.controller;

import com.busticketbookingsystem.customer.dto.CustomerRequestDTO;
import com.busticketbookingsystem.customer.dto.CustomerResponseDTO;
import com.busticketbookingsystem.customer.service.AddressService;
import com.busticketbookingsystem.customer.service.CustomerService;
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

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CustomerService customerService;
    @MockBean private AddressService addressService;
    @Autowired private ObjectMapper objectMapper;

    private CustomerRequestDTO request;
    private CustomerResponseDTO response;

    @BeforeEach
    void setUp() {
        request = new CustomerRequestDTO();
        request.setName("Sarthak");
        request.setEmail("sarthak@email.com");
        request.setPhone("9876543210");
        request.setAddressId(1);

        response = new CustomerResponseDTO(1, "Sarthak", "sarthak@email.com", "9876543210", "Mumbai");
    }

    @Test
    void createCustomer() throws Exception {
        when(customerService.create(any())).thenReturn(response);
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createCustomer_validationFails() throws Exception {
        CustomerRequestDTO bad = new CustomerRequestDTO();
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCustomers() throws Exception {
        when(customerService.getAll()).thenReturn(List.of(response));
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getById() throws Exception {
        when(customerService.getById(1)).thenReturn(response);
        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sarthak"));
    }

    @Test
    void updateCustomer() throws Exception {
        when(customerService.update(eq(1), any())).thenReturn(response);
        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void patchCustomer() throws Exception {
        when(customerService.patch(eq(1), any())).thenReturn(response);
        mockMvc.perform(patch("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCustomer() throws Exception {
        doNothing().when(customerService).delete(1);
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Customer deleted"));
    }

    @Test
    void listCustomersView() throws Exception {
        when(customerService.getAll()).thenReturn(List.of(response));
        mockMvc.perform(get("/view/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customers"))
                .andExpect(model().attributeExists("customers"));
    }

    @Test
    void showAddCustomerForm() throws Exception {
        when(addressService.getAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/view/customers/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/add-customer"))
                .andExpect(model().attributeExists("customer", "addresses"));
    }

    @Test
    void saveCustomerView() throws Exception {
        when(customerService.create(any())).thenReturn(response);
        mockMvc.perform(post("/view/customers/save")
                        .param("name", "Sarthak").param("email", "s@e.com")
                        .param("phone", "9876543210").param("addressId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/customers"));
    }

    @Test
    void showEditCustomerForm() throws Exception {
        when(customerService.getById(1)).thenReturn(response);
        when(addressService.getAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/view/customers/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/update-customer"));
    }
}
