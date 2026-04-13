package com.busticketbookingsystem.trip.controller;

import com.busticketbookingsystem.agency.service.BusService;
import com.busticketbookingsystem.agency.service.DriverService;
import com.busticketbookingsystem.customer.service.AddressService;
import com.busticketbookingsystem.trip.dto.TripDTO;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.service.RouteService;
import com.busticketbookingsystem.trip.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("java:S4684")
@Controller
public class TripController {

    private static final String ATTR_MESSAGE = "message";
    private static final String ATTR_ERROR = "error";
    private static final String REDIRECT_VIEW_TRIPS = "redirect:/view/trips";

    private final TripService tripService;
    private final RouteService routeService;
    private final BusService busService;
    private final AddressService addressService;
    private final DriverService driverService;

    public TripController(TripService tripService, RouteService routeService,
                          BusService busService, AddressService addressService,
                          DriverService driverService) {
        this.tripService = tripService;
        this.routeService = routeService;
        this.busService = busService;
        this.addressService = addressService;
        this.driverService = driverService;
    }

    // ======================== Helper ========================

    private TripDTO mapToDTO(Trip t) {
        return TripDTO.builder()
                .tripId(t.getTripId())
                .routeId(t.getRoute() != null ? t.getRoute().getRouteId() : null)
                .busId(t.getBus() != null ? t.getBus().getBusId() : null)
                .boardingAddressId(t.getBoardingAddress() != null ? t.getBoardingAddress().getAddressId() : null)
                .droppingAddressId(t.getDroppingAddress() != null ? t.getDroppingAddress().getAddressId() : null)
                .departureTime(t.getDepartureTime())
                .arrivalTime(t.getArrivalTime())
                .driver1Id(t.getDriver1() != null ? t.getDriver1().getDriverId() : null)
                .driver2Id(t.getDriver2() != null ? t.getDriver2().getDriverId() : null)
                .availableSeats(t.getAvailableSeats())
                .fare(t.getFare())
                .tripDate(t.getTripDate())
                .fromCity(t.getRoute() != null ? t.getRoute().getFromCity() : null)
                .toCity(t.getRoute() != null ? t.getRoute().getToCity() : null)
                .busType(t.getBus() != null ? t.getBus().getType() : null)
                .registrationNumber(t.getBus() != null ? t.getBus().getRegistrationNumber() : null)
                .boardingAddress(t.getBoardingAddress() != null
                        ? t.getBoardingAddress().getAddress() + ", " + t.getBoardingAddress().getCity() : null)
                .droppingAddress(t.getDroppingAddress() != null
                        ? t.getDroppingAddress().getAddress() + ", " + t.getDroppingAddress().getCity() : null)
                .build();
    }

    // ======================== REST API ========================

