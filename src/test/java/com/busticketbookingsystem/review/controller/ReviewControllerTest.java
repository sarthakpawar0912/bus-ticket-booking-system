package com.busticketbookingsystem.review.controller;

import com.busticketbookingsystem.customer.entity.Customer;
import com.busticketbookingsystem.customer.service.CustomerService;
import com.busticketbookingsystem.exception.BadRequestException;
import com.busticketbookingsystem.review.dto.ReviewDTO;
import com.busticketbookingsystem.review.entity.Review;
import com.busticketbookingsystem.review.service.ReviewService;
import com.busticketbookingsystem.trip.service.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ReviewService reviewService;
    @MockBean private CustomerService customerService;
    @MockBean private TripService tripService;
    @Autowired private ObjectMapper objectMapper;

    private ReviewDTO reviewDTO;
    private Review review;

    @BeforeEach
    void setUp() {
        reviewDTO = ReviewDTO.builder()
                .customerId(1).tripId(1).rating(5)
                .comment("Great!").build();

        Customer cust = Customer.builder().customerId(1).name("Sarthak").build();
        review = Review.builder()
                .reviewId(1).customer(cust).rating(5)
                .comment("Great!").reviewDate(LocalDateTime.now()).build();
    }

    @Test
    void createReview_success() throws Exception {
        when(reviewService.createReview(any())).thenReturn(review);
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.customerName").value("Sarthak"));
    }

    @Test
    void createReview_validationFails() throws Exception {
        ReviewDTO bad = ReviewDTO.builder().rating(10).build(); // rating > 5
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllReviews() throws Exception {
        when(reviewService.getAllReviews()).thenReturn(List.of(review));
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getReviewsByTrip() throws Exception {
        when(reviewService.getReviewsByTrip(1)).thenReturn(List.of(review));
        mockMvc.perform(get("/api/reviews/trip/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getReviewsByCustomer() throws Exception {
        when(reviewService.getReviewsByCustomer(1)).thenReturn(List.of(review));
        mockMvc.perform(get("/api/reviews/customer/1"))
                .andExpect(status().isOk());
    }



    @Test
    void listReviewsView() throws Exception {
        when(reviewService.getAllReviews()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/view/reviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("review/reviews"))
                .andExpect(model().attributeExists("reviews"));
    }

    @Test
    void showAddReviewForm() throws Exception {
        when(customerService.getAll()).thenReturn(Collections.emptyList());
        when(tripService.getAllTrips()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/view/reviews/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("review/add-review"))
                .andExpect(model().attributeExists("review", "customers", "trips"));
    }

    @Test
    void saveReviewView() throws Exception {
        when(reviewService.createReview(any())).thenReturn(review);
        mockMvc.perform(post("/view/reviews/save")
                        .param("customerId", "1").param("tripId", "1")
                        .param("rating", "5").param("comment", "Great"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/reviews"));
    }

    @Test
    void saveReviewView_errorRedirectsWithFlashAndPreservesReview() throws Exception {
        when(reviewService.createReview(any()))
                .thenThrow(new BadRequestException("Customer 1 has no booking on trip 1."));

        MvcResult result = mockMvc.perform(post("/view/reviews/save")
                        .param("customerId", "1").param("tripId", "1")
                        .param("rating", "5").param("comment", "Great"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/reviews/add"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attributeExists("review"))
                .andReturn();

        ReviewDTO flashed = (ReviewDTO) result.getFlashMap().get("review");
        org.junit.jupiter.api.Assertions.assertEquals(5, flashed.getRating());
        org.junit.jupiter.api.Assertions.assertEquals("Great", flashed.getComment());
        org.junit.jupiter.api.Assertions.assertTrue(
                ((String) result.getFlashMap().get("error")).contains("no booking"));
    }

    @Test
    void showAddReviewForm_usesFlashedReviewWhenPresent() throws Exception {
        ReviewDTO flashed = ReviewDTO.builder()
                .customerId(7).tripId(3).rating(4).comment("retry me").build();
        when(customerService.getAll()).thenReturn(Collections.emptyList());
        when(tripService.getAllTrips()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/view/reviews/add")
                        .flashAttr("review", flashed))
                .andExpect(status().isOk())
                .andExpect(view().name("review/add-review"))
                .andExpect(model().attribute("review",
                        org.hamcrest.Matchers.hasProperty("comment",
                                org.hamcrest.Matchers.equalTo("retry me"))))
                .andExpect(model().attribute("review",
                        org.hamcrest.Matchers.hasProperty("rating",
                                org.hamcrest.Matchers.equalTo(4))));
    }
}
