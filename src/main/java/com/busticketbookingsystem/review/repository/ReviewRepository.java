package com.busticketbookingsystem.review.repository;

import com.busticketbookingsystem.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.customer LEFT JOIN FETCH r.trip t LEFT JOIN FETCH t.route")
    List<Review> findAllWithDetails();

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.customer LEFT JOIN FETCH r.trip t LEFT JOIN FETCH t.route WHERE t.tripId = :tripId")
    List<Review> findByTripIdWithDetails(@Param("tripId") Integer tripId);

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.customer LEFT JOIN FETCH r.trip t LEFT JOIN FETCH t.route WHERE r.customer.customerId = :customerId")
    List<Review> findByCustomerIdWithDetails(@Param("customerId") Integer customerId);

    List<Review> findByTrip_TripId(Integer tripId);

    List<Review> findByCustomer_CustomerId(Integer customerId);
}
