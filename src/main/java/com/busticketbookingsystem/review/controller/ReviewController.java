package com.busticketbookingsystem.review.controller;

import com.busticketbookingsystem.customer.service.CustomerService;
import com.busticketbookingsystem.review.dto.ReviewDTO;
import com.busticketbookingsystem.review.entity.Review;
import com.busticketbookingsystem.review.service.ReviewService;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.dto.TripDTO;
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
import java.util.stream.Collectors;

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
    public Review createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        return reviewService.createReview(reviewDTO);
    }

    @GetMapping("/api/reviews")
    @ResponseBody
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/api/reviews/trip/{tripId}")
    @ResponseBody
    public List<Review> getReviewsByTrip(@PathVariable Integer tripId) {
        return reviewService.getReviewsByTrip(tripId);
    }

    @GetMapping("/api/reviews/customer/{customerId}")
    @ResponseBody
    public List<Review> getReviewsByCustomer(@PathVariable Integer customerId) {
        return reviewService.getReviewsByCustomer(customerId);
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
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "review/reviews";
    }

    @GetMapping("/view/reviews/add")
    public String showAddReviewForm(Model model) {
        model.addAttribute("review", new ReviewDTO());
        model.addAttribute("customers", customerService.getAll());
        List<TripDTO> trips = tripService.getAllTrips().stream().map(this::mapTripToDTO).collect(Collectors.toList());
        model.addAttribute("trips", trips);
        return "review/add-review";
    }

    private TripDTO mapTripToDTO(Trip t) {
        return TripDTO.builder()
                .tripId(t.getTripId())
                .fromCity(t.getRoute().getFromCity())
                .toCity(t.getRoute().getToCity())
                .departureTime(t.getDepartureTime())
                .build();
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
}
