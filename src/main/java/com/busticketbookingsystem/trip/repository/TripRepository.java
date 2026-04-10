package com.busticketbookingsystem.trip.repository;

import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.entity.TripStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    @Override
    @EntityGraph(attributePaths = {"route", "bus", "driver"})
    List<Trip> findAll();

    @Override
    @EntityGraph(attributePaths = {"route", "bus", "driver"})
    Optional<Trip> findById(Long tripId);

    @EntityGraph(attributePaths = {"route", "bus", "driver"})
    @Query("""
            select t
            from Trip t
            where (:source is null or lower(t.route.source) = lower(:source))
              and (:destination is null or lower(t.route.destination) = lower(:destination))
              and (:travelDate is null or t.travelDate = :travelDate)
              and (:routeId is null or t.route.id = :routeId)
              and (:busType is null or lower(t.bus.type) = lower(:busType))
              and t.status = :status
              and t.availableSeats > 0
            order by t.travelDate asc, t.departureTime asc
            """)
    List<Trip> searchTrips(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("travelDate") LocalDate travelDate,
            @Param("routeId") Long routeId,
            @Param("busType") String busType,
            @Param("status") TripStatus status
    );

    boolean existsByBus_BusIdAndTravelDateAndDepartureTime(Integer busId, LocalDate travelDate, LocalTime departureTime);

    boolean existsByDriver_DriverIdAndTravelDateAndDepartureTime(Integer driverId, LocalDate travelDate, LocalTime departureTime);

    boolean existsByRoute_Id(Long routeId);

    @Query("""
            select count(t) > 0
            from Trip t
            where t.id <> :tripId
              and t.bus.busId = :busId
              and t.travelDate = :travelDate
              and t.departureTime = :departureTime
            """)
    boolean existsBusScheduleConflict(
            @Param("tripId") Long tripId,
            @Param("busId") Integer busId,
            @Param("travelDate") LocalDate travelDate,
            @Param("departureTime") LocalTime departureTime
    );

    @Query("""
            select count(t) > 0
            from Trip t
            where t.id <> :tripId
              and t.driver.driverId = :driverId
              and t.travelDate = :travelDate
              and t.departureTime = :departureTime
            """)
    boolean existsDriverScheduleConflict(
            @Param("tripId") Long tripId,
            @Param("driverId") Integer driverId,
            @Param("travelDate") LocalDate travelDate,
            @Param("departureTime") LocalTime departureTime
    );
}
