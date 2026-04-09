package com.busticketbookingsystem.payment.repository;

import com.busticketbookingsystem.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Payment findByBooking_BookingId(Integer bookingId);
}