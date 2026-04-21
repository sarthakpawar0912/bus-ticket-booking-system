package com.busticketbookingsystem.payment.controller;

import com.busticketbookingsystem.booking.service.BookingService;
import com.busticketbookingsystem.booking.service.TicketPdfService;
import com.busticketbookingsystem.customer.service.CustomerService;
import com.busticketbookingsystem.payment.dto.PaymentRequestDTO;
import com.busticketbookingsystem.payment.dto.PaymentResponseDTO;
import com.busticketbookingsystem.payment.entity.PaymentStatus;
import com.busticketbookingsystem.payment.service.PaymentService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PaymentService paymentService;
    @MockBean private BookingService bookingService;
    @MockBean private CustomerService customerService;
    @MockBean private TicketPdfService ticketPdfService;
    @Autowired private ObjectMapper objectMapper;

    private PaymentRequestDTO request;
    private PaymentResponseDTO response;

    @BeforeEach
    void setUp() {
        request = new PaymentRequestDTO(1, 10, new BigDecimal("500"));

        response = PaymentResponseDTO.builder()
                .paymentId(201).bookingId(1).customerId(10)
                .amount(new BigDecimal("500"))
                .paymentStatus(PaymentStatus.Success)
                .paymentDate(LocalDateTime.now())
                .message("Payment processed successfully")
                .hasValidBooking(true).build();
    }

    @Test
    void processPayment() throws Exception {
        when(paymentService.processPayment(any())).thenReturn(response);
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(201))
                .andExpect(jsonPath("$.paymentStatus").value("Success"));
    }

    @Test
    void getAllPayments() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of(response));
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPaymentById() throws Exception {
        when(paymentService.getPaymentById(201)).thenReturn(response);
        mockMvc.perform(get("/api/payments/201"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(201));
    }

    @Test
    void getPaymentByBookingId() throws Exception {
        when(paymentService.getPaymentByBookingId(1)).thenReturn(response);
        mockMvc.perform(get("/api/payments/booking/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(1));
    }

    @Test
    void getPaymentsByCustomerId() throws Exception {
        when(paymentService.getPaymentsByCustomerId(10)).thenReturn(List.of(response));
        mockMvc.perform(get("/api/payments/customer/10"))
                .andExpect(status().isOk());
    }

    @Test
    void downloadTicketByPayment() throws Exception {
        // The controller checks siblings; mock getAllPayments to return a single payment.
        when(paymentService.getPaymentById(201)).thenReturn(response);
        when(paymentService.getAllPayments()).thenReturn(List.of(response));
        when(ticketPdfService.generateTicketByPaymentId(201)).thenReturn(new byte[]{1, 2, 3});
        mockMvc.perform(get("/api/payments/201/ticket"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void downloadGroupTicket() throws Exception {
        when(ticketPdfService.generateGroupTicket(any())).thenReturn(new byte[]{1, 2, 3});
        mockMvc.perform(get("/api/payments/group-ticket").param("paymentIds", "201,202,203"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void listPaymentsView() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of(response));
        mockMvc.perform(get("/view/payments"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/payments"))
                .andExpect(model().attributeExists("payments"));
    }

    @Test
    void showSuccessView() throws Exception {
        when(paymentService.getPaymentById(201)).thenReturn(response);
        mockMvc.perform(get("/view/payments/success/201"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/success"))
                .andExpect(model().attributeExists("payment"));
    }

    @Test
    void processPaymentView_errorRedirects() throws Exception {
        when(paymentService.processPayment(any())).thenThrow(new RuntimeException("boom"));
        mockMvc.perform(post("/view/payments/process")
                        .param("bookingIds", "1,2").param("customerId", "10")
                        .param("amount", "1000"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void showCheckoutAll_whenBookingLookupFails_returns5xx() throws Exception {
        when(customerService.getAll()).thenReturn(Collections.emptyList());
        when(bookingService.getBookingById(any())).thenThrow(new RuntimeException("skip"));
        // Controller has no try/catch — MockMvc surfaces the failure as a 5xx status.
        mockMvc.perform(get("/view/payments/pay-all").param("bookingIds", "1"))
                .andExpect(status().is5xxServerError());
    }
}
