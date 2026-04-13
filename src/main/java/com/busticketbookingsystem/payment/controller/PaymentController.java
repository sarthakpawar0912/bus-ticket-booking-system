package com.busticketbookingsystem.payment.controller;

import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.service.BookingService;
import com.busticketbookingsystem.booking.service.TicketPdfService;
import com.busticketbookingsystem.customer.service.CustomerService;
import com.busticketbookingsystem.payment.dto.PaymentRequestDTO;
import com.busticketbookingsystem.payment.dto.PaymentResponseDTO;
import com.busticketbookingsystem.payment.service.PaymentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class PaymentController {

    private static final String ATTR_SEAT_COUNT = "seatCount";
    private static final String ATTR_ALL_PAYMENT_IDS = "allPaymentIds";

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final CustomerService customerService;
    private final TicketPdfService ticketPdfService;

    public PaymentController(PaymentService paymentService,
                             BookingService bookingService,
                             CustomerService customerService,
                             TicketPdfService ticketPdfService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.customerService = customerService;
        this.ticketPdfService = ticketPdfService;
    }

    // ======================== REST API ========================

    @PostMapping("/api/payments")
    @ResponseBody
    public ResponseEntity<PaymentResponseDTO> processPayment(@RequestBody PaymentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request));
    }

    @GetMapping("/api/payments")
    @ResponseBody
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/api/payments/{id}")
    @ResponseBody
    public PaymentResponseDTO getPaymentById(@PathVariable Integer id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/api/payments/booking/{bookingId}")
    @ResponseBody
    public PaymentResponseDTO getPaymentByBookingId(@PathVariable Integer bookingId) {
        return paymentService.getPaymentByBookingId(bookingId);
    }

    @GetMapping("/api/payments/customer/{customerId}")
    @ResponseBody
    public List<PaymentResponseDTO> getPaymentsByCustomerId(@PathVariable Integer customerId) {
        return paymentService.getPaymentsByCustomerId(customerId);
    }

    @PostMapping("/api/payments/{id}/refund")
    @ResponseBody
    public PaymentResponseDTO refundPayment(@PathVariable Integer id) {
        return paymentService.refundPayment(id);
    }

    @GetMapping("/api/payments/{id}/ticket")
    @ResponseBody
    public ResponseEntity<byte[]> downloadTicketByPayment(@PathVariable Integer id) {
        byte[] pdfBytes = ticketPdfService.generateTicketByPaymentId(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ticket-payment-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/api/payments/group-ticket")
    @ResponseBody
    public ResponseEntity<byte[]> downloadGroupTicket(@RequestParam String paymentIds) {
        String[] ids = paymentIds.split(",");
        List<Integer> pidList = new java.util.ArrayList<>();
        for (String s : ids) pidList.add(Integer.parseInt(s.trim()));
        byte[] pdfBytes = ticketPdfService.generateGroupTicket(pidList);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "group-ticket.pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view/payments")
    public String listPayments(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        return "payment/payments";
    }

    @GetMapping("/view/payments/pay/{bookingId}")
    public String showCheckout(@PathVariable Integer bookingId, Model model) {
        Booking booking = bookingService.getBookingById(bookingId);
        model.addAttribute("bookingIds", String.valueOf(bookingId));
        model.addAttribute(ATTR_SEAT_COUNT, 1);
        model.addAttribute("seatNumbers", String.valueOf(booking.getSeatNumber()));
        model.addAttribute("amount", booking.getTrip().getFare());
        model.addAttribute("customers", customerService.getAll());
        return "payment/checkout";
    }

    @GetMapping("/view/payments/pay-all")
    public String showCheckoutAll(@RequestParam String bookingIds,
                                  @RequestParam(required = false) Integer customerId,
                                  Model model) {
        String[] ids = bookingIds.split(",");
        BigDecimal totalFare = BigDecimal.ZERO;
        StringBuilder seatNums = new StringBuilder();
        for (String idStr : ids) {
            Booking b = bookingService.getBookingById(Integer.parseInt(idStr.trim()));
            totalFare = totalFare.add(b.getTrip().getFare());
            if (!seatNums.isEmpty()) seatNums.append(", ");
            seatNums.append(b.getSeatNumber());
        }
        model.addAttribute("bookingIds", bookingIds);
        model.addAttribute(ATTR_SEAT_COUNT, ids.length);
        model.addAttribute("seatNumbers", seatNums.toString());
        model.addAttribute("amount", totalFare);
        model.addAttribute("preSelectedCustomerId", customerId);
        model.addAttribute("customers", customerService.getAll());
        return "payment/checkout";
    }

    @PostMapping("/view/payments/process")
    public String processPaymentView(@RequestParam String bookingIds,
                                     @RequestParam Integer customerId,
                                     @RequestParam BigDecimal amount,
                                     RedirectAttributes ra) {
        try {
            String[] ids = bookingIds.split(",");
            BigDecimal perBookingAmount = amount.divide(BigDecimal.valueOf(ids.length), 2, java.math.RoundingMode.HALF_UP);
            List<Integer> paymentIds = new java.util.ArrayList<>();

            for (String idStr : ids) {
                Integer bookingId = Integer.parseInt(idStr.trim());
                PaymentRequestDTO request = new PaymentRequestDTO(bookingId, customerId, perBookingAmount);
                PaymentResponseDTO response = paymentService.processPayment(request);
                paymentIds.add(response.getPaymentId());
            }

            ra.addFlashAttribute(ATTR_ALL_PAYMENT_IDS, paymentIds);
            ra.addFlashAttribute("totalAmount", amount);
            ra.addFlashAttribute(ATTR_SEAT_COUNT, ids.length);
            return "redirect:/view/payments/success/" + paymentIds.get(0);
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/view/payments/pay-all?bookingIds=" + bookingIds;
        }
    }

    @GetMapping("/view/payments/success/{paymentId}")
    public String showSuccess(@PathVariable Integer paymentId, Model model) {
        PaymentResponseDTO payment = paymentService.getPaymentById(paymentId);
        model.addAttribute("payment", payment);

        // If allPaymentIds was passed via flash, use it; otherwise single payment
        if (!model.containsAttribute(ATTR_ALL_PAYMENT_IDS)) {
            model.addAttribute(ATTR_ALL_PAYMENT_IDS, List.of(paymentId));
            model.addAttribute("totalAmount", payment.getAmount());
            model.addAttribute(ATTR_SEAT_COUNT, 1);
        }
        return "payment/success";
    }
}
