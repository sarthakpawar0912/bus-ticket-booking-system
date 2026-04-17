package com.busticketbookingsystem.agency.controller;

import com.busticketbookingsystem.agency.dto.AgencyOfficeDTO;
import com.busticketbookingsystem.agency.service.AgencyOfficeService;
import com.busticketbookingsystem.agency.service.AgencyService;
import com.busticketbookingsystem.customer.service.AddressService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Thymeleaf view layer for Agency Offices. The REST side lives in
 * AgencyOfficeController (@RestController); this controller renders HTML
 * pages so "Atharv Kadam" has UI screens for Offices.
 */
@Controller
@RequestMapping("/view/offices")
public class OfficeViewController {

    private static final String REDIRECT = "redirect:/view/offices";

    private final AgencyOfficeService officeService;
    private final AgencyService agencyService;
    private final AddressService addressService;

    public OfficeViewController(AgencyOfficeService officeService,
                                AgencyService agencyService,
                                AddressService addressService) {
        this.officeService = officeService;
        this.agencyService = agencyService;
        this.addressService = addressService;
    }

    @GetMapping
    public String listOffices(Model model) {
        model.addAttribute("offices", officeService.getAll());
        return "office/offices";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("office", new AgencyOfficeDTO());
        model.addAttribute("agencies", agencyService.getAllAgencies());
        model.addAttribute("addresses", addressService.getAll());
        return "office/add-office";
    }

    @PostMapping("/save")
    public String saveOffice(@ModelAttribute("office") AgencyOfficeDTO dto,
                             RedirectAttributes ra) {
        try {
            officeService.create(dto);
            ra.addFlashAttribute("message", "Office added successfully!");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return REDIRECT;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("office", officeService.getById(id));
        model.addAttribute("agencies", agencyService.getAllAgencies());
        model.addAttribute("addresses", addressService.getAll());
        return "office/update-office";
    }

    @PostMapping("/update/{id}")
    public String updateOffice(@PathVariable Integer id,
                               @ModelAttribute("office") AgencyOfficeDTO dto,
                               RedirectAttributes ra) {
        try {
            officeService.update(id, dto);
            ra.addFlashAttribute("message", "Office updated successfully!");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return REDIRECT;
    }
}
