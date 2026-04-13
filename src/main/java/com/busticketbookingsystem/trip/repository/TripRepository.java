package com.busticketbookingsystem.trip.repository;

import com.busticketbookingsystem.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {

    @Query("SELECT t FROM Trip t JOIN FETCH t.route r WHERE LOWER(r.fromCity) = LOWER(:from) AND LOWER(r.toCity) = LOWER(:to) AND t.availableSeats > 0")
    List<Trip> searchTrips(@Param("from") String from, @Param("to") String to);

    @Query("SELECT DISTINCT t FROM Trip t " +
           "LEFT JOIN FETCH t.route " +
           "LEFT JOIN FETCH t.bus " +
           "LEFT JOIN FETCH t.boardingAddress " +
           "LEFT JOIN FETCH t.droppingAddress " +
           "LEFT JOIN FETCH t.driver1 " +
           "LEFT JOIN FETCH t.driver2")
    List<Trip> findAllWithDetails();

    boolean existsByRoute_RouteId(Integer routeId);

    boolean existsByBus_BusId(Integer busId);

    List<Trip> findByBus_BusId(Integer busId);
}
