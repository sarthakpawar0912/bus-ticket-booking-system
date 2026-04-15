package busticketbookingsystem.trip.service;

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
import com.busticketbookingsystem.trip.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TripService.
 * Covers create (with 6 FK lookups), getAllTrips, getById, update (partial), delete, searchTrips.
 */
@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock private TripRepository tripRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private BusRepository busRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private DriverRepository driverRepository;

    @InjectMocks
    private TripService tripService;

    private Trip trip;
    private Route route;
    private Bus bus;
    private Address boardingAddr;
    private Address droppingAddr;
    private Driver driver1;
    private Driver driver2;

    @BeforeEach
    void setUp() {
        route = Route.builder().routeId(1).fromCity("Mumbai").toCity("Pune")
                .breakPoints(2).duration(180).build();

        bus = Bus.builder().busId(1).registrationNumber("MH12AB1234").capacity(40).type("AC").build();

        boardingAddr = Address.builder().addressId(1).address("Boarding Point")
                .city("Mumbai").state("MH").zipCode("400001").build();

        droppingAddr = Address.builder().addressId(2).address("Dropping Point")
                .city("Pune").state("MH").zipCode("411001").build();

        driver1 = Driver.builder().driverId(1).name("Driver One")
                .licenseNumber("DL001").phone("1111111111").build();

        driver2 = Driver.builder().driverId(2).name("Driver Two")
                .licenseNumber("DL002").phone("2222222222").build();

        trip = Trip.builder()
                .tripId(1).route(route).bus(bus)
                .boardingAddress(boardingAddr).droppingAddress(droppingAddr)
                .departureTime(LocalDateTime.of(2026, 5, 1, 8, 0))
                .arrivalTime(LocalDateTime.of(2026, 5, 1, 11, 0))
                .driver1(driver1).driver2(driver2)
                .availableSeats(40).fare(new BigDecimal("500.00"))
                .tripDate(LocalDateTime.of(2026, 5, 1, 0, 0)).build();
    }

    @Nested
    @DisplayName("create() Tests")
    class CreateTests {

        @Test
        @DisplayName("POSITIVE: Should create trip successfully with all valid FK references")
        void create_Success() {
            when(routeRepository.findById(1)).thenReturn(Optional.of(route));
            when(busRepository.findById(1)).thenReturn(Optional.of(bus));
            when(addressRepository.findById(1)).thenReturn(Optional.of(boardingAddr));
            when(addressRepository.findById(2)).thenReturn(Optional.of(droppingAddr));
            when(driverRepository.findById(1)).thenReturn(Optional.of(driver1));
            when(driverRepository.findById(2)).thenReturn(Optional.of(driver2));
            when(tripRepository.save(any(Trip.class))).thenReturn(trip);

            Trip result = tripService.create(trip, 1, 1, 1, 2, 1, 2);

            assertNotNull(result);
            assertEquals(1, result.getTripId());
            assertEquals(route, result.getRoute());
            assertEquals(bus, result.getBus());
            assertEquals(boardingAddr, result.getBoardingAddress());
            assertEquals(droppingAddr, result.getDroppingAddress());
            assertEquals(driver1, result.getDriver1());
            assertEquals(driver2, result.getDriver2());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when route not found")
        void create_RouteNotFound() {
            when(routeRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> tripService.create(trip, 999, 1, 1, 2, 1, 2));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when bus not found")
        void create_BusNotFound() {
            when(routeRepository.findById(1)).thenReturn(Optional.of(route));
            when(busRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> tripService.create(trip, 1, 999, 1, 2, 1, 2));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when boarding address not found")
        void create_BoardingAddressNotFound() {
            when(routeRepository.findById(1)).thenReturn(Optional.of(route));
            when(busRepository.findById(1)).thenReturn(Optional.of(bus));
            when(addressRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> tripService.create(trip, 1, 1, 999, 2, 1, 2));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when dropping address not found")
        void create_DroppingAddressNotFound() {
            when(routeRepository.findById(1)).thenReturn(Optional.of(route));
            when(busRepository.findById(1)).thenReturn(Optional.of(bus));
            when(addressRepository.findById(1)).thenReturn(Optional.of(boardingAddr));
            when(addressRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> tripService.create(trip, 1, 1, 1, 999, 1, 2));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when driver1 not found")
        void create_Driver1NotFound() {
            when(routeRepository.findById(1)).thenReturn(Optional.of(route));
            when(busRepository.findById(1)).thenReturn(Optional.of(bus));
            when(addressRepository.findById(1)).thenReturn(Optional.of(boardingAddr));
            when(addressRepository.findById(2)).thenReturn(Optional.of(droppingAddr));
            when(driverRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> tripService.create(trip, 1, 1, 1, 2, 999, 2));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when driver2 not found")
        void create_Driver2NotFound() {
            when(routeRepository.findById(1)).thenReturn(Optional.of(route));
            when(busRepository.findById(1)).thenReturn(Optional.of(bus));
            when(addressRepository.findById(1)).thenReturn(Optional.of(boardingAddr));
            when(addressRepository.findById(2)).thenReturn(Optional.of(droppingAddr));
            when(driverRepository.findById(1)).thenReturn(Optional.of(driver1));
            when(driverRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> tripService.create(trip, 1, 1, 1, 2, 1, 999));
        }
    }

    @Nested
    @DisplayName("getAllTrips() Tests")
    class GetAllTripsTests {

        @Test
        @DisplayName("POSITIVE: Should return all trips with details")
        void getAllTrips_ReturnsList() {
            when(tripRepository.findAllWithDetails()).thenReturn(List.of(trip));

            List<Trip> result = tripService.getAllTrips();

            assertEquals(1, result.size());
            assertEquals("Mumbai", result.get(0).getRoute().getFromCity());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no trips exist")
        void getAllTrips_Empty() {
            when(tripRepository.findAllWithDetails()).thenReturn(Collections.emptyList());

            assertTrue(tripService.getAllTrips().isEmpty());
        }
    }

    @Nested
    @DisplayName("getById() Tests")
    class GetByIdTests {

        @Test
        @DisplayName("POSITIVE: Should return trip for valid ID")
        void getById_Success() {
            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));

            Trip result = tripService.getById(1);

            assertEquals(1, result.getTripId());
            assertEquals(new BigDecimal("500.00"), result.getFare());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw for invalid ID")
        void getById_NotFound() {
            when(tripRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> tripService.getById(999));
        }
    }

    @Nested
    @DisplayName("update() Tests")
    class UpdateTests {

        @Test
        @DisplayName("POSITIVE: Should update all non-null fields")
        void update_AllFields_Success() {
            Trip updatedTrip = Trip.builder()
                    .departureTime(LocalDateTime.of(2026, 6, 1, 9, 0))
                    .arrivalTime(LocalDateTime.of(2026, 6, 1, 12, 0))
                    .availableSeats(35)
                    .fare(new BigDecimal("600.00"))
                    .tripDate(LocalDateTime.of(2026, 6, 1, 0, 0)).build();

            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(tripRepository.save(any(Trip.class))).thenReturn(trip);

            Trip result = tripService.update(1, updatedTrip);

            assertNotNull(result);
            assertEquals(LocalDateTime.of(2026, 6, 1, 9, 0), trip.getDepartureTime());
            assertEquals(35, trip.getAvailableSeats());
            assertEquals(new BigDecimal("600.00"), trip.getFare());
        }

        @Test
        @DisplayName("EDGE CASE: Should only update non-null fields (partial update)")
        void update_PartialFields() {
            // Only update fare, leave everything else null
            Trip partialUpdate = Trip.builder().fare(new BigDecimal("750.00")).build();

            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(tripRepository.save(any(Trip.class))).thenReturn(trip);

            tripService.update(1, partialUpdate);

            // Fare updated
            assertEquals(new BigDecimal("750.00"), trip.getFare());
            // Other fields unchanged
            assertEquals(LocalDateTime.of(2026, 5, 1, 8, 0), trip.getDepartureTime());
            assertEquals(40, trip.getAvailableSeats());
        }

        @Test
        @DisplayName("EDGE CASE: Should handle update with all null fields (no changes)")
        void update_AllNullFields() {
            Trip emptyUpdate = new Trip(); // all fields null

            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
            when(tripRepository.save(any(Trip.class))).thenReturn(trip);

            Trip result = tripService.update(1, emptyUpdate);

            assertNotNull(result);
            // Nothing should change
            assertEquals(new BigDecimal("500.00"), trip.getFare());
            assertEquals(40, trip.getAvailableSeats());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when trip not found for update")
        void update_NotFound() {
            when(tripRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> tripService.update(999, new Trip()));
        }
    }

    @Nested
    @DisplayName("delete() Tests")
    class DeleteTests {

        @Test
        @DisplayName("POSITIVE: Should delete trip when it exists")
        void delete_Success() {
            when(tripRepository.findById(1)).thenReturn(Optional.of(trip));

            tripService.delete(1);

            verify(tripRepository).delete(trip);
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when trip not found for deletion")
        void delete_NotFound() {
            when(tripRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> tripService.delete(999));
        }
    }

    @Nested
    @DisplayName("searchTrips() Tests")
    class SearchTripsTests {

        @Test
        @DisplayName("POSITIVE: Should return matching trips for from/to cities")
        void searchTrips_Found() {
            when(tripRepository.searchTrips("Mumbai", "Pune")).thenReturn(List.of(trip));

            List<Trip> result = tripService.searchTrips("Mumbai", "Pune");

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no matching trips")
        void searchTrips_NoMatch() {
            when(tripRepository.searchTrips("Mars", "Jupiter")).thenReturn(Collections.emptyList());

            assertTrue(tripService.searchTrips("Mars", "Jupiter").isEmpty());
        }
    }
}
