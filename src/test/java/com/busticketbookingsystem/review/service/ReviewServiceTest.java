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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
                .name("Sarthak")
                .email("sarthak@test.com")
                .phone("9876543210")
                .build();

        trip = Trip.builder()
                .tripId(1)
                .departureTime(LocalDateTime.of(2026, 5, 1, 8, 0))
                .arrivalTime(LocalDateTime.of(2026, 5, 1, 11, 0))
                .availableSeats(40)
                .fare(new BigDecimal("500.00"))
                .tripDate(LocalDateTime.of(2026, 5, 1, 0, 0))
                .build();

        review = Review.builder()
                .reviewId(1)
                .customer(customer)
                .trip(trip)
                .rating(4)
                .comment("Good trip, comfortable bus.")
                .reviewDate(LocalDateTime.now())
                .build();

        reviewDTO = ReviewDTO.builder()
                .customerId(1)
                .tripId(1)
                .rating(4)
                .comment("Good trip, comfortable bus.")
                .build();
    }

    // ==================== createReview ====================

    @Test
    void createReview_success() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.createReview(reviewDTO);

        assertNotNull(result);
        assertEquals(4, result.getRating());
        assertEquals("Good trip, comfortable bus.", result.getComment());
        assertEquals(customer, result.getCustomer());
        assertEquals(trip, result.getTrip());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createReview_customerNotFound() {
        ReviewDTO dto = ReviewDTO.builder()
                .customerId(999)
                .tripId(1)
                .rating(5)
                .comment("Great!")
                .build();

        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.createReview(dto));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_tripNotFound() {
        ReviewDTO dto = ReviewDTO.builder()
                .customerId(1)
                .tripId(999)
                .rating(3)
                .comment("Average")
                .build();

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(tripRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.createReview(dto));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_withNullComment() {
        ReviewDTO dtoNoComment = ReviewDTO.builder()
                .customerId(1)
                .tripId(1)
                .rating(5)
                .comment(null)
                .build();

        Review reviewNoComment = Review.builder()
                .reviewId(2)
                .customer(customer)
                .trip(trip)
                .rating(5)
                .comment(null)
                .reviewDate(LocalDateTime.now())
                .build();

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewNoComment);

        Review result = reviewService.createReview(dtoNoComment);

        assertNotNull(result);
        assertNull(result.getComment());
        assertEquals(5, result.getRating());
    }

    // ==================== getReviewsByTrip ====================

    @Test
    void getReviewsByTrip_success() {
        when(reviewRepository.findByTrip_TripId(1)).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByTrip(1);

        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getRating());
    }

    @Test
    void getReviewsByTrip_noReviews() {
        when(reviewRepository.findByTrip_TripId(999)).thenReturn(Collections.emptyList());

        List<Review> result = reviewService.getReviewsByTrip(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void getReviewsByTrip_multipleReviews() {
        Review review2 = Review.builder()
                .reviewId(2)
                .customer(customer)
                .trip(trip)
                .rating(5)
                .comment("Excellent!")
                .reviewDate(LocalDateTime.now())
                .build();
        when(reviewRepository.findByTrip_TripId(1)).thenReturn(List.of(review, review2));

        List<Review> result = reviewService.getReviewsByTrip(1);

        assertEquals(2, result.size());
    }

    // ==================== getReviewsByCustomer ====================

    @Test
    void getReviewsByCustomer_success() {
        when(reviewRepository.findByCustomer_CustomerId(1)).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByCustomer(1);

        assertEquals(1, result.size());
    }

    @Test
    void getReviewsByCustomer_noReviews() {
        when(reviewRepository.findByCustomer_CustomerId(999)).thenReturn(Collections.emptyList());

        List<Review> result = reviewService.getReviewsByCustomer(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void getReviewsByCustomer_multipleReviews() {
        Trip trip2 = Trip.builder().tripId(2).fare(new BigDecimal("300.00")).availableSeats(30)
                .departureTime(LocalDateTime.now()).arrivalTime(LocalDateTime.now().plusHours(2))
                .tripDate(LocalDateTime.now()).build();
        Review review2 = Review.builder()
                .reviewId(3)
                .customer(customer)
                .trip(trip2)
                .rating(2)
                .comment("Not great")
                .reviewDate(LocalDateTime.now())
                .build();
        when(reviewRepository.findByCustomer_CustomerId(1)).thenReturn(List.of(review, review2));

        List<Review> result = reviewService.getReviewsByCustomer(1);

        assertEquals(2, result.size());
    }

    // ==================== getAllReviews ====================

    @Test
    void getAllReviews_success() {
        when(reviewRepository.findAll()).thenReturn(List.of(review));

        List<Review> result = reviewService.getAllReviews();

        assertEquals(1, result.size());
    }

    @Test
    void getAllReviews_emptyList() {
        when(reviewRepository.findAll()).thenReturn(Collections.emptyList());

        List<Review> result = reviewService.getAllReviews();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllReviews_multipleReviews() {
        Review review2 = Review.builder().reviewId(2).customer(customer).trip(trip).rating(1).comment("Bad").reviewDate(LocalDateTime.now()).build();
        when(reviewRepository.findAll()).thenReturn(List.of(review, review2));

        List<Review> result = reviewService.getAllReviews();

        assertEquals(2, result.size());
    }

    // ==================== deleteReview ====================

    @Test
    void deleteReview_success() {
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        assertDoesNotThrow(() -> reviewService.deleteReview(1));

        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void deleteReview_notFound() {
        when(reviewRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> reviewService.deleteReview(999));

        assertEquals("Review not found with id: 999", exception.getMessage());
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReview_verifyInteractions() {
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        reviewService.deleteReview(1);

        verify(reviewRepository).findById(1);
        verify(reviewRepository).delete(review);
        verifyNoMoreInteractions(reviewRepository);
    }
}
