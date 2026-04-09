package com.busticketbookingsystem.CUSTOMER.SERVICE;

import com.busticketbookingsystem.CUSTOMER.ENTITY.Address;
import com.busticketbookingsystem.CUSTOMER.EXCEPTION.ResourceNotFoundException;
import com.busticketbookingsystem.CUSTOMER.REPOSITORY.AddressRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }


    public Address create(Address address) {
        return addressRepository.save(address);
    }


    public List<Address> getAll() {
        return addressRepository.findAll();
    }


   public Address getById(Integer id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }


    public Address update(Integer id, Address updatedAddress) {
        Address address = getById(id);

        address.setAddress(updatedAddress.getAddress());
        address.setCity(updatedAddress.getCity());
        address.setState(updatedAddress.getState());
        address.setZipCode(updatedAddress.getZipCode());

        return addressRepository.save(address);
    }


    public void delete(Integer id) {
        if (!addressRepository.existsById(id)) {
            throw new ResourceNotFoundException("Address not found");
        }
        addressRepository.deleteById(id);
    }
}