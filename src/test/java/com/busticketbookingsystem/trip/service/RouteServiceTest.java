package com.busticketbookingsystem.trip.service;

import com.busticketbookingsystem.exception.BadRequestException;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.trip.entity.Route;
import com.busticketbookingsystem.trip.repository.RouteRepository;
import com.busticketbookingsystem.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
                .routeId(1)
                .fromCity("Pune")
                .toCity("Mumbai")
                .breakPoints(2)
                .duration(180)
                .build();
    }

    // ==================== create ====================

    @Test
    void create_success() {
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        Route result = routeService.create(route);

        assertNotNull(result);
        assertEquals("Pune", result.getFromCity());
        assertEquals("Mumbai", result.getToCity());
        assertEquals(2, result.getBreakPoints());
        verify(routeRepository, times(1)).save(route);
    }

    @Test
    void create_withNullBreakPoints() {
        Route routeNoBP = Route.builder()
                .fromCity("Pune")
                .toCity("Delhi")
                .breakPoints(null)
                .duration(720)
                .build();
        when(routeRepository.save(any(Route.class))).thenReturn(routeNoBP);

        Route result = routeService.create(routeNoBP);

        assertNotNull(result);
        assertNull(result.getBreakPoints());
    }

    @Test
    void create_withZeroDuration() {
        Route zeroRoute = Route.builder()
                .fromCity("CityA")
                .toCity("CityB")
                .breakPoints(0)
                .duration(0)
                .build();
        when(routeRepository.save(any(Route.class))).thenReturn(zeroRoute);

        Route result = routeService.create(zeroRoute);

        assertEquals(0, result.getDuration());
    }

    // ==================== getAll ====================

    @Test
    void getAll_success() {
        Route route2 = Route.builder().routeId(2).fromCity("Mumbai").toCity("Goa").breakPoints(1).duration(480).build();
        when(routeRepository.findAll()).thenReturn(List.of(route, route2));

        List<Route> result = routeService.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void getAll_emptyList() {
        when(routeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Route> result = routeService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_singleElement() {
        when(routeRepository.findAll()).thenReturn(List.of(route));

        List<Route> result = routeService.getAll();

        assertEquals(1, result.size());
        assertEquals("Pune", result.get(0).getFromCity());
    }

    // ==================== getById ====================

    @Test
    void getById_success() {
        when(routeRepository.findById(1)).thenReturn(Optional.of(route));

        Route result = routeService.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getRouteId());
    }

    @Test
    void getById_notFound() {
        when(routeRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> routeService.getById(999));

        assertEquals("Route not found with id: 999", exception.getMessage());
    }

    @Test
    void getById_withNullId() {
        when(routeRepository.findById(null)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> routeService.getById(null));
    }

    // ==================== update ====================

    @Test
    void update_success() {
        Route updatedRoute = Route.builder()
                .fromCity("Pune")
                .toCity("Nashik")
                .breakPoints(1)
                .duration(120)
                .build();

        when(routeRepository.findById(1)).thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        Route result = routeService.update(1, updatedRoute);

        assertNotNull(result);
        assertEquals("Nashik", route.getToCity());
        assertEquals(1, route.getBreakPoints());
    }

    @Test
    void update_notFound() {
        Route updatedRoute = Route.builder().fromCity("X").build();

        when(routeRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> routeService.update(999, updatedRoute));

        verify(routeRepository, never()).save(any());
    }

    @Test
    void update_partialFields() {
        Route partialUpdate = Route.builder()
                .toCity("Nagpur")
                .build();

        when(routeRepository.findById(1)).thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        Route result = routeService.update(1, partialUpdate);

        assertNotNull(result);
        assertEquals("Nagpur", route.getToCity());
        assertEquals("Pune", route.getFromCity()); // unchanged
    }

    // ==================== delete ====================

    @Test
    void delete_success() {
        when(routeRepository.findById(1)).thenReturn(Optional.of(route));
        when(tripRepository.existsByRoute_RouteId(1)).thenReturn(false);

        assertDoesNotThrow(() -> routeService.delete(1));

        verify(routeRepository, times(1)).delete(route);
    }

    @Test
    void delete_notFound() {
        when(routeRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> routeService.delete(999));

        verify(routeRepository, never()).delete(any());
    }

    @Test
    void delete_routeUsedInTrips() {
        when(routeRepository.findById(1)).thenReturn(Optional.of(route));
        when(tripRepository.existsByRoute_RouteId(1)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> routeService.delete(1));

        assertEquals("Route is used in trips. Remove associated trips before deleting the route.", exception.getMessage());
        verify(routeRepository, never()).delete(any());
    }

    // ==================== searchRoutes ====================

    @Test
    void searchRoutes_success() {
        when(routeRepository.findByFromCityIgnoreCaseAndToCityIgnoreCase("Pune", "Mumbai"))
                .thenReturn(List.of(route));

        List<Route> result = routeService.searchRoutes("Pune", "Mumbai");

        assertEquals(1, result.size());
        assertEquals("Pune", result.get(0).getFromCity());
    }

    @Test
    void searchRoutes_noResults() {
        when(routeRepository.findByFromCityIgnoreCaseAndToCityIgnoreCase("X", "Y"))
                .thenReturn(Collections.emptyList());

        List<Route> result = routeService.searchRoutes("X", "Y");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchRoutes_caseInsensitive() {
        when(routeRepository.findByFromCityIgnoreCaseAndToCityIgnoreCase("pune", "mumbai"))
                .thenReturn(List.of(route));

        List<Route> result = routeService.searchRoutes("pune", "mumbai");

        assertEquals(1, result.size());
    }
}
