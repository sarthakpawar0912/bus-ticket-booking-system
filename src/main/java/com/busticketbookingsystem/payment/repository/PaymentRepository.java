package com.busticketbookingsystem.payment.repository;

import com.busticketbookingsystem.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByBooking_BookingId(Integer bookingId);

    List<Payment> findByCustomer_CustomerId(Integer customerId);

    boolean existsByCustomer_CustomerIdAndBooking_Trip_TripId(Integer customerId, Integer tripId);

    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.booking LEFT JOIN FETCH p.customer")
    List<Payment> findAllWithDetails();

    @Query("SELECT p FROM Payment p " +
           "LEFT JOIN FETCH p.booking b " +
           "LEFT JOIN FETCH b.trip t " +
           "LEFT JOIN FETCH t.route " +
           "LEFT JOIN FETCH t.bus " +
           "LEFT JOIN FETCH t.boardingAddress " +
           "LEFT JOIN FETCH t.droppingAddress " +
           "LEFT JOIN FETCH p.customer " +
           "WHERE p.paymentId = :paymentId")
    Optional<Payment> findByIdWithAllDetails(@Param("paymentId") Integer paymentId);
}
