package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.DriverRequestDTO;
import com.busticketbookingsystem.agency.dto.DriverResponseDTO;
import com.busticketbookingsystem.agency.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    // Constructor Injection
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @PostMapping
    public ResponseEntity<DriverResponseDTO> addDriver(@Valid @RequestBody DriverRequestDTO requestDTO) {
        DriverResponseDTO response = driverService.addDriver(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @GetMapping
    public ResponseEntity<List<DriverResponseDTO>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponseDTO> getDriverById(@PathVariable Integer id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponseDTO> updateDriver(@PathVariable Integer id,
                                                          @Valid @RequestBody DriverRequestDTO requestDTO) {
        return ResponseEntity.ok(driverService.updateDriver(id, requestDTO));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDriver(@PathVariable Integer id) {
        driverService.deleteDriver(id);
        return ResponseEntity.ok("Driver deleted successfully.");
    }
}