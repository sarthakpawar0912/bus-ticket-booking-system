package com.busticketbookingsystem.review.controller;

import com.busticketbookingsystem.customer.service.CustomerService;
import com.busticketbookingsystem.review.dto.ReviewDTO;
import com.busticketbookingsystem.review.entity.Review;
import com.busticketbookingsystem.review.service.ReviewService;
import com.busticketbookingsystem.trip.dto.TripDTO;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReviewController {

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

    @PostMapping("/api/reviews")
    @ResponseBody
    public Map<String, Object> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        Review r = reviewService.createReview(reviewDTO);
        return mapReviewToMap(r);
    }

    @GetMapping("/api/reviews")
    @ResponseBody
    public List<Map<String, Object>> getAllReviews() {
        return reviewService.getAllReviews().stream()
                .map(this::mapReviewToMap)
                .toList();
    }

    @GetMapping("/api/reviews/trip/{tripId}")
    @ResponseBody
    public List<Map<String, Object>> getReviewsByTrip(@PathVariable Integer tripId) {
        return reviewService.getReviewsByTrip(tripId).stream()
                .map(this::mapReviewToMap)
                .toList();
    }

    @GetMapping("/api/reviews/customer/{customerId}")
    @ResponseBody
    public List<Map<String, Object>> getReviewsByCustomer(@PathVariable Integer customerId) {
        return reviewService.getReviewsByCustomer(customerId).stream()
                .map(this::mapReviewToMap)
                .toList();
    }

    @DeleteMapping("/api/reviews/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable Integer id) {
        reviewService.deleteReview(id);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Review deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view/reviews")
    public String listReviews(Model model) {
        List<Map<String, Object>> reviews = reviewService.getAllReviews().stream()
                .map(this::mapReviewToMap)
                .toList();
        model.addAttribute("reviews", reviews);
        return "review/reviews";
    }

    @GetMapping("/view/reviews/add")
    public String showAddReviewForm(Model model) {
        model.addAttribute("review", new ReviewDTO());
        model.addAttribute("customers", customerService.getAll());
        List<TripDTO> trips = tripService.getAllTrips().stream()
                .map(this::mapTripToDTO)
                .toList();
        model.addAttribute("trips", trips);
        return "review/add-review";
    }

    @PostMapping("/view/reviews/save")
    public String saveReview(@ModelAttribute("review") ReviewDTO reviewDTO, RedirectAttributes redirectAttributes) {
        reviewService.createReview(reviewDTO);
        redirectAttributes.addFlashAttribute("success", "Review created successfully");
        return "redirect:/view/reviews";
    }

    @GetMapping("/view/reviews/delete/{id}")
    public String deleteReviewView(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        reviewService.deleteReview(id);
        redirectAttributes.addFlashAttribute("success", "Review deleted successfully");
        return "redirect:/view/reviews";
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
                .fromCity(t.getRoute().getFromCity())
                .toCity(t.getRoute().getToCity())
                .departureTime(t.getDepartureTime())
                .build();
    }
}
