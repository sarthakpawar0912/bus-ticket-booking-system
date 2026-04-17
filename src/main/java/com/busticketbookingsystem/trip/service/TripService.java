package com.busticketbookingsystem.trip.service;

import com.busticketbookingsystem.agency.entity.Bus;
import com.busticketbookingsystem.agency.entity.Driver;
import com.busticketbookingsystem.agency.repository.BusRepository;
import com.busticketbookingsystem.agency.repository.DriverRepository;
import com.busticketbookingsystem.customer.entity.Address;
import com.busticketbookingsystem.customer.repository.AddressRepository;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.trip.entity.Route;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.repository.RouteRepository;
import com.busticketbookingsystem.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private static final String TRIP_NOT_FOUND = "Trip not found with id: ";

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final AddressRepository addressRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public Trip create(Trip trip, Integer routeId, Integer busId,
                       Integer boardingAddressId, Integer droppingAddressId,
                       Integer driver1Id, Integer driver2Id) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + busId));
        Address boardingAddress = addressRepository.findById(boardingAddressId)
                .orElseThrow(() -> new ResourceNotFoundException("Boarding Address not found"));
        Address droppingAddress = addressRepository.findById(droppingAddressId)
                .orElseThrow(() -> new ResourceNotFoundException("Dropping Address not found"));
        Driver driver1 = driverRepository.findById(driver1Id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver 1 not found"));
        Driver driver2 = driverRepository.findById(driver2Id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver 2 not found"));

        trip.setRoute(route);
        trip.setBus(bus);
        trip.setBoardingAddress(boardingAddress);
        trip.setDroppingAddress(droppingAddress);
        trip.setDriver1(driver1);
        trip.setDriver2(driver2);
        return tripRepository.save(trip);
    }

    @Transactional(readOnly = true)
    public List<Trip> getAllTrips() { return tripRepository.findAllWithDetails(); }

    @Transactional(readOnly = true)
    public Trip getById(Integer id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TRIP_NOT_FOUND + id));
    }

    @Transactional
    public Trip update(Integer id, Trip updatedTrip) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TRIP_NOT_FOUND + id));
        if (updatedTrip.getDepartureTime() != null) trip.setDepartureTime(updatedTrip.getDepartureTime());
        if (updatedTrip.getArrivalTime() != null) trip.setArrivalTime(updatedTrip.getArrivalTime());
        if (updatedTrip.getAvailableSeats() != null) trip.setAvailableSeats(updatedTrip.getAvailableSeats());
        if (updatedTrip.getFare() != null) trip.setFare(updatedTrip.getFare());
        if (updatedTrip.getTripDate() != null) trip.setTripDate(updatedTrip.getTripDate());
        return tripRepository.save(trip);
    }

    @Transactional(readOnly = true)
    public List<Trip> searchTrips(String fromCity, String toCity) {
        return tripRepository.searchTrips(fromCity, toCity);
    }
}
