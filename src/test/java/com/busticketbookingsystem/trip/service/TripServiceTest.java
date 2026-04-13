package com.busticketbookingsystem.trip.service;

import com.busticketbookingsystem.agency.entity.Agency;
import com.busticketbookingsystem.agency.entity.AgencyOffice;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private BusRepository busRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private TripService tripService;

    private Route route;
    private Bus bus;
    private Address boardingAddress;
    private Address droppingAddress;
    private Driver driver1;
    private Driver driver2;
    private Trip trip;

    @BeforeEach
    void setUp() {
        route = Route.builder().routeId(1).fromCity("Pune").toCity("Mumbai").breakPoints(2).duration(180).build();

        Agency agency = Agency.builder().agencyId(1).name("Express").contactPersonName("R").email("e@e.com").phone("9876543210").build();
        AgencyOffice office = AgencyOffice.builder().officeId(1).agency(agency).officeMail("o@o.com").officeContactPersonName("A").officeContactNumber("1234567890").build();

        bus = Bus.builder().busId(1).office(office).registrationNumber("MH12AB1234").capacity(40).type("AC Sleeper").build();

        boardingAddress = Address.builder().addressId(1).address("Pune Bus Stand").city("Pune").state("MH").zipCode("411001").build();
        droppingAddress = Address.builder().addressId(2).address("Mumbai Central").city("Mumbai").state("MH").zipCode("400001").build();

        driver1 = Driver.builder().driverId(1).licenseNumber("LIC001").name("Driver One").phone("1111111111").office(office).build();
        driver2 = Driver.builder().driverId(2).licenseNumber("LIC002").name("Driver Two").phone("2222222222").office(office).build();

        trip = Trip.builder()
                .tripId(1)
                .route(route)
                .bus(bus)
                .boardingAddress(boardingAddress)
                .droppingAddress(droppingAddress)
                .departureTime(LocalDateTime.of(2026, 5, 1, 8, 0))
                .arrivalTime(LocalDateTime.of(2026, 5, 1, 11, 0))
                .driver1(driver1)
                .driver2(driver2)
                .availableSeats(40)
                .fare(new BigDecimal("500.00"))
                .tripDate(LocalDateTime.of(2026, 5, 1, 0, 0))
                .build();
    }

    // ==================== create ====================

    @Test
    void create_success() {
        Trip inputTrip = Trip.builder()
                .departureTime(LocalDateTime.of(2026, 5, 1, 8, 0))
                .arrivalTime(LocalDateTime.of(2026, 5, 1, 11, 0))
                .availableSeats(40)
                .fare(new BigDecimal("500.00"))
                .tripDate(LocalDateTime.of(2026, 5, 1, 0, 0))
                .build();

        when(routeRepository.findById(1)).thenReturn(Optional.of(route));
        when(busRepository.findById(1)).thenReturn(Optional.of(bus));
        when(addressRepository.findById(1)).thenReturn(Optional.of(boardingAddress));
        when(addressRepository.findById(2)).thenReturn(Optional.of(droppingAddress));
        when(driverRepository.findById(1)).thenReturn(Optional.of(driver1));
        when(driverRepository.findById(2)).thenReturn(Optional.of(driver2));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        Trip result = tripService.create(inputTrip, 1, 1, 1, 2, 1, 2);

        assertNotNull(result);
        assertEquals(route, result.getRoute());
        assertEquals(bus, result.getBus());
        assertEquals(40, result.getAvailableSeats());
        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    void create_routeNotFound() {
        Trip inputTrip = Trip.builder().build();

        when(routeRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tripService.create(inputTrip, 999, 1, 1, 2, 1, 2));

        verify(tripRepository, never()).save(any());
    }

    @Test
    void create_busNotFound() {
        Trip inputTrip = Trip.builder().build();

        when(routeRepository.findById(1)).thenReturn(Optional.of(route));
        when(busRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tripService.create(inputTrip, 1, 999, 1, 2, 1, 2));

        verify(tripRepository, never()).save(any());
    }

    @Test
    void create_driver1NotFound() {
        Trip inputTrip = Trip.builder().build();

        when(routeRepository.findById(1)).thenReturn(Optional.of(route));
        when(busRepository.findById(1)).thenReturn(Optional.of(bus));
        when(addressRepository.findById(1)).thenReturn(Optional.of(boardingAddress));
        when(addressRepository.findById(2)).thenReturn(Optional.of(droppingAddress));
        when(driverRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tripService.create(inputTrip, 1, 1, 1, 2, 999, 2));
    }

    // ==================== getAllTrips ====================

    @Test
    void getAllTrips_success() {
        when(tripRepository.findAll()).thenReturn(List.of(trip));

        List<Trip> result = tripService.getAllTrips();

        assertEquals(1, result.size());
    }

    @Test
    void getAllTrips_emptyList() {
        when(tripRepository.findAll()).thenReturn(Collections.emptyList());

        List<Trip> result = tripService.getAllTrips();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllTrips_multipleTrips() {
        Trip trip2 = Trip.builder().tripId(2).route(route).bus(bus).boardingAddress(boardingAddress)
                .droppingAddress(droppingAddress).departureTime(LocalDateTime.now()).arrivalTime(LocalDateTime.now().plusHours(3))
                .driver1(driver1).driver2(driver2).availableSeats(30).fare(new BigDecimal("600.00"))
                .tripDate(LocalDateTime.now()).build();
        when(tripRepository.findAll()).thenReturn(List.of(trip, trip2));

        List<Trip> result = tripService.getAllTrips();

        assertEquals(2, result.size());
    }

    // ==================== getById ====================

    @Test
    void getById_success() {
        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));

        Trip result = tripService.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getTripId());
        assertEquals(new BigDecimal("500.00"), result.getFare());
    }

    @Test
    void getById_notFound() {
        when(tripRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> tripService.getById(999));

        assertEquals("Trip not found with id: 999", exception.getMessage());
    }

    @Test
    void getById_withNullId() {
        when(tripRepository.findById(null)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tripService.getById(null));
    }

    // ==================== update ====================

    @Test
    void update_success() {
        Trip updatedTrip = Trip.builder()
                .departureTime(LocalDateTime.of(2026, 6, 1, 9, 0))
                .arrivalTime(LocalDateTime.of(2026, 6, 1, 12, 0))
                .availableSeats(35)
                .fare(new BigDecimal("550.00"))
                .tripDate(LocalDateTime.of(2026, 6, 1, 0, 0))
                .build();

        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        Trip result = tripService.update(1, updatedTrip);

        assertNotNull(result);
        assertEquals(35, trip.getAvailableSeats());
        assertEquals(new BigDecimal("550.00"), trip.getFare());
    }

    @Test
    void update_notFound() {
        Trip updatedTrip = Trip.builder().fare(new BigDecimal("600.00")).build();

        when(tripRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tripService.update(999, updatedTrip));

        verify(tripRepository, never()).save(any());
    }

    @Test
    void update_partialFields() {
        Trip partialUpdate = Trip.builder()
                .fare(new BigDecimal("700.00"))
                .build();

        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        Trip result = tripService.update(1, partialUpdate);

        assertNotNull(result);
        assertEquals(new BigDecimal("700.00"), trip.getFare());
        assertEquals(40, trip.getAvailableSeats()); // unchanged
    }

    // ==================== delete ====================

    @Test
    void delete_success() {
        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));

        assertDoesNotThrow(() -> tripService.delete(1));

        verify(tripRepository, times(1)).delete(trip);
    }

    @Test
    void delete_notFound() {
        when(tripRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tripService.delete(999));

        verify(tripRepository, never()).delete(any());
    }

    @Test
    void delete_verifyInteractions() {
        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));

        tripService.delete(1);

        verify(tripRepository).findById(1);
        verify(tripRepository).delete(trip);
        verifyNoMoreInteractions(tripRepository);
    }

    // ==================== searchTrips ====================

    @Test
    void searchTrips_success() {
        when(tripRepository.searchTrips("Pune", "Mumbai")).thenReturn(List.of(trip));

        List<Trip> result = tripService.searchTrips("Pune", "Mumbai");

        assertEquals(1, result.size());
    }

    @Test
    void searchTrips_noResults() {
        when(tripRepository.searchTrips("X", "Y")).thenReturn(Collections.emptyList());

        List<Trip> result = tripService.searchTrips("X", "Y");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchTrips_withNullParams() {
        when(tripRepository.searchTrips(null, null)).thenReturn(Collections.emptyList());

        List<Trip> result = tripService.searchTrips(null, null);

        assertTrue(result.isEmpty());
    }
}
