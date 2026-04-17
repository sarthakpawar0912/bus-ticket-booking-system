package com.busticketbookingsystem.trip.service;

import com.busticketbookingsystem.exception.BadRequestException;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.trip.entity.Route;
import com.busticketbookingsystem.trip.repository.RouteRepository;
import com.busticketbookingsystem.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RouteService.
 * Covers create, getAll, getById, update (partial), delete (with trip check), searchRoutes.
 */
@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private RouteService routeService;

    private Route route;

    @BeforeEach
    void setUp() {
        route = Route.builder()
                .routeId(1).fromCity("Mumbai").toCity("Pune")
                .breakPoints(2).duration(180).build();
    }

    @Nested
    @DisplayName("create() Tests")
    class CreateTests {

        @Test
        @DisplayName("POSITIVE: Should create route successfully")
        void create_Success() {
            when(routeRepository.save(any(Route.class))).thenReturn(route);

            Route result = routeService.create(route);

            assertNotNull(result);
            assertEquals("Mumbai", result.getFromCity());
            assertEquals("Pune", result.getToCity());
            assertEquals(2, result.getBreakPoints());
            assertEquals(180, result.getDuration());
        }
    }

    @Nested
    @DisplayName("getAll() Tests")
    class GetAllTests {

        @Test
        @DisplayName("POSITIVE: Should return all routes")
        void getAll_ReturnsList() {
            Route route2 = Route.builder().routeId(2).fromCity("Delhi").toCity("Jaipur")
                    .breakPoints(1).duration(300).build();

            when(routeRepository.findAll()).thenReturn(List.of(route, route2));

            List<Route> result = routeService.getAll();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no routes exist")
        void getAll_Empty() {
            when(routeRepository.findAll()).thenReturn(Collections.emptyList());

            assertTrue(routeService.getAll().isEmpty());
        }
    }

    @Nested
    @DisplayName("getById() Tests")
    class GetByIdTests {

        @Test
        @DisplayName("POSITIVE: Should return route for valid ID")
        void getById_Success() {
            when(routeRepository.findById(1)).thenReturn(Optional.of(route));

            Route result = routeService.getById(1);

            assertEquals("Mumbai", result.getFromCity());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for invalid ID")
        void getById_NotFound() {
            when(routeRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> routeService.getById(999));
        }
    }

    @Nested
    @DisplayName("update() Tests")
    class UpdateTests {

        @Test
        @DisplayName("POSITIVE: Should update all fields of existing route")
        void update_AllFields_Success() {
            Route updatedRoute = Route.builder()
                    .fromCity("Delhi").toCity("Agra")
                    .breakPoints(0).duration(240).build();

            when(routeRepository.findById(1)).thenReturn(Optional.of(route));
            when(routeRepository.save(any(Route.class))).thenReturn(route);

            Route result = routeService.update(1, updatedRoute);

            assertNotNull(result);
            assertEquals("Delhi", route.getFromCity());
            assertEquals("Agra", route.getToCity());
            assertEquals(0, route.getBreakPoints());
            assertEquals(240, route.getDuration());
        }

        @Test
        @DisplayName("EDGE CASE: Should only update non-null fields (partial update)")
        void update_PartialFields() {
            // Only update fromCity, leave other fields null
            Route partialUpdate = Route.builder().fromCity("Bangalore").build();

            when(routeRepository.findById(1)).thenReturn(Optional.of(route));
            when(routeRepository.save(any(Route.class))).thenReturn(route);

            routeService.update(1, partialUpdate);

            // fromCity updated, others unchanged
            assertEquals("Bangalore", route.getFromCity());
            assertEquals("Pune", route.getToCity()); // unchanged
            assertEquals(2, route.getBreakPoints()); // unchanged
            assertEquals(180, route.getDuration()); // unchanged
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when route not found")
        void update_NotFound() {
            when(routeRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> routeService.update(999, new Route()));
        }
    }

    @Nested
    @DisplayName("delete() Tests")
    class DeleteTests {

        @Test
        @DisplayName("POSITIVE: Should delete route when not used by any trips")
        void delete_Success() {
            when(routeRepository.findById(1)).thenReturn(Optional.of(route));
            when(tripRepository.existsByRoute_RouteId(1)).thenReturn(false);

            routeService.delete(1);

            verify(routeRepository).delete(route);
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException when route is used by trips")
        void delete_RouteInUse_Throws() {
            when(routeRepository.findById(1)).thenReturn(Optional.of(route));
            when(tripRepository.existsByRoute_RouteId(1)).thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> routeService.delete(1));

            assertEquals("Cannot delete route - it is used by existing trips.", ex.getMessage());
            verify(routeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for non-existent route")
        void delete_NotFound() {
            when(routeRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> routeService.delete(999));
        }
    }

    @Nested
    @DisplayName("searchRoutes() Tests")
    class SearchRoutesTests {

        @Test
        @DisplayName("POSITIVE: Should return matching routes for from/to cities")
        void searchRoutes_Found() {
            when(routeRepository.findByFromCityIgnoreCaseAndToCityIgnoreCase("Mumbai", "Pune"))
                    .thenReturn(List.of(route));

            List<Route> result = routeService.searchRoutes("Mumbai", "Pune");

            assertEquals(1, result.size());
            assertEquals("Mumbai", result.get(0).getFromCity());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no matching routes")
        void searchRoutes_NoMatch() {
            when(routeRepository.findByFromCityIgnoreCaseAndToCityIgnoreCase("Mars", "Jupiter"))
                    .thenReturn(Collections.emptyList());

            assertTrue(routeService.searchRoutes("Mars", "Jupiter").isEmpty());
        }
    }
}
