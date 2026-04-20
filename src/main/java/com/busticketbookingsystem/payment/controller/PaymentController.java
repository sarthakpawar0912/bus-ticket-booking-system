package com.busticketbookingsystem.payment.controller;

import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.service.BookingService;
import com.busticketbookingsystem.booking.service.TicketPdfService;
import com.busticketbookingsystem.customer.service.CustomerService;
import com.busticketbookingsystem.payment.dto.PaymentRequestDTO;
import com.busticketbookingsystem.payment.dto.PaymentResponseDTO;
import com.busticketbookingsystem.payment.service.PaymentService;
import jakarta.validation.Valid;
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
    public ResponseEntity<PaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequestDTO request) {
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

    /**
     * Download ticket by payment ID. If the payment belongs to a multi-seat group
     * (same customer + same second-level timestamp), auto-generates a consolidated
     * group ticket covering every seat in that transaction. Otherwise generates a
     * single ticket. This is the ONLY download entry point the user needs.
     */
    @GetMapping("/api/payments/{id}/ticket")
    @ResponseBody
    public ResponseEntity<byte[]> downloadTicketByPayment(@PathVariable Integer id) {
        PaymentResponseDTO target = paymentService.getPaymentById(id);
        List<Integer> sibling = paymentService.getAllPayments().stream()
                .filter(p -> java.util.Objects.equals(p.getCustomerId(), target.getCustomerId())
                        && p.getPaymentDate() != null && target.getPaymentDate() != null
                        && p.getPaymentDate().withNano(0).equals(target.getPaymentDate().withNano(0))
                        && p.getPaymentStatus() == target.getPaymentStatus())
                .map(PaymentResponseDTO::getPaymentId)
                .toList();

        byte[] pdfBytes;
        String filename;
        if (sibling.size() > 1) {
            pdfBytes = ticketPdfService.generateGroupTicket(sibling);
            filename = "ticket-group-" + id + ".pdf";
        } else {
            pdfBytes = ticketPdfService.generateTicketByPaymentId(id);
            filename = "ticket-payment-" + id + ".pdf";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
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
        List<PaymentResponseDTO> all = paymentService.getAllPayments();
        model.addAttribute("payments", all);
        model.addAttribute("paymentGroups", groupRelatedPayments(all));
        return "payment/payments";
    }

    /**
     * Groups payments that were processed together (same customer, same
     * second-level timestamp). A 10-seat booking creates 10 Payment rows;
     * we collapse them to ONE group so the UI shows one row with a single
     * "Download Group Ticket" action instead of 10 separate ticket buttons.
     */
    private List<com.busticketbookingsystem.payment.dto.PaymentGroup> groupRelatedPayments(
            List<PaymentResponseDTO> all) {
        java.util.Map<String, List<PaymentResponseDTO>> buckets = new java.util.LinkedHashMap<>();
        for (PaymentResponseDTO p : all) {
            String ts = p.getPaymentDate() == null ? "null"
                    : p.getPaymentDate().withNano(0).toString();
            String key = p.getCustomerId() + "|" + ts + "|" + p.getPaymentStatus();
            buckets.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(p);
        }
        List<com.busticketbookingsystem.payment.dto.PaymentGroup> result = new java.util.ArrayList<>();
        for (List<PaymentResponseDTO> bucket : buckets.values()) {
            PaymentResponseDTO first = bucket.get(0);
            BigDecimal total = bucket.stream()
                    .map(PaymentResponseDTO::getAmount)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.add(com.busticketbookingsystem.payment.dto.PaymentGroup.builder()
                    .paymentIds(bucket.stream().map(PaymentResponseDTO::getPaymentId).toList())
                    .bookingIds(bucket.stream().map(PaymentResponseDTO::getBookingId).toList())
                    .customerId(first.getCustomerId())
                    .totalAmount(total)
                    .paymentStatus(first.getPaymentStatus())
                    .paymentDate(first.getPaymentDate())
                    .seatCount(bucket.size())
                    .build());
        }
        // Sort newest first so the most recent booking always shows up on top
        result.sort((a, b) -> {
            if (a.getPaymentDate() == null && b.getPaymentDate() == null) return 0;
            if (a.getPaymentDate() == null) return 1;
            if (b.getPaymentDate() == null) return -1;
            return b.getPaymentDate().compareTo(a.getPaymentDate());
        });
        return result;
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

    /**
     * Creates ONE Payment row per Booking, sharing a single paymentDate so the
     * group is re-detectable. Total charged = sum of per-seat fares; mismatches
     * are rejected. After success every booking is marked PAID.
     */
    @PostMapping("/view/payments/process")
    public String processPaymentView(@RequestParam String bookingIds,
                                     @RequestParam Integer customerId,
                                     @RequestParam BigDecimal amount,
                                     RedirectAttributes ra) {
        try {
            String[] idStrs = bookingIds.split(",");
            List<Integer> bookingIdList = new java.util.ArrayList<>();
            for (String s : idStrs) bookingIdList.add(Integer.parseInt(s.trim()));

            List<PaymentResponseDTO> responses =
                    paymentService.processPaymentsForBookings(bookingIdList, customerId, amount);

            List<Integer> paymentIds = responses.stream()
                    .map(PaymentResponseDTO::getPaymentId)
                    .toList();
            Integer firstPaymentId = paymentIds.get(0);
            BigDecimal perSeatFare = responses.get(0).getAmount();

            ra.addFlashAttribute(ATTR_ALL_PAYMENT_IDS, paymentIds);
            ra.addFlashAttribute("totalAmount", amount);
            ra.addFlashAttribute("perSeatFare", perSeatFare);
            ra.addFlashAttribute(ATTR_SEAT_COUNT, bookingIdList.size());
            ra.addFlashAttribute("allBookingIds", bookingIds);
            return "redirect:/view/payments/success/" + firstPaymentId;
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/view/payments/pay-all?bookingIds=" + bookingIds;
        }
    }

    // ---- Payment Ticket Download page (one unified download page) ----

    @GetMapping("/view/payments/ticket")
    public String showPaymentTicketForm() {
        return "payment/ticket-download";
    }

    @GetMapping("/view/payments/success/{paymentId}")
    public String showSuccess(@PathVariable Integer paymentId, Model model) {
        PaymentResponseDTO payment = paymentService.getPaymentById(paymentId);
        model.addAttribute("payment", payment);

        // If flash is empty (direct URL access), reconstruct the group from
        // sibling payments (same customer + same paymentDate).
        if (!model.containsAttribute(ATTR_ALL_PAYMENT_IDS)) {
            List<PaymentResponseDTO> siblings = paymentService.getAllPayments().stream()
                    .filter(p -> java.util.Objects.equals(p.getCustomerId(), payment.getCustomerId())
                            && p.getPaymentDate() != null && payment.getPaymentDate() != null
                            && p.getPaymentDate().withNano(0).equals(payment.getPaymentDate().withNano(0))
                            && p.getPaymentStatus() == payment.getPaymentStatus())
                    .toList();
            List<Integer> paymentIds = siblings.stream().map(PaymentResponseDTO::getPaymentId).toList();
            BigDecimal total = siblings.stream()
                    .map(PaymentResponseDTO::getAmount)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute(ATTR_ALL_PAYMENT_IDS, paymentIds.isEmpty() ? List.of(paymentId) : paymentIds);
            model.addAttribute("totalAmount", total.signum() > 0 ? total : payment.getAmount());
            model.addAttribute("perSeatFare", payment.getAmount());
            model.addAttribute(ATTR_SEAT_COUNT, Math.max(1, siblings.size()));
        }
        return "payment/success";
    }
}
