package com.busticketbookingsystem.trip.repository;

import com.busticketbookingsystem.trip.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {

    List<Route> findByFromCityIgnoreCaseAndToCityIgnoreCase(String fromCity, String toCity);
}
