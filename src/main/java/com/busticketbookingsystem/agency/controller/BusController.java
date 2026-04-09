package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.BusRequestDTO;
import com.busticketbookingsystem.agency.dto.BusResponseDTO;
import com.busticketbookingsystem.agency.service.BusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
public class BusController {

    private final BusService busService;

    // Constructor Injection
    public BusController(BusService busService) {
        this.busService = busService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @PostMapping
    public ResponseEntity<BusResponseDTO> addBus(@Valid @RequestBody BusRequestDTO requestDTO) {
        BusResponseDTO response = busService.addBus(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BusResponseDTO>> getAllBuses() {
        return ResponseEntity.ok(busService.getAllBuses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusResponseDTO> getBusById(@PathVariable Integer id) {
        return ResponseEntity.ok(busService.getBusById(id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<BusResponseDTO> updateBus(@PathVariable Integer id,
                                                    @Valid @RequestBody BusRequestDTO requestDTO) {
        return ResponseEntity.ok(busService.updateBus(id, requestDTO));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBus(@PathVariable Integer id) {
        busService.deleteBus(id);
        return ResponseEntity.ok("Bus deleted successfully.");
    }
}