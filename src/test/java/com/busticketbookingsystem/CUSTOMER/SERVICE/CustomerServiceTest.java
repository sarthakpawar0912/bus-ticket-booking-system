package com.busticketbookingsystem.CUSTOMER.SERVICE;

import com.busticketbookingsystem.CUSTOMER.DTO.*;
import com.busticketbookingsystem.CUSTOMER.ENTITY.*;
import com.busticketbookingsystem.CUSTOMER.EXCEPTION.ResourceNotFoundException;
import com.busticketbookingsystem.CUSTOMER.REPOSITORY.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock AddressRepository addressRepository;

    @InjectMocks CustomerService customerService;


    CustomerRequestDTO dto;

    Address address;

    @BeforeEach
    void setup() {

        dto = new CustomerRequestDTO();
        dto.setName("Sarthak");
        dto.setEmail("test@gmail.com");
        dto.setPhone("9876543210");
        dto.setAddressId(1);

        address = new Address();
        address.setAddressId(1);
        address.setCity("Pune");
    }



    @Test
    void create_success() {

        when(addressRepository.findById(1)).thenReturn(Optional.of(address));

        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CustomerResponseDTO res = customerService.create(dto);

        assertEquals("Sarthak", res.getName());
    }

    @Test
    void create_addressNotFound() {

        when(addressRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.create(dto));
    }


    @Test
    void getAll_success() {

        Customer c = Customer.builder().customerId(1).name("A").email("a").phone("123").address(address).build();

        when(customerRepository.findAll()).thenReturn(List.of(c));

        List<CustomerResponseDTO> list = customerService.getAll();

        assertFalse(list.isEmpty());
    }

    @Test
    void getAll_empty() {

        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        assertTrue(customerService.getAll().isEmpty());
    }


    @Test
    void getById_success() {

        Customer c = Customer.builder().customerId(1).name("A").email("a").phone("123").address(address).build();

        when(customerRepository.findById(1)).thenReturn(Optional.of(c));

        assertEquals("A", customerService.getById(1).getName());
    }

    @Test
    void getById_notFound() {

        when(customerRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.getById(1));
    }



    @Test
    void update_success() {

        Customer c = Customer.builder().customerId(1).name("Old").email("old").phone("111").address(address).build();

        when(customerRepository.findById(1)).thenReturn(Optional.of(c));

          when(customerRepository.save(any())).thenReturn(c);

        CustomerResponseDTO res = customerService.update(1, dto);

         assertEquals("Sarthak", res.getName());
    }

    @Test
    void update_notFound() {

        when(customerRepository.findById(1)).thenReturn(Optional.empty());


         assertThrows(ResourceNotFoundException.class,
                () -> customerService.update(1, dto));
    }


    @Test
    void delete_success() {

        when(customerRepository.existsById(1)).thenReturn(true);

        customerService.delete(1);

        verify(customerRepository).deleteById(1);

    }

    @Test
    void delete_notFound() {

        when(customerRepository.existsById(1)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.delete(1));

    }
}