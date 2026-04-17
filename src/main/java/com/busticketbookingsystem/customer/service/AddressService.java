package com.busticketbookingsystem.customer.service;

import com.busticketbookingsystem.customer.entity.Address;
import com.busticketbookingsystem.exception.BadRequestException;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.customer.repository.AddressRepository;
import com.busticketbookingsystem.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    public AddressService(AddressRepository addressRepository, CustomerRepository customerRepository) {
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
    }

    // ✅ CREATE
    public Address create(Address address) {
        return addressRepository.save(address);
    }

    // ✅ GET ALL
    public List<Address> getAll() {
        return addressRepository.findAll();
    }

    // ✅ GET BY ID
    public Address getById(Integer id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    // ✅ UPDATE (SAFE UPDATE)
    @Transactional
    public Address update(Integer id, Address updatedAddress) {
        Address address = getById(id);

        if (updatedAddress.getAddress() != null) {
            address.setAddress(updatedAddress.getAddress());
        }
        if (updatedAddress.getCity() != null) {
            address.setCity(updatedAddress.getCity());
        }
        if (updatedAddress.getState() != null) {
            address.setState(updatedAddress.getState());
        }
        if (updatedAddress.getZipCode() != null) {
            address.setZipCode(updatedAddress.getZipCode());
        }

        return addressRepository.save(address);
    }

}