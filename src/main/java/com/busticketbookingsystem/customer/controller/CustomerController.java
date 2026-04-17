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

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view/customers")
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.getAll());
        return "customer/customers";
    }

    @GetMapping("/view/customers/add")
    public String showAddForm(Model model) {
        model.addAttribute("customer", new CustomerRequestDTO());
        return "customer/add-customer";
    }

    /**
     * Creates a new Address first from the inline form fields, then a Customer
     * pointing to that brand-new Address. This way every customer enters their
     * OWN address rather than picking from other customers' existing addresses.
     */
    @PostMapping("/view/customers/save")
    public String saveCustomer(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String phone,
                               @RequestParam String address,
                               @RequestParam String city,
                               @RequestParam String state,
                               @RequestParam String zipCode,
                               org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            com.busticketbookingsystem.customer.entity.Address addr =
                    new com.busticketbookingsystem.customer.entity.Address();
            addr.setAddress(address);
            addr.setCity(city);
            addr.setState(state);
            addr.setZipCode(zipCode);
            com.busticketbookingsystem.customer.entity.Address savedAddr = addressService.create(addr);

            CustomerRequestDTO dto = new CustomerRequestDTO();
            dto.setName(name);
            dto.setEmail(email);
            dto.setPhone(phone);
            dto.setAddressId(savedAddr.getAddressId());
            customerService.create(dto);

            ra.addFlashAttribute("message", "Customer added successfully.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
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
