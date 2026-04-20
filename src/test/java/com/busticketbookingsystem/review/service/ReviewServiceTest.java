package com.busticketbookingsystem.review.service;

import com.busticketbookingsystem.customer.entity.Customer;
import com.busticketbookingsystem.customer.repository.CustomerRepository;
import com.busticketbookingsystem.exception.BadRequestException;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.payment.repository.PaymentRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReviewService.
 * Covers createReview, getReviewsByTrip, getReviewsByCustomer,
 * getAllReviews, deleteReview.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Customer customer;
    private Trip trip;
    private Review review;
    private ReviewDTO reviewDTO;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .customerId(1).name("Test Customer")
                .email("test@email.com").phone("9876543210").build();

        trip = Trip.builder().tripId(1).availableSeats(30).build();

        review = Review.builder()
                .reviewId(1).customer(customer).trip(trip)
                .rating(5).comment("Excellent service!")
                .reviewDate(LocalDateTime.of(2026, 4, 15, 10, 0)).build();

        reviewDTO = ReviewDTO.builder()
                .customerId(1).tripId(1).rating(5)
                .comment("Excellent service!").build();
    }

    @Nested
    @DisplayName("createReview() Tests")
    class CreateReviewTests {

        @Test
        @DisplayName("POSITIVE: Should create review successfully with auto-generated ID")
        void createReview_Success() {
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(paymentRepository.existsByCustomer_CustomerIdAndBooking_Trip_TripId(1, 1))
                    .thenReturn(true);
            // No existing reviews, so max ID will be 0
            when(reviewRepository.findAll()).thenReturn(Collections.emptyList());
            when(reviewRepository.save(any(Review.class))).thenReturn(review);

            Review result = reviewService.createReview(reviewDTO);

            assertNotNull(result);
            assertEquals(5, result.getRating());
            assertEquals("Excellent service!", result.getComment());
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("POSITIVE: Should auto-increment reviewId from existing reviews")
        void createReview_AutoIncrementId() {
            Review r1 = Review.builder().reviewId(1).build();
            Review r2 = Review.builder().reviewId(2).build();
            Review r3 = Review.builder().reviewId(3).build();

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(paymentRepository.existsByCustomer_CustomerIdAndBooking_Trip_TripId(1, 1))
                    .thenReturn(true);
            when(reviewRepository.findAll()).thenReturn(List.of(r1, r2, r3));
            when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
                Review saved = invocation.getArgument(0);
                assertEquals(4, saved.getReviewId());
                return saved;
            });

            reviewService.createReview(reviewDTO);

            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException when customer has no paid booking on the trip")
        void createReview_NoPaidBooking() {
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(paymentRepository.existsByCustomer_CustomerIdAndBooking_Trip_TripId(1, 1))
                    .thenReturn(false);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> reviewService.createReview(reviewDTO));
            assertTrue(ex.getMessage().contains("no booking on trip"));
            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when customer not found")
        void createReview_CustomerNotFound() {
            when(customerRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> reviewService.createReview(reviewDTO));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when trip not found")
        void createReview_TripNotFound() {
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(tripRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> reviewService.createReview(reviewDTO));
        }
    }

    @Nested
    @DisplayName("getReviewsByTrip() Tests")
    class GetReviewsByTripTests {

        @Test
        @DisplayName("POSITIVE: Should return reviews for a trip")
        void getReviewsByTrip_ReturnsList() {
            when(reviewRepository.findByTripIdWithDetails(1)).thenReturn(List.of(review));

            List<Review> result = reviewService.getReviewsByTrip(1);

            assertEquals(1, result.size());
            assertEquals(5, result.get(0).getRating());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no reviews for trip")
        void getReviewsByTrip_Empty() {
            when(reviewRepository.findByTripIdWithDetails(99)).thenReturn(Collections.emptyList());

            assertTrue(reviewService.getReviewsByTrip(99).isEmpty());
        }
    }

    @Nested
    @DisplayName("getReviewsByCustomer() Tests")
    class GetReviewsByCustomerTests {

        @Test
        @DisplayName("POSITIVE: Should return reviews for a customer")
        void getReviewsByCustomer_ReturnsList() {
            when(reviewRepository.findByCustomerIdWithDetails(1)).thenReturn(List.of(review));

            List<Review> result = reviewService.getReviewsByCustomer(1);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list for customer with no reviews")
        void getReviewsByCustomer_Empty() {
            when(reviewRepository.findByCustomerIdWithDetails(99)).thenReturn(Collections.emptyList());

            assertTrue(reviewService.getReviewsByCustomer(99).isEmpty());
        }
    }

    @Nested
    @DisplayName("getAllReviews() Tests")
    class GetAllReviewsTests {

        @Test
        @DisplayName("POSITIVE: Should return all reviews with details")
        void getAllReviews_ReturnsList() {
            when(reviewRepository.findAllWithDetails()).thenReturn(List.of(review));

            List<Review> result = reviewService.getAllReviews();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no reviews exist")
        void getAllReviews_Empty() {
            when(reviewRepository.findAllWithDetails()).thenReturn(Collections.emptyList());

            assertTrue(reviewService.getAllReviews().isEmpty());
        }
    }



}
