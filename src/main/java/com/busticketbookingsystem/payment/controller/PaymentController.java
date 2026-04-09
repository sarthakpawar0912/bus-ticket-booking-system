package com.busticketbookingsystem.payment.controller;

import com.busticketbookingsystem.payment.dto.PaymentRequestDTO;
import com.busticketbookingsystem.payment.dto.PaymentResponseDTO;
import com.busticketbookingsystem.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ✅ Process Payment
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @Valid @RequestBody PaymentRequestDTO request) {

        PaymentResponseDTO response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    // ✅ Refund Payment
    @PostMapping("/refund/{id}")
    public ResponseEntity<PaymentResponseDTO> refund(@PathVariable Integer id) {

        PaymentResponseDTO response = paymentService.refund(id);
        return ResponseEntity.ok(response);
    }

    // ✅ Get Total Revenue
    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getRevenue() {

        return ResponseEntity.ok(paymentService.getTotalRevenue());
    }
}