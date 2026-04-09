package com.busticketbookingsystem.CUSTOMER.controller;

import com.busticketbookingsystem.CUSTOMER.dto.CustomerRequestDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/view/customers")
public class CustomerViewController {

    private final com.busticketbookingsystem.CUSTOMER.SERVICE.CustomerService service;

    public CustomerViewController(com.busticketbookingsystem.CUSTOMER.SERVICE.CustomerService service) {
        this.service = service;
    }

    // 📄 LIST PAGE
    @GetMapping
    public String listCustomers(Model model) {
        model.addAttribute("customers", service.getAll());
        return "customers";
    }

    // ➕ ADD FORM PAGE
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("customer", new CustomerRequestDTO());
        return "add-customer";
    }

    // 💾 SAVE CUSTOMER
    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute CustomerRequestDTO dto) {
        service.create(dto);
        return "redirect:/view/customers";
    }

    // ✏ UPDATE FORM
    @GetMapping("/edit/{id}")
    public String editCustomer(@PathVariable Integer id, Model model) {
        model.addAttribute("customer", service.getById(id));
        return "update-customer";
    }

    // 🔄 UPDATE
    @PostMapping("/update/{id}")
    public String updateCustomer(@PathVariable Integer id,
                                 @ModelAttribute CustomerRequestDTO dto) {
        service.update(id, dto);
        return "redirect:/view/customers";
    }

    // ❌ DELETE
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/view/customers";
    }
}