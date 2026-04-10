package com.busticketbookingsystem.customer.controller;

import com.busticketbookingsystem.customer.dto.CustomerRequestDTO;
import com.busticketbookingsystem.customer.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/view/customers")
public class CustomerViewController {

    private final CustomerService service;

    public CustomerViewController(CustomerService service) {
        this.service = service;
    }


    @GetMapping
    public String listCustomers(Model model) {
        model.addAttribute("customers", service.getAll());
        return "customers";
    }


    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("customer", new CustomerRequestDTO());
        return "add-customer";
    }


    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute CustomerRequestDTO dto) {
        service.create(dto);
        return "redirect:/view/customers";
    }


    @GetMapping("/edit/{id}")
    public String editCustomer(@PathVariable Integer id, Model model) {
        model.addAttribute("customer", service.getById(id));
        return "update-customer";
    }

    @PostMapping("/update/{id}")
    public String updateCustomer(@PathVariable Integer id,
                                 @ModelAttribute CustomerRequestDTO dto) {
        service.update(id, dto);
        return "redirect:/view/customers";
    }


    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/view/customers";
    }
}