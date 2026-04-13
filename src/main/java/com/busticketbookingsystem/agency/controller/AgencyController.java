package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.AgencyRequestDTO;
import com.busticketbookingsystem.agency.dto.AgencyResponseDTO;
import com.busticketbookingsystem.agency.dto.OfficeRequestDTO;
import com.busticketbookingsystem.agency.dto.OfficeResponseDTO;
import com.busticketbookingsystem.agency.service.AgencyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AgencyController {

    private final AgencyService agencyService;

    public AgencyController(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    // ======================== REST API - AGENCIES ========================

    @PostMapping("/api/agencies")
    @ResponseBody
    public ResponseEntity<AgencyResponseDTO> createAgency(@Valid @RequestBody AgencyRequestDTO requestDTO) {
        return new ResponseEntity<>(agencyService.createAgency(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/api/agencies")
    @ResponseBody
    public ResponseEntity<List<AgencyResponseDTO>> getAllAgencies() {
        return ResponseEntity.ok(agencyService.getAllAgencies());
    }

    @GetMapping("/api/agencies/{id}")
    @ResponseBody
    public ResponseEntity<AgencyResponseDTO> getAgencyById(@PathVariable Integer id) {
        return ResponseEntity.ok(agencyService.getAgencyById(id));
    }

    @PutMapping("/api/agencies/{id}")
    @ResponseBody
    public ResponseEntity<AgencyResponseDTO> updateAgency(@PathVariable Integer id,
                                                          @Valid @RequestBody AgencyRequestDTO requestDTO) {
        return ResponseEntity.ok(agencyService.updateAgency(id, requestDTO));
    }

    @DeleteMapping("/api/agencies/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteAgency(@PathVariable Integer id) {
        agencyService.deleteAgency(id);
        return ResponseEntity.ok("Agency deleted successfully.");
    }

    // ======================== REST API - OFFICES ========================

    @GetMapping("/api/agencies/offices")
    @ResponseBody
    public ResponseEntity<List<OfficeResponseDTO>> getAllOffices() {
        return ResponseEntity.ok(agencyService.getAllOffices());
    }

    @GetMapping("/api/agencies/offices/{id}")
    @ResponseBody
    public ResponseEntity<OfficeResponseDTO> getOfficeById(@PathVariable Integer id) {
        return ResponseEntity.ok(agencyService.getOfficeById(id));
    }

    @PostMapping("/api/agencies/offices")
    @ResponseBody
    public ResponseEntity<OfficeResponseDTO> createOffice(@Valid @RequestBody OfficeRequestDTO requestDTO) {
        return new ResponseEntity<>(agencyService.createOffice(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/api/agencies/offices/{id}")
    @ResponseBody
    public ResponseEntity<OfficeResponseDTO> updateOffice(@PathVariable Integer id,
                                                          @Valid @RequestBody OfficeRequestDTO requestDTO) {
        return ResponseEntity.ok(agencyService.updateOffice(id, requestDTO));
    }

    @DeleteMapping("/api/agencies/offices/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteOffice(@PathVariable Integer id) {
        agencyService.deleteOffice(id);
        return ResponseEntity.ok("Office deleted successfully.");
    }

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view/agencies")
    public String listAgencies(Model model) {
        model.addAttribute("agencies", agencyService.getAllAgencies());
        return "agency/agencies";
    }

    @GetMapping("/view/agencies/add")
    public String showAddForm(Model model) {
        model.addAttribute("agency", new AgencyRequestDTO());
        return "agency/add-agency";
    }

    @PostMapping("/view/agencies/save")
    public String saveAgency(@ModelAttribute AgencyRequestDTO dto) {
        agencyService.createAgency(dto);
        return "redirect:/view/agencies";
    }

    @GetMapping("/view/agencies/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("agency", agencyService.getAgencyById(id));
        return "agency/update-agency";
    }

    @PostMapping("/view/agencies/update/{id}")
    public String updateAgencyView(@PathVariable Integer id,
                                   @ModelAttribute AgencyRequestDTO dto) {
        agencyService.updateAgency(id, dto);
        return "redirect:/view/agencies";
    }
}
