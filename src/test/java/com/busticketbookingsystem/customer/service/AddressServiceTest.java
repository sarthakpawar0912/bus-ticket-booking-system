package com.busticketbookingsystem.customer.service;

import com.busticketbookingsystem.customer.entity.Address;
import com.busticketbookingsystem.customer.exception.ResourceNotFoundException;
import com.busticketbookingsystem.customer.repository.AddressRepository;
import com.busticketbookingsystem.customer.repository.CustomerRepository;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AddressService addressService;

    Address address;

    @BeforeEach
    void setup() {

        address = new Address();
        address.setAddressId(1);
        address.setAddress("ABC Street");
        address.setCity("Pune");
        address.setState("MH");
        address.setZipCode("411001");
    }


    @Test
    void create_success() {

        when(addressRepository.save(address)).thenReturn(address);

        Address saved = addressService.create(address);

        assertEquals("Pune", saved.getCity());
    }


    @Test
    void getAll_success() {

        when(addressRepository.findAll()).thenReturn(List.of(address));

        List<Address> list = addressService.getAll();

        assertFalse(list.isEmpty());
    }

    @Test
    void getAll_empty() {

        when(addressRepository.findAll()).thenReturn(Collections.emptyList());

        assertTrue(addressService.getAll().isEmpty());
    }


    @Test
    void getById_success() {

        when(addressRepository.findById(1)).thenReturn(Optional.of(address));

        Address result = addressService.getById(1);

        assertEquals("Pune", result.getCity());
    }

    @Test
    void getById_notFound() {

        when(addressRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressService.getById(1));
    }


    @Test
    void update_success() {
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));

        when(addressRepository.save(any())).thenReturn(address);

        Address updated = new Address();
        updated.setAddress("New Street");
        updated.setCity("Mumbai");
        updated.setState("MH");
        updated.setZipCode("400001");

        Address result = addressService.update(1, updated);

        assertEquals("Mumbai", result.getCity());
    }

    @Test
    void update_notFound() {

        when(addressRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressService.update(1, address));
    }


    @Test
    void delete_success() {
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));

        when(customerRepository.existsByAddress_AddressId(1)).thenReturn(false);

        addressService.delete(1);

        verify(addressRepository).delete(address);
    }

    @Test
    void delete_notFound() {

        when(addressRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressService.delete(1));
    }



    @Test
    void delete_whenUsed_shouldThrow() {
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));


        when(customerRepository.existsByAddress_AddressId(1)).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> addressService.delete(1));
    }
}