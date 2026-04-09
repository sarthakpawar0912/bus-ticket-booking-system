package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.AgencyRequestDTO;
import com.busticketbookingsystem.agency.dto.AgencyResponseDTO;
import com.busticketbookingsystem.agency.dto.OfficeRequestDTO;
import com.busticketbookingsystem.agency.dto.OfficeResponseDTO;
import com.busticketbookingsystem.agency.service.AgencyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Notice I changed this to plural "/api/agencies". This is a REST API best practice!
@RequestMapping("/api/agencies")
public class AgencyController {

    private final AgencyService agencyService;

    // ✅ Constructor Injection (Replaces @Autowired - Much safer and faster!)
    public AgencyController(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    /**
     * 1. Create a new Agency
     * Security: Only ADMIN can register a parent travel agency.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AgencyResponseDTO> createAgency(@Valid @RequestBody AgencyRequestDTO requestDTO) {
        AgencyResponseDTO response = agencyService.createAgency(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 2. Get All Agencies
     * Security: Public. Anyone visiting the site should be able to see the list of travel partners.
     */
    @GetMapping
    public ResponseEntity<List<AgencyResponseDTO>> getAllAgencies() {
        List<AgencyResponseDTO> agencies = agencyService.getAllAgencies();
        return ResponseEntity.ok(agencies);
    }

    /**
     * 3. Get Agency by ID
     * Security: Public.
     */

    // ----------------------------------------------------
    // MISSING OFFICE ENDPOINTS (Paste into AgencyController)
    // ----------------------------------------------------

    /**
     * Get All Offices
     * Security: Public (Passengers need to see where they can board)
     */
    @GetMapping("/offices")
    public ResponseEntity<List<OfficeResponseDTO>> getAllOffices() {
        return ResponseEntity.ok(agencyService.getAllOffices());
    }

    /**
     * Get Office by ID
     * Security: Public
     */
    @GetMapping("/offices/{id}")
    public ResponseEntity<OfficeResponseDTO> getOfficeById(@PathVariable Integer id) {
        return ResponseEntity.ok(agencyService.getOfficeById(id));
    }

    /**
     * Create an Office
     * Security: ADMIN or OPERATOR
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @PostMapping("/offices")
    public ResponseEntity<OfficeResponseDTO> createOffice(@Valid @RequestBody OfficeRequestDTO requestDTO) {
        return new ResponseEntity<>(agencyService.createOffice(requestDTO), HttpStatus.CREATED);
    }

    /**
     * Update an Office
     * Security: ADMIN or OPERATOR
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @PutMapping("/offices/{id}")
    public ResponseEntity<OfficeResponseDTO> updateOffice(@PathVariable Integer id,
                                                          @Valid @RequestBody OfficeRequestDTO requestDTO) {
        return ResponseEntity.ok(agencyService.updateOffice(id, requestDTO));
    }

    /**
     * Delete an Office
     * Security: ADMIN or OPERATOR
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @DeleteMapping("/offices/{id}")
    public ResponseEntity<String> deleteOffice(@PathVariable Integer id) {
        agencyService.deleteOffice(id);
        return ResponseEntity.ok("Office deleted successfully.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgencyResponseDTO> getAgencyById(@PathVariable Integer id) {
        AgencyResponseDTO response = agencyService.getAgencyById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. Update an Agency
     * Security: Only ADMIN can update parent agency details.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AgencyResponseDTO> updateAgency(@PathVariable Integer id,
                                                          @Valid @RequestBody AgencyRequestDTO requestDTO) {
        AgencyResponseDTO response = agencyService.updateAgency(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * 5. Delete an Agency
     * Security: Only ADMIN can delete an agency.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAgency(@PathVariable Integer id) {
        agencyService.deleteAgency(id);
        return ResponseEntity.ok("Agency deleted successfully.");
    }
}