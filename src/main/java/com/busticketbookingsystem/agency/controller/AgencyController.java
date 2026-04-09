package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.entity.*;
import com.busticketbookingsystem.agency.service.AgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agency")
public class AgencyController {

    @Autowired
    private AgencyService agencyService;

    // Create Agency
    @PostMapping("/create")
    public Agency createAgency(@RequestBody Agency agency) {
        return agencyService.createAgency(agency);
    }

    // Get Agencies
    @GetMapping("/all")
    public List<Agency> getAll() {
        return agencyService.getAllAgencies();
    }

    // Add Office
    @PostMapping("/office/{agencyId}")
    public AgencyOffice addOffice(@PathVariable Integer agencyId,
                                  @RequestBody AgencyOffice office) {
        return agencyService.addOffice(agencyId, office);
    }

    // Add Bus
    @PostMapping("/bus/{officeId}")
    public Bus addBus(@PathVariable Integer officeId,
                      @RequestBody Bus bus) {
        return agencyService.addBus(officeId, bus);
    }

    // Get Buses
    @GetMapping("/buses")
    public List<Bus> getBuses() {
        return agencyService.getAllBuses();
    }

    @GetMapping("/{id}")
    public Agency getById(@PathVariable Integer id) {
        return agencyService.getAgencyById(id);
    }

    @PutMapping("/update/{id}")
    public Agency updateAgency(@PathVariable Integer id,
                               @RequestBody Agency agency) {
        return agencyService.updateAgency(id, agency);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteAgency(@PathVariable Integer id) {
        agencyService.deleteAgency(id);
        return "Agency deleted successfully";
    }

    @PutMapping("/bus/update/{id}")
    public Bus updateBus(@PathVariable Integer id,
                         @RequestBody Bus bus) {
        return agencyService.updateBus(id, bus);
    }

    @DeleteMapping("/bus/delete/{id}")
    public String deleteBus(@PathVariable Integer id) {
        agencyService.deleteBus(id);
        return "Bus deleted successfully";
    }
}