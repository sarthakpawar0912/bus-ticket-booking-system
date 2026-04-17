package com.busticketbookingsystem.customer.controller;

import com.busticketbookingsystem.customer.dto.CustomerRequestDTO;
import com.busticketbookingsystem.customer.dto.CustomerResponseDTO;
import com.busticketbookingsystem.customer.service.AddressService;
import com.busticketbookingsystem.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CustomerController {

    private final CustomerService customerService;
    private final AddressService addressService;

    public CustomerController(CustomerService customerService, AddressService addressService) {
        this.customerService = customerService;
        this.addressService = addressService;
    }

    // ======================== REST API ========================

    @PostMapping("/api/customers")
    @ResponseBody
    public CustomerResponseDTO create(@Valid @RequestBody CustomerRequestDTO dto) {
        return customerService.create(dto);
    }

    @GetMapping("/api/customers")
    @ResponseBody
    public List<CustomerResponseDTO> getAll() {
        return customerService.getAll();
    }

    @GetMapping("/api/customers/{id}")
    @ResponseBody
    public CustomerResponseDTO getById(@PathVariable Integer id) {
        return customerService.getById(id);
    }

    @PutMapping("/api/customers/{id}")
    @ResponseBody
    public CustomerResponseDTO update(@PathVariable Integer id,
                                      @Valid @RequestBody CustomerRequestDTO dto) {
        return customerService.update(id, dto);
    }

    @PatchMapping("/api/customers/{id}")
    @ResponseBody
    public CustomerResponseDTO patch(@PathVariable Integer id,
                                     @RequestBody CustomerRequestDTO dto) {
        return customerService.patch(id, dto);
    }

    @DeleteMapping("/api/customers/{id}")
    @ResponseBody
    public String delete(@PathVariable Integer id) {
        customerService.delete(id);
        return "Customer deleted";
    }

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view/customers")
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.getAll());
        return "customer/customers";
    }

    @GetMapping("/view/customers/add")
    public String showAddForm(Model model) {
        model.addAttribute("customer", new CustomerRequestDTO());
        model.addAttribute("addresses", addressService.getAll());
        return "customer/add-customer";
    }

    @PostMapping("/view/customers/save")
    public String saveCustomer(@ModelAttribute CustomerRequestDTO dto) {
        customerService.create(dto);
        return "redirect:/view/customers";
    }

    @GetMapping("/view/customers/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("customer", customerService.getById(id));
        model.addAttribute("addresses", addressService.getAll());
        return "customer/update-customer";
    }

    @PostMapping("/view/customers/update/{id}")
    public String updateCustomer(@PathVariable Integer id,
                                 @ModelAttribute CustomerRequestDTO dto) {
        customerService.update(id, dto);
        return "redirect:/view/customers";
    }
}
