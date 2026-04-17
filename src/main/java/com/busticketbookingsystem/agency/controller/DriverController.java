package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.DriverRequestDTO;
import com.busticketbookingsystem.agency.dto.DriverResponseDTO;
import com.busticketbookingsystem.agency.service.AgencyOfficeService;
import com.busticketbookingsystem.agency.service.DriverService;
import com.busticketbookingsystem.customer.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DriverController {

    private static final String REDIRECT_VIEW_DRIVERS = "redirect:/view/drivers";

    private final DriverService driverService;
    private final AgencyOfficeService agencyOfficeService;
    private final AddressService addressService;

    public DriverController(DriverService driverService,
                            AgencyOfficeService agencyOfficeService,
                            AddressService addressService) {
        this.driverService = driverService;
        this.agencyOfficeService = agencyOfficeService;
        this.addressService = addressService;
    }

    // ======================== REST API ========================

    @PostMapping("/api/drivers")
    @ResponseBody
    public ResponseEntity<DriverResponseDTO> addDriver(@Valid @RequestBody DriverRequestDTO requestDTO) {
        DriverResponseDTO response = driverService.addDriver(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/drivers")
    @ResponseBody
    public ResponseEntity<List<DriverResponseDTO>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @GetMapping("/api/drivers/{id}")
    @ResponseBody
    public ResponseEntity<DriverResponseDTO> getDriverById(@PathVariable Integer id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @GetMapping("/api/drivers/office/{officeId}")
    @ResponseBody
    public ResponseEntity<List<DriverResponseDTO>> getDriversByOffice(@PathVariable Integer officeId) {
        return ResponseEntity.ok(driverService.getDriversByOfficeId(officeId));
    }

    @PutMapping("/api/drivers/{id}")
    @ResponseBody
    public ResponseEntity<DriverResponseDTO> updateDriver(@PathVariable Integer id,
                                                          @Valid @RequestBody DriverRequestDTO requestDTO) {
        return ResponseEntity.ok(driverService.updateDriver(id, requestDTO));
    }

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view/drivers")
    public String listDrivers(Model model) {
        model.addAttribute("drivers", driverService.getAllDrivers());
        return "driver/drivers";
    }

    @GetMapping("/view/drivers/add")
    public String showAddForm(Model model) {
        model.addAttribute("driver", new DriverRequestDTO());
        model.addAttribute("offices", agencyOfficeService.getAll());
        model.addAttribute("addresses", addressService.getAll());
        return "driver/add-driver";
    }

    @PostMapping("/view/drivers/save")
    public String saveDriver(@ModelAttribute DriverRequestDTO dto) {
        driverService.addDriver(dto);
        return REDIRECT_VIEW_DRIVERS;
    }

    @GetMapping("/view/drivers/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("driver", driverService.getDriverById(id));
        model.addAttribute("offices", agencyOfficeService.getAll());
        model.addAttribute("addresses", addressService.getAll());
        return "driver/update-driver";
    }

    @PostMapping("/view/drivers/update/{id}")
    public String updateDriverView(@PathVariable Integer id,
                                   @ModelAttribute DriverRequestDTO dto) {
        driverService.updateDriver(id, dto);
        return REDIRECT_VIEW_DRIVERS;
    }

}
