package com.busticketbookingsystem.trip.repository;

import com.busticketbookingsystem.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
}
