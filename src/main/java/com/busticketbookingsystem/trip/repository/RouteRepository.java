package com.busticketbookingsystem.trip.repository;

import com.busticketbookingsystem.trip.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
