package com.busticketbookingsystem.booking.controller;

import com.busticketbookingsystem.booking.dto.BookingRequestDTO;
import com.busticketbookingsystem.booking.dto.BookingResponseDTO;
import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.entity.BookingStatus;
import com.busticketbookingsystem.booking.service.BookingService;
import com.busticketbookingsystem.booking.service.TicketPdfService;
import com.busticketbookingsystem.customer.service.CustomerService;
import com.busticketbookingsystem.trip.dto.TripDTO;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class BookingController {

    private static final String ATTR_BOOKING = "booking";

    private final BookingService bookingService;
    private final TicketPdfService ticketPdfService;
    private final TripService tripService;
    private final CustomerService customerService;

    public BookingController(BookingService bookingService, TicketPdfService ticketPdfService,
                             TripService tripService, CustomerService customerService) {
        this.bookingService = bookingService;
        this.ticketPdfService = ticketPdfService;
        this.tripService = tripService;
        this.customerService = customerService;
    }

    private TripDTO mapTripToDTO(Trip t) {
        return TripDTO.builder()
                .tripId(t.getTripId())
                .routeId(t.getRoute().getRouteId())
                .busId(t.getBus().getBusId())
                .departureTime(t.getDepartureTime())
                .arrivalTime(t.getArrivalTime())
                .availableSeats(t.getAvailableSeats())
                .fare(t.getFare())
                .tripDate(t.getTripDate())
                .fromCity(t.getRoute().getFromCity())
                .toCity(t.getRoute().getToCity())
                .busType(t.getBus().getType())
                .registrationNumber(t.getBus().getRegistrationNumber())
                .boardingAddressId(t.getBoardingAddress().getAddressId())
                .droppingAddressId(t.getDroppingAddress().getAddressId())
                .boardingAddress(t.getBoardingAddress().getAddress() + ", " + t.getBoardingAddress().getCity())
                .droppingAddress(t.getDroppingAddress().getAddress() + ", " + t.getDroppingAddress().getCity())
                .build();
    }

    // ======================== REST API ========================

    @PostMapping("/api/bookings")
    @ResponseBody
    public BookingResponseDTO initiateBooking(@Valid @RequestBody BookingRequestDTO dto) {
        return bookingService.initiateBooking(dto);
    }

    @GetMapping("/api/bookings/trip/{tripId}")
    @ResponseBody
    public List<Map<String, Object>> getBookingsForTrip(@PathVariable Integer tripId) {
        return bookingService.getBookingsForTrip(tripId).stream().map(b -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("bookingId", b.getBookingId());
            map.put("tripId", b.getTrip() != null ? b.getTrip().getTripId() : null);
            map.put("seatNumber", b.getSeatNumber());
            map.put("status", b.getStatus().name());
            return map;
        }).toList();
    }

    @GetMapping("/api/bookings/{id}")
    @ResponseBody
    public Map<String, Object> apiGetBookingById(@PathVariable Integer id) {
        Booking b = bookingService.getBookingById(id);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("bookingId", b.getBookingId());
        map.put("tripId", b.getTrip() != null ? b.getTrip().getTripId() : null);
        map.put("seatNumber", b.getSeatNumber());
        map.put("status", b.getStatus().name());
        return map;
    }

    @PostMapping("/api/bookings/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, String>> cancelBooking(@PathVariable Integer id) {
        String message = bookingService.cancelBooking(id);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/api/bookings/{id}/ticket")
    @ResponseBody
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable Integer id) {
        byte[] pdfBytes = ticketPdfService.generateTicketPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ticket-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/api/bookings/group-ticket")
    @ResponseBody
    public ResponseEntity<byte[]> downloadGroupBookingTicket(@RequestParam String bookingIds) {
        String[] ids = bookingIds.split(",");
        List<Integer> bidList = new ArrayList<>();
        for (String s : ids) bidList.add(Integer.parseInt(s.trim()));
        byte[] pdfBytes = ticketPdfService.generateGroupBookingTicket(bidList);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "group-booking-ticket.pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view/bookings")
    public String listAvailableTrips(Model model) {
        List<TripDTO> trips = tripService.getAllTrips().stream()
                .filter(trip -> trip.getAvailableSeats() != null && trip.getAvailableSeats() > 0)
                .map(this::mapTripToDTO)
                .toList();
        model.addAttribute("trips", trips);
        return "booking/trips";
    }

    @GetMapping("/view/bookings/trip/{tripId}")
    public String showSeatSelection(@PathVariable Integer tripId, Model model, RedirectAttributes ra) {
        try {
            Trip trip = tripService.getById(tripId);
            TripDTO tripDTO = mapTripToDTO(trip);
            int capacity = trip.getBus().getCapacity();

            List<Integer> allSeats = new ArrayList<>();
            for (int i = 1; i <= capacity; i++) allSeats.add(i);

            List<Integer> bookedSeats = bookingService.getBookingsForTrip(tripId).stream()
                    .filter(b -> b.getStatus() == BookingStatus.Booked)
                    .map(Booking::getSeatNumber)
                    .toList();

            model.addAttribute("trip", tripDTO);
            model.addAttribute("allSeats", allSeats);
            model.addAttribute("bookedSeats", bookedSeats);
            model.addAttribute("customers", customerService.getAll());
            return "booking/select-seat";
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/view/bookings";
        }
    }

    @PostMapping("/view/bookings/book")
    public String bookSeats(@RequestParam Integer tripId,
                            @RequestParam List<Integer> seatNumbers,
                            @RequestParam Integer customerId,
                            RedirectAttributes ra) {
        try {
            BookingRequestDTO request = BookingRequestDTO.builder()
                    .tripId(tripId).seatNumbers(seatNumbers).customerId(customerId).build();
            BookingResponseDTO response = bookingService.initiateBooking(request);
            ra.addFlashAttribute(ATTR_BOOKING, response);
            ra.addFlashAttribute("seatNumbers", seatNumbers);
            // Pass customer name
            var customer = customerService.getById(customerId);
            ra.addFlashAttribute("customerName", customer.getName());
            // Pass trip info
            Trip trip = tripService.getById(tripId);
            if (trip.getRoute() != null) {
                ra.addFlashAttribute("fromCity", trip.getRoute().getFromCity());
                ra.addFlashAttribute("toCity", trip.getRoute().getToCity());
            }
            if (trip.getTripDate() != null) {
                ra.addFlashAttribute("tripDate", trip.getTripDate().toLocalDate().toString());
            }
            return "redirect:/view/bookings/confirmation/" + response.getBookingIds().get(0);
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/view/bookings/trip/" + tripId;
        }
    }

    // ---- Cancel Booking (UI flow, delegates to REST cancel) ----

    @GetMapping("/view/bookings/cancel")
    public String showCancelForm(Model model) {
        return "booking/cancel-booking";
    }

    @PostMapping("/view/bookings/cancel")
    public String cancelBookingView(@RequestParam Integer bookingId, RedirectAttributes ra) {
        try {
            String msg = bookingService.cancelBooking(bookingId);
            ra.addFlashAttribute("message", msg);
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/view/bookings/cancel";
    }

    // ---- Single Ticket Download page (UI wrapper around /api/bookings/{id}/ticket) ----

    @GetMapping("/view/bookings/ticket")
    public String showTicketDownloadForm() {
        return "booking/ticket-download";
    }

    // ---- Group Ticket Download page (UI wrapper around /api/bookings/group-ticket) ----

    @GetMapping("/view/bookings/group-ticket")
    public String showGroupTicketDownloadForm() {
        return "booking/group-ticket-download";
    }

    @GetMapping("/view/bookings/confirmation/{bookingId}")
    public String showConfirmation(@PathVariable Integer bookingId, Model model) {
        if (!model.containsAttribute(ATTR_BOOKING)) {
            Booking b = bookingService.getBookingById(bookingId);
            BookingResponseDTO dto = BookingResponseDTO.builder()
                    .bookingIds(List.of(bookingId))
                    .message("Booking #" + bookingId + " confirmed")
                    .totalFare(b.getTrip() != null ? b.getTrip().getFare() : null)
                    .build();
            model.addAttribute(ATTR_BOOKING, dto);
            model.addAttribute("seatNumbers", List.of(b.getSeatNumber()));
            if (b.getTrip() != null && b.getTrip().getRoute() != null) {
                model.addAttribute("fromCity", b.getTrip().getRoute().getFromCity());
                model.addAttribute("toCity", b.getTrip().getRoute().getToCity());
            }
            if (b.getTrip() != null && b.getTrip().getTripDate() != null) {
                model.addAttribute("tripDate", b.getTrip().getTripDate().toLocalDate().toString());
            }
        }
        return "booking/confirmation";
    }
}