    @PostMapping("/api/trips")
    @ResponseBody
    public ResponseEntity<TripDTO> createTrip(@RequestBody TripDTO dto) {
        Trip trip = new Trip();
        trip.setDepartureTime(dto.getDepartureTime());
        trip.setArrivalTime(dto.getArrivalTime());
        trip.setAvailableSeats(dto.getAvailableSeats());
        trip.setFare(dto.getFare());
        trip.setTripDate(dto.getTripDate());
        Trip saved = tripService.create(trip, dto.getRouteId(), dto.getBusId(),
                dto.getBoardingAddressId(), dto.getDroppingAddressId(),
                dto.getDriver1Id(), dto.getDriver2Id());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(saved));
    }

    @GetMapping("/api/trips")
    @ResponseBody
    public List<TripDTO> getAllTrips() {
        return tripService.getAllTrips().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @GetMapping("/api/trips/{id}")
    @ResponseBody
    public TripDTO getTripById(@PathVariable Integer id) {
        return mapToDTO(tripService.getById(id));
    }

    @PutMapping("/api/trips/{id}")
    @ResponseBody
    public TripDTO updateTrip(@PathVariable Integer id, @RequestBody Trip trip) {
        return mapToDTO(tripService.update(id, trip));
    }

    @DeleteMapping("/api/trips/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteTrip(@PathVariable Integer id) {
        tripService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/trips/search")
    @ResponseBody
    public List<TripDTO> searchTrips(@RequestParam String from, @RequestParam String to) {
        return tripService.searchTrips(from, to).stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view/trips")
    public String listTrips(Model model) {
        List<TripDTO> trips = tripService.getAllTrips().stream()
                .map(this::mapToDTO)
                .toList();
        model.addAttribute("trips", trips);
        return "trip/trips";
    }

    @GetMapping("/view/trips/add")
    public String showAddForm(Model model) {
        model.addAttribute("routes", routeService.getAll());
        model.addAttribute("buses", busService.getAllBuses());
        model.addAttribute("addresses", addressService.getAll());
        model.addAttribute("drivers", driverService.getAllDrivers());
        return "trip/add-trip";
    }

    @PostMapping("/view/trips/save")
    public String saveTrip(@RequestParam Integer routeId,
                           @RequestParam Integer busId,
                           @RequestParam Integer boardingAddressId,
                           @RequestParam Integer droppingAddressId,
                           @RequestParam String departureTime,
                           @RequestParam String arrivalTime,
                           @RequestParam Integer driver1Id,
                           @RequestParam(required = false) Integer driver2Id,
                           @RequestParam Integer availableSeats,
                           @RequestParam BigDecimal fare,
                           @RequestParam String tripDate,
                           RedirectAttributes ra) {
        try {
            Trip trip = new Trip();
            trip.setDepartureTime(LocalDateTime.parse(departureTime));
            trip.setArrivalTime(LocalDateTime.parse(arrivalTime));
            trip.setAvailableSeats(availableSeats);
            trip.setFare(fare);
            trip.setTripDate(LocalDateTime.parse(tripDate));

            tripService.create(trip, routeId, busId, boardingAddressId, droppingAddressId, driver1Id, driver2Id);
            ra.addFlashAttribute(ATTR_MESSAGE, "Trip added successfully!");
        } catch (Exception ex) {
            ra.addFlashAttribute(ATTR_ERROR, ex.getMessage());
        }
        return REDIRECT_VIEW_TRIPS;
    }

    @GetMapping("/view/trips/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Trip trip = tripService.getById(id);
        model.addAttribute("trip", mapToDTO(trip));
        model.addAttribute("routes", routeService.getAll());
        model.addAttribute("buses", busService.getAllBuses());
        model.addAttribute("addresses", addressService.getAll());
        model.addAttribute("drivers", driverService.getAllDrivers());
        return "trip/update-trip";
    }

    @PostMapping("/view/trips/update/{id}")
    public String updateTrip(@PathVariable Integer id,
                             @RequestParam Integer routeId,
                             @RequestParam Integer busId,
                             @RequestParam Integer boardingAddressId,
                             @RequestParam Integer droppingAddressId,
                             @RequestParam String departureTime,
                             @RequestParam String arrivalTime,
                             @RequestParam Integer driver1Id,
                             @RequestParam(required = false) Integer driver2Id,
                             @RequestParam Integer availableSeats,
                             @RequestParam BigDecimal fare,
                             @RequestParam String tripDate,
                             RedirectAttributes ra) {
        try {
            Trip trip = new Trip();
            trip.setDepartureTime(LocalDateTime.parse(departureTime));
            trip.setArrivalTime(LocalDateTime.parse(arrivalTime));
            trip.setAvailableSeats(availableSeats);
            trip.setFare(fare);
            trip.setTripDate(LocalDateTime.parse(tripDate));

            tripService.update(id, trip);
            ra.addFlashAttribute(ATTR_MESSAGE, "Trip updated successfully!");
        } catch (Exception ex) {
            ra.addFlashAttribute(ATTR_ERROR, ex.getMessage());
        }
        return REDIRECT_VIEW_TRIPS;
    }

    @GetMapping("/view/trips/delete/{id}")
    public String deleteTripView(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            tripService.delete(id);
            ra.addFlashAttribute(ATTR_MESSAGE, "Trip deleted successfully!");
        } catch (Exception ex) {
            ra.addFlashAttribute(ATTR_ERROR, ex.getMessage());
        }
        return REDIRECT_VIEW_TRIPS;
    }
}
