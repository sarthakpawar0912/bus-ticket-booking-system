package com.busticketbookingsystem.review.controller;

import com.busticketbookingsystem.customer.service.CustomerService;
import com.busticketbookingsystem.review.dto.ReviewDTO;
import com.busticketbookingsystem.review.entity.Review;
import com.busticketbookingsystem.review.service.ReviewService;
import com.busticketbookingsystem.trip.dto.TripDTO;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.service.TripService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class ReviewController {

    private static final String REVIEW_BASE_PATH = "/review";
    private static final String ATTR_REVIEW = "review";

    private final ReviewService reviewService;
    private final CustomerService customerService;
    private final TripService tripService;

    public ReviewController(ReviewService reviewService, CustomerService customerService,
                            TripService tripService) {
        this.reviewService = reviewService;
        this.customerService = customerService;
        this.tripService = tripService;
    }

    // ======================== REST API ========================

    @PostMapping("/api" + REVIEW_BASE_PATH + "s")
    @ResponseBody
    public Map<String, Object> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        Review r = reviewService.createReview(reviewDTO);
        return mapReviewToMap(r);
    }

    @GetMapping("/api" + REVIEW_BASE_PATH + "s")
    @ResponseBody
    public List<Map<String, Object>> getAllReviews() {
        return reviewService.getAllReviews().stream()
                .map(this::mapReviewToMap)
                .toList();
    }

    @GetMapping("/api" + REVIEW_BASE_PATH + "s/trip/{tripId}")
    @ResponseBody
    public List<Map<String, Object>> getReviewsByTrip(@PathVariable Integer tripId) {
        return reviewService.getReviewsByTrip(tripId).stream()
                .map(this::mapReviewToMap)
                .toList();
    }

    @GetMapping("/api" + REVIEW_BASE_PATH + "s/customer/{customerId}")
    @ResponseBody
    public List<Map<String, Object>> getReviewsByCustomer(@PathVariable Integer customerId) {
        return reviewService.getReviewsByCustomer(customerId).stream()
                .map(this::mapReviewToMap)
                .toList();
    }

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view" + REVIEW_BASE_PATH + "s")
    public String listReviews(Model model) {
        List<Map<String, Object>> reviews = reviewService.getAllReviews().stream()
                .map(this::mapReviewToMap)
                .toList();
        model.addAttribute("reviews", reviews);
        return "review/reviews";
    }

    @GetMapping("/view" + REVIEW_BASE_PATH + "s/add")
    public String showAddReviewForm(Model model) {
        if (!model.containsAttribute(ATTR_REVIEW)) {
            model.addAttribute(ATTR_REVIEW, new ReviewDTO());
        }
        model.addAttribute("customers", customerService.getAll());

        List<TripDTO> trips = tripService.getAllTrips().stream()
                .map(this::mapTripToDTO)
                .toList();

        model.addAttribute("trips", trips);
        return "review/add-review";
    }

    @PostMapping("/view" + REVIEW_BASE_PATH + "s/save")
    public String saveReview(@ModelAttribute(ATTR_REVIEW) ReviewDTO reviewDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            reviewService.createReview(reviewDTO);
            redirectAttributes.addFlashAttribute("success", "Review created successfully");
            return "redirect:/view/reviews";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute(ATTR_REVIEW, reviewDTO);
            return "redirect:/view/reviews/add";
        }
    }

    // ======================== HELPERS ========================

    private Map<String, Object> mapReviewToMap(Review r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("reviewId", r.getReviewId());
        map.put("customerId", r.getCustomer() != null ? r.getCustomer().getCustomerId() : null);
        map.put("customerName", r.getCustomer() != null ? r.getCustomer().getName() : "N/A");
        map.put("tripId", r.getTrip() != null ? r.getTrip().getTripId() : null);
        map.put("tripInfo", r.getTrip() != null && r.getTrip().getRoute() != null
                ? r.getTrip().getRoute().getFromCity() + " -> " + r.getTrip().getRoute().getToCity() : "N/A");
        map.put("rating", r.getRating());
        map.put("comment", r.getComment());
        map.put("reviewDate", r.getReviewDate());
        return map;
    }

    private TripDTO mapTripToDTO(Trip t) {
        return TripDTO.builder()
                .tripId(t.getTripId())
                .fromCity(t.getRoute() != null ? t.getRoute().getFromCity() : null)
                .toCity(t.getRoute() != null ? t.getRoute().getToCity() : null)
                .departureTime(t.getDepartureTime())
                .build();
    }
}