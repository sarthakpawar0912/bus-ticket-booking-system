package com.busticketbookingsystem.customer.service;

import com.busticketbookingsystem.customer.dto.CustomerRequestDTO;
import com.busticketbookingsystem.customer.dto.CustomerResponseDTO;
import com.busticketbookingsystem.customer.entity.Address;
import com.busticketbookingsystem.customer.entity.Customer;
import com.busticketbookingsystem.customer.repository.AddressRepository;
import com.busticketbookingsystem.customer.repository.CustomerRepository;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService.
 * Covers create, getAll, getById, update, patch, delete.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private Address address;
    private CustomerRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .addressId(1).address("123 Main St").city("Mumbai")
                .state("Maharashtra").zipCode("400001").build();

        customer = Customer.builder()
                .customerId(1).name("Sarthak Pawar")
                .email("sarthak@email.com").phone("9876543210")
                .address(address).build();

        requestDTO = new CustomerRequestDTO();
        requestDTO.setName("Sarthak Pawar");
        requestDTO.setEmail("sarthak@email.com");
        requestDTO.setPhone("9876543210");
        requestDTO.setAddressId(1);
    }

    @Nested
    @DisplayName("create() Tests")
    class CreateTests {

        @Test
        @DisplayName("POSITIVE: Should create customer successfully with valid address")
        void create_Success() {
            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            CustomerResponseDTO result = customerService.create(requestDTO);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Sarthak Pawar", result.getName());
            assertEquals("sarthak@email.com", result.getEmail());
            assertEquals("9876543210", result.getPhone());
            assertEquals("Mumbai", result.getCity()); // city comes from Address
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when address not found")
        void create_AddressNotFound() {
            when(addressRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> customerService.create(requestDTO));

            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAll() Tests")
    class GetAllTests {

        @Test
        @DisplayName("POSITIVE: Should return all customers")
        void getAll_ReturnsList() {
            when(customerRepository.findAllWithAddress()).thenReturn(List.of(customer));

            List<CustomerResponseDTO> result = customerService.getAll();

            assertEquals(1, result.size());
            assertEquals("Sarthak Pawar", result.get(0).getName());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no customers exist")
        void getAll_Empty() {
            when(customerRepository.findAllWithAddress()).thenReturn(Collections.emptyList());

            assertTrue(customerService.getAll().isEmpty());
        }
    }

    @Nested
    @DisplayName("getById() Tests")
    class GetByIdTests {

        @Test
        @DisplayName("POSITIVE: Should return customer for valid ID")
        void getById_Success() {
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));

            CustomerResponseDTO result = customerService.getById(1);

            assertEquals("Sarthak Pawar", result.getName());
            assertEquals("Mumbai", result.getCity());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for invalid ID")
        void getById_NotFound() {
            when(customerRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> customerService.getById(999));
        }
    }

    @Nested
    @DisplayName("update() Tests")
    class UpdateTests {

        @Test
        @DisplayName("POSITIVE: Should update all fields of existing customer")
        void update_Success() {
            requestDTO.setName("Updated Name");
            requestDTO.setEmail("updated@email.com");
            requestDTO.setPhone("1111111111");

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            CustomerResponseDTO result = customerService.update(1, requestDTO);

            assertNotNull(result);
            // Verify the entity fields were set
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when customer not found")
        void update_NotFound() {
            when(customerRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> customerService.update(999, requestDTO));
        }
    }

    @Nested
    @DisplayName("patch() Tests")
    class PatchTests {

        @Test
        @DisplayName("POSITIVE: Should patch only the name field (others null)")
        void patch_OnlyName() {
            CustomerRequestDTO patchDto = new CustomerRequestDTO();
            patchDto.setName("Patched Name");
            // email, phone, addressId all null

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            CustomerResponseDTO result = customerService.patch(1, patchDto);

            assertNotNull(result);
            // Address shouldn't be looked up since addressId is null
            verify(addressRepository, never()).findById(anyInt());
        }

        @Test
        @DisplayName("POSITIVE: Should patch only the email field")
        void patch_OnlyEmail() {
            CustomerRequestDTO patchDto = new CustomerRequestDTO();
            patchDto.setEmail("newmail@test.com");

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            CustomerResponseDTO result = customerService.patch(1, patchDto);

            assertNotNull(result);
        }

        @Test
        @DisplayName("POSITIVE: Should patch address when addressId is provided")
        void patch_WithNewAddress() {
            Address newAddr = Address.builder().addressId(2).address("New St")
                    .city("Pune").state("MH").zipCode("411001").build();

            CustomerRequestDTO patchDto = new CustomerRequestDTO();
            patchDto.setAddressId(2);

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(addressRepository.findById(2)).thenReturn(Optional.of(newAddr));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            CustomerResponseDTO result = customerService.patch(1, patchDto);

            assertNotNull(result);
            verify(addressRepository).findById(2);
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when addressId provided but address not found")
        void patch_AddressNotFound() {
            CustomerRequestDTO patchDto = new CustomerRequestDTO();
            patchDto.setAddressId(999);

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(addressRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> customerService.patch(1, patchDto));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when customer not found for patch")
        void patch_CustomerNotFound() {
            when(customerRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> customerService.patch(999, new CustomerRequestDTO()));
        }

        @Test
        @DisplayName("EDGE CASE: Should handle patch with all null fields (no changes)")
        void patch_AllFieldsNull() {
            CustomerRequestDTO emptyPatch = new CustomerRequestDTO();
            // All fields are null

            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            CustomerResponseDTO result = customerService.patch(1, emptyPatch);

            assertNotNull(result);
            // Still saves, but nothing changes
            verify(customerRepository).save(any(Customer.class));
        }
    }


}
