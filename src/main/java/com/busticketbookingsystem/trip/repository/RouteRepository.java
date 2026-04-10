package com.busticketbookingsystem.trip.repository;

import com.busticketbookingsystem.trip.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

    boolean existsBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);

    Optional<Route> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);
}
