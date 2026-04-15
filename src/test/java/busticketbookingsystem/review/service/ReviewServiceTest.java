package com.busticketbookingsystem.review.service;

import com.busticketbookingsystem.customer.entity.Customer;
import com.busticketbookingsystem.customer.repository.CustomerRepository;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.review.dto.ReviewDTO;
import com.busticketbookingsystem.review.entity.Review;
import com.busticketbookingsystem.review.repository.ReviewRepository;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Customer customer;
    private Trip trip;
    private Review review;
    private ReviewDTO reviewDTO;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .customerId(1)
                .name("Test Customer")
                .email("test@email.com")
                .phone("9876543210")
                .build();

        trip = Trip.builder()
                .tripId(1)
                .availableSeats(30)
                .build();

        review = Review.builder()
                .reviewId(1)
                .customer(customer)
                .trip(trip)
                .rating(5)
                .comment("Excellent service!")
                .reviewDate(LocalDateTime.now())
                .build();

        reviewDTO = ReviewDTO.builder()
                .customerId(1)
                .tripId(1)
                .rating(5)
                .comment("Excellent service!")
                .build();
    }

    // ================= CREATE REVIEW =================
    @Nested
    @DisplayName("createReview() Tests")
    class CreateReviewTests {

        @Test
        void createReview_Success() {

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(reviewRepository.findAll()).thenReturn(Collections.emptyList());

            when(reviewRepository.save(any(Review.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Review result = reviewService.createReview(reviewDTO);

            assertNotNull(result);
            assertEquals(5, result.getRating());
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        void createReview_AutoIncrement() {

            Review r1 = Review.builder().reviewId(1).build();
            Review r2 = Review.builder().reviewId(2).build();

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(reviewRepository.findAll()).thenReturn(List.of(r1, r2));

            when(reviewRepository.save(any(Review.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Review result = reviewService.createReview(reviewDTO);

            assertEquals(3, result.getReviewId());
        }

        @Test
        void createReview_CustomerNotFound() {

            when(customerRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> reviewService.createReview(reviewDTO));
        }

        @Test
        void createReview_TripNotFound() {

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(tripRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> reviewService.createReview(reviewDTO));
        }
    }

    // ================= GET BY TRIP =================
    @Test
    void getReviewsByTrip() {

        when(reviewRepository.findByTrip_TripId(1)).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByTrip(1);

        assertEquals(1, result.size());
    }

    @Test
    void getReviewsByTrip_Empty() {

        when(reviewRepository.findByTrip_TripId(99)).thenReturn(Collections.emptyList());

        assertTrue(reviewService.getReviewsByTrip(99).isEmpty());
    }

    // ================= GET BY CUSTOMER =================
    @Test
    void getReviewsByCustomer() {

        when(reviewRepository.findByCustomer_CustomerId(1)).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByCustomer(1);

        assertEquals(1, result.size());
    }

    @Test
    void getReviewsByCustomer_Empty() {

        when(reviewRepository.findByCustomer_CustomerId(99)).thenReturn(Collections.emptyList());

        assertTrue(reviewService.getReviewsByCustomer(99).isEmpty());
    }

    // ================= GET ALL =================
    @Test
    void getAllReviews() {

        when(reviewRepository.findAll()).thenReturn(List.of(review));

        List<Review> result = reviewService.getAllReviews();

        assertEquals(1, result.size());
    }

    @Test
    void getAllReviews_Empty() {

        when(reviewRepository.findAll()).thenReturn(Collections.emptyList());

        assertTrue(reviewService.getAllReviews().isEmpty());
    }

    // ================= DELETE =================
    @Test
    void deleteReview_Success() {

        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        reviewService.deleteReview(1);

        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_NotFound() {

        when(reviewRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.deleteReview(999));
    }
}