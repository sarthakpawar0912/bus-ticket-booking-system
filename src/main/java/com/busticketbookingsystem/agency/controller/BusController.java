package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.BusRequestDTO;
import com.busticketbookingsystem.agency.dto.BusResponseDTO;
import com.busticketbookingsystem.agency.service.AgencyOfficeService;
import com.busticketbookingsystem.agency.service.BusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class BusController {

    private static final String REDIRECT_VIEW_BUSES = "redirect:/view/buses";

    private final BusService busService;
    private final AgencyOfficeService agencyOfficeService;

    public BusController(BusService busService, AgencyOfficeService agencyOfficeService) {
        this.busService = busService;
        this.agencyOfficeService = agencyOfficeService;
    }

    // ======================== REST API ========================

    @PostMapping("/api/buses")
    @ResponseBody
    public ResponseEntity<BusResponseDTO> addBus(@Valid @RequestBody BusRequestDTO requestDTO) {
        BusResponseDTO response = busService.addBus(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/buses")
    @ResponseBody
    public ResponseEntity<List<BusResponseDTO>> getAllBuses() {
        return ResponseEntity.ok(busService.getAllBuses());
    }

    @GetMapping("/api/buses/{id}")
    @ResponseBody
    public ResponseEntity<BusResponseDTO> getBusById(@PathVariable Integer id) {
        return ResponseEntity.ok(busService.getBusById(id));
    }

    @GetMapping("/api/buses/office/{officeId}")
    @ResponseBody
    public ResponseEntity<List<BusResponseDTO>> getBusesByOffice(@PathVariable Integer officeId) {
        return ResponseEntity.ok(busService.getBusesByOfficeId(officeId));
    }

    @PutMapping("/api/buses/{id}")
    @ResponseBody
    public ResponseEntity<BusResponseDTO> updateBus(@PathVariable Integer id,
                                                    @Valid @RequestBody BusRequestDTO requestDTO) {
        return ResponseEntity.ok(busService.updateBus(id, requestDTO));
    }

    @DeleteMapping("/api/buses/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteBus(@PathVariable Integer id) {
        busService.deleteBus(id);
        return ResponseEntity.ok("Bus deleted successfully.");
    }

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view/buses")
    public String listBuses(Model model) {
        model.addAttribute("buses", busService.getAllBuses());
        return "bus/buses";
    }

    @GetMapping("/view/buses/add")
    public String showAddForm(Model model) {
        model.addAttribute("bus", new BusRequestDTO());
        model.addAttribute("offices", agencyOfficeService.getAll());
        return "bus/add-bus";
    }

    @PostMapping("/view/buses/save")
    public String saveBus(@ModelAttribute BusRequestDTO dto) {
        busService.addBus(dto);
        return REDIRECT_VIEW_BUSES;
    }

    @GetMapping("/view/buses/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("bus", busService.getBusById(id));
        model.addAttribute("offices", agencyOfficeService.getAll());
        return "bus/update-bus";
    }

    @PostMapping("/view/buses/update/{id}")
    public String updateBusView(@PathVariable Integer id,
                                @ModelAttribute BusRequestDTO dto) {
        busService.updateBus(id, dto);
        return REDIRECT_VIEW_BUSES;
    }

    @GetMapping("/view/buses/delete/{id}")
    public String deleteBusView(@PathVariable Integer id) {
        busService.deleteBus(id);
        return REDIRECT_VIEW_BUSES;
    }
}
