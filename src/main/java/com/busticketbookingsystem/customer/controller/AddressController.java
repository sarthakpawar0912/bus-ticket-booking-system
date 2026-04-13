package com.busticketbookingsystem.customer.controller;

import com.busticketbookingsystem.customer.dto.AddressDTO;
import com.busticketbookingsystem.customer.entity.Address;
import com.busticketbookingsystem.customer.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("java:S4684")
@Controller
public class AddressController {

    private static final String REDIRECT_VIEW_ADDRESSES = "redirect:/view/addresses";

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }


    @PostMapping("/api/addresses")
    @ResponseBody
    public Address create(@Valid @RequestBody AddressDTO addressDTO) {
        Address address = mapToEntity(addressDTO);
        return addressService.create(address);
    }

    private Address mapToEntity(AddressDTO dto) {
        return Address.builder()
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .build();
    }

    @GetMapping("/api/addresses")
    @ResponseBody
    public List<Address> getAll() {
        return addressService.getAll();
    }

    @GetMapping("/api/addresses/{id}")
    @ResponseBody
    public Address getById(@PathVariable Integer id) {
        return addressService.getById(id);
    }

    @PutMapping("/api/addresses/{id}")
    @ResponseBody
    public Address update(@PathVariable Integer id,
                          @Valid @RequestBody AddressDTO addressDTO) {

        Address address = mapToEntity(addressDTO);
        return addressService.update(id, address);
    }

    @DeleteMapping("/api/addresses/{id}")
    @ResponseBody
    public String delete(@PathVariable Integer id) {
        addressService.delete(id);
        return "Address deleted successfully";
    }

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view/addresses")
    public String listAddresses(Model model) {
        model.addAttribute("addresses", addressService.getAll());
        return "address/addresses";
    }

    @GetMapping("/view/addresses/add")
    public String showAddForm(Model model) {
        model.addAttribute("address", new Address());
        return "address/add-address";
    }

    @PostMapping("/view/addresses/save")
    public String saveAddress(@RequestParam String address,
                              @RequestParam String city,
                              @RequestParam String state,
                              @RequestParam String zipCode) {
        Address addr = new Address();
        addr.setAddress(address);
        addr.setCity(city);
        addr.setState(state);
        addr.setZipCode(zipCode);
        addressService.create(addr);
        return REDIRECT_VIEW_ADDRESSES;
    }

    @GetMapping("/view/addresses/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("address", addressService.getById(id));
        return "address/update-address";
    }

    @PostMapping("/view/addresses/update/{id}")
    public String updateAddress(@PathVariable Integer id,
                                @RequestParam String address,
                                @RequestParam String city,
                                @RequestParam String state,
                                @RequestParam String zipCode) {
        Address addr = new Address();
        addr.setAddress(address);
        addr.setCity(city);
        addr.setState(state);
        addr.setZipCode(zipCode);
        addressService.update(id, addr);
        return REDIRECT_VIEW_ADDRESSES;
    }

    @GetMapping("/view/addresses/delete/{id}")
    public String deleteAddress(@PathVariable Integer id) {
        addressService.delete(id);
        return REDIRECT_VIEW_ADDRESSES;
    }
}