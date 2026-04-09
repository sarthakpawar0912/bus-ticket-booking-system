package com.busticketbookingsystem.CUSTOMER.CONTROLLER;

import com.busticketbookingsystem.CUSTOMER.DTO.CustomerRequestDTO;
import com.busticketbookingsystem.CUSTOMER.DTO.CustomerResponseDTO;
import com.busticketbookingsystem.CUSTOMER.SERVICE.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
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
        return customerService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        customerService.delete(id);
        return "Customer deleted";
    }



}
