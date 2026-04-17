package com.busticketbookingsystem.review.service;

import com.busticketbookingsystem.customer.entity.Customer;
import com.busticketbookingsystem.customer.repository.CustomerRepository;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.review.dto.ReviewDTO;
import com.busticketbookingsystem.review.entity.Review;
import com.busticketbookingsystem.review.repository.ReviewRepository;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final TripRepository tripRepository;

    @Transactional
    public Review createReview(ReviewDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));

        Trip trip = tripRepository.findById(dto.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + dto.getTripId()));

        // Generate next reviewId manually (reviews table has no AUTO_INCREMENT)
        Integer maxId = reviewRepository.findAll().stream()
                .map(Review::getReviewId)
                .max(Integer::compareTo)
                .orElse(0);

        Review review = Review.builder()
                .reviewId(maxId + 1)
                .customer(customer)
                .trip(trip)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .reviewDate(LocalDateTime.now())
                .build();

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByTrip(Integer tripId) {
        return reviewRepository.findByTripIdWithDetails(tripId);
    }

    public List<Review> getReviewsByCustomer(Integer customerId) {
        return reviewRepository.findByCustomerIdWithDetails(customerId);
    }

    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAllWithDetails();
    }

}
