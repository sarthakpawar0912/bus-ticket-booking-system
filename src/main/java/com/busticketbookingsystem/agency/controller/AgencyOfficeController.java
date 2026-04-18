package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.AgencyOfficeDTO;
import com.busticketbookingsystem.agency.service.AgencyOfficeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AgencyOfficeController {

    private final AgencyOfficeService agencyOfficeService;

    // ======================== REST API Endpoints ========================

    @PostMapping("/api/offices")
    public AgencyOfficeDTO createOffice(@Valid @RequestBody AgencyOfficeDTO officeDTO) {
        return agencyOfficeService.create(officeDTO);
    }

    @GetMapping("/api/offices")
    public List<AgencyOfficeDTO> getAllOffices() {
        return agencyOfficeService.getAll();
    }

    @GetMapping("/api/offices/{id}")
    public AgencyOfficeDTO getOfficeById(@PathVariable Integer id) {
        return agencyOfficeService.getById(id);
    }

    @GetMapping("/api/offices/agency/{agencyId}")
    public List<AgencyOfficeDTO> getOfficesByAgencyId(@PathVariable Integer agencyId) {
        return agencyOfficeService.getByAgencyId(agencyId);
    }

    @PutMapping("/api/offices/{id}")
    public AgencyOfficeDTO updateOffice(@PathVariable Integer id, @Valid @RequestBody AgencyOfficeDTO officeDTO) {
        return agencyOfficeService.update(id, officeDTO);
    }

}
