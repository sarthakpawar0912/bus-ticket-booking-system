package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.AgencyOfficeDTO;
import com.busticketbookingsystem.agency.service.AgencyOfficeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AgencyOfficeController {

    private final AgencyOfficeService agencyOfficeService;

    // ======================== REST API Endpoints ========================

    @PostMapping("/api/offices")
    @ResponseBody
    public AgencyOfficeDTO createOffice(@Valid @RequestBody AgencyOfficeDTO officeDTO) {
        return agencyOfficeService.create(officeDTO);
    }

    @GetMapping("/api/offices")
    @ResponseBody
    public List<AgencyOfficeDTO> getAllOffices() {
        return agencyOfficeService.getAll();
    }

    @GetMapping("/api/offices/{id}")
    @ResponseBody
    public AgencyOfficeDTO getOfficeById(@PathVariable Integer id) {
        return agencyOfficeService.getById(id);
    }

    @GetMapping("/api/offices/agency/{agencyId}")
    @ResponseBody
    public List<AgencyOfficeDTO> getOfficesByAgencyId(@PathVariable Integer agencyId) {
        return agencyOfficeService.getByAgencyId(agencyId);
    }

    @PutMapping("/api/offices/{id}")
    @ResponseBody
    public AgencyOfficeDTO updateOffice(@PathVariable Integer id, @Valid @RequestBody AgencyOfficeDTO officeDTO) {
        return agencyOfficeService.update(id, officeDTO);
    }

    @DeleteMapping("/api/offices/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteOffice(@PathVariable Integer id) {
        agencyOfficeService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Agency office deleted successfully"));
    }
}
