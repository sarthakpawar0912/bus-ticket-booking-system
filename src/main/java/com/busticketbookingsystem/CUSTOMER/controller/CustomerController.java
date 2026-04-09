package com.busticketbookingsystem.CUSTOMER.controller;

import com.busticketbookingsystem.CUSTOMER.dto.CustomerRequestDTO;
import com.busticketbookingsystem.CUSTOMER.dto.CustomerResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final com.busticketbookingsystem.CUSTOMER.SERVICE.CustomerService customerService;

    public CustomerController(com.busticketbookingsystem.CUSTOMER.SERVICE.CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public CustomerResponseDTO create(@Valid  @RequestBody CustomerRequestDTO dto){
        return customerService.create(dto);
    }

    @GetMapping
    public List<CustomerResponseDTO> getAll() {
        return customerService.getAll();
    }

    @GetMapping("/{id}")
    public CustomerResponseDTO getById(@PathVariable Integer id) {
        return customerService.getById(id);
    }

    @PutMapping("/{id}")
    public CustomerResponseDTO update(@PathVariable Integer id,
                                      @Valid @RequestBody CustomerRequestDTO dto) {
        return customerService.update(id, dto);
    }

    @PatchMapping("/{id}")
    public CustomerResponseDTO patch(@PathVariable Integer id,
                                     @RequestBody CustomerRequestDTO dto) {
        return customerService.patch(id, dto); // ✅ call PATCH method
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        customerService.delete(id);
        return "Customer deleted";
    }



}
