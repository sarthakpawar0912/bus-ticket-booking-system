package com.busticketbookingsystem.review.repository;

import com.busticketbookingsystem.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByTrip_TripId(Integer tripId);

    List<Review> findByCustomer_CustomerId(Integer customerId);
}
