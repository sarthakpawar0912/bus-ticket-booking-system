package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.AgencyRequestDTO;
import com.busticketbookingsystem.agency.dto.AgencyResponseDTO;
import com.busticketbookingsystem.agency.dto.OfficeRequestDTO;
import com.busticketbookingsystem.agency.dto.OfficeResponseDTO;
import com.busticketbookingsystem.agency.service.AgencyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // ✅ Fixes PreAuthorize error
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agencies")
public class AgencyController {

    private final AgencyService agencyService;

    // ✅ Constructor Injection
    public AgencyController(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    // ==========================================
    // AGENCY ENDPOINTS
    // ==========================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AgencyResponseDTO> createAgency(@Valid @RequestBody AgencyRequestDTO requestDTO) {
        return new ResponseEntity<>(agencyService.createAgency(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AgencyResponseDTO>> getAllAgencies() {
        return ResponseEntity.ok(agencyService.getAllAgencies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgencyResponseDTO> getAgencyById(@PathVariable Integer id) {
        return ResponseEntity.ok(agencyService.getAgencyById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AgencyResponseDTO> updateAgency(@PathVariable Integer id,
                                                          @Valid @RequestBody AgencyRequestDTO requestDTO) {
        return ResponseEntity.ok(agencyService.updateAgency(id, requestDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAgency(@PathVariable Integer id) {
        agencyService.deleteAgency(id);
        return ResponseEntity.ok("Agency deleted successfully.");
    }

    // ==========================================
    // OFFICE ENDPOINTS
    // ==========================================

    @GetMapping("/offices")
    public ResponseEntity<List<OfficeResponseDTO>> getAllOffices() {
        return ResponseEntity.ok(agencyService.getAllOffices());
    }

    @GetMapping("/offices/{id}")
    public ResponseEntity<OfficeResponseDTO> getOfficeById(@PathVariable Integer id) {
        return ResponseEntity.ok(agencyService.getOfficeById(id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @PostMapping("/offices")
    public ResponseEntity<OfficeResponseDTO> createOffice(@Valid @RequestBody OfficeRequestDTO requestDTO) {
        return new ResponseEntity<>(agencyService.createOffice(requestDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @PutMapping("/offices/{id}")
    public ResponseEntity<OfficeResponseDTO> updateOffice(@PathVariable Integer id,
                                                          @Valid @RequestBody OfficeRequestDTO requestDTO) {
        return ResponseEntity.ok(agencyService.updateOffice(id, requestDTO));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @DeleteMapping("/offices/{id}")
    public ResponseEntity<String> deleteOffice(@PathVariable Integer id) {
        agencyService.deleteOffice(id);
        return ResponseEntity.ok("Office deleted successfully.");
    }
}