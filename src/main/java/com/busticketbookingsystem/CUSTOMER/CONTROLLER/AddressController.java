package com.busticketbookingsystem.CUSTOMER.CONTROLLER;

import com.busticketbookingsystem.CUSTOMER.ENTITY.Address;
import com.busticketbookingsystem.CUSTOMER.SERVICE.AddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }


    @PostMapping
    public Address create(@RequestBody Address address) {
        return addressService.create(address);
    }


    @GetMapping
    public List<Address> getAll() {
        return addressService.getAll();
    }


    @GetMapping("/{id}")
    public Address getById(@PathVariable Integer id) {
        return addressService.getById(id);
    }


    @PutMapping("/{id}")
    public Address update(@PathVariable Integer id, @RequestBody Address address) {
        return addressService.update(id, address);
    }


    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        addressService.delete(id);
        return "Address deleted successfully";
    }
}