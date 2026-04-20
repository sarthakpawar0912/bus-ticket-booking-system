package com.busticketbookingsystem.trip.repository;

import com.busticketbookingsystem.trip.entity.Trip;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {

    @Query("SELECT t FROM Trip t JOIN FETCH t.route r WHERE LOWER(r.fromCity) = LOWER(:from) AND LOWER(r.toCity) = LOWER(:to) AND t.availableSeats > 0 AND t.tripDate >= CURRENT_TIMESTAMP ORDER BY t.tripDate ASC")
    List<Trip> searchTrips(@Param("from") String from, @Param("to") String to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trip t WHERE t.tripId = :tripId")
    Optional<Trip> findByIdForUpdate(@Param("tripId") Integer tripId);

    @Query("SELECT DISTINCT t FROM Trip t " +
           "LEFT JOIN FETCH t.route " +
           "LEFT JOIN FETCH t.bus " +
           "LEFT JOIN FETCH t.boardingAddress " +
           "LEFT JOIN FETCH t.droppingAddress " +
           "LEFT JOIN FETCH t.driver1 " +
           "LEFT JOIN FETCH t.driver2")
    List<Trip> findAllWithDetails();

    @Query(value = "SELECT t FROM Trip t " +
           "LEFT JOIN FETCH t.route " +
           "LEFT JOIN FETCH t.bus",
           countQuery = "SELECT COUNT(t) FROM Trip t")
    org.springframework.data.domain.Page<Trip> findAllPaged(org.springframework.data.domain.Pageable pageable);

    boolean existsByRoute_RouteId(Integer routeId);

    boolean existsByBus_BusId(Integer busId);

    List<Trip> findByBus_BusId(Integer busId);
}
