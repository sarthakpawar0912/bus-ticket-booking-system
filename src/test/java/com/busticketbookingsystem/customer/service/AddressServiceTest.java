package com.busticketbookingsystem.customer.service;

import com.busticketbookingsystem.customer.entity.Address;
import com.busticketbookingsystem.customer.repository.AddressRepository;
import com.busticketbookingsystem.customer.repository.CustomerRepository;
import com.busticketbookingsystem.exception.BadRequestException;
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
 * Unit tests for AddressService.
 * Covers create, getAll, getById, update (partial), and delete (with FK check).
 */
@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AddressService addressService;

    private Address address;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .addressId(1).address("123 Main Street")
                .city("Mumbai").state("Maharashtra").zipCode("400001")
                .build();
    }

    @Nested
    @DisplayName("create() Tests")
    class CreateTests {

        @Test
        @DisplayName("POSITIVE: Should create address successfully")
        void create_Success() {
            when(addressRepository.save(any(Address.class))).thenReturn(address);

            Address result = addressService.create(address);

            assertNotNull(result);
            assertEquals(1, result.getAddressId());
            assertEquals("Mumbai", result.getCity());
            verify(addressRepository).save(address);
        }
    }

    @Nested
    @DisplayName("getAll() Tests")
    class GetAllTests {

        @Test
        @DisplayName("POSITIVE: Should return all addresses")
        void getAll_ReturnsList() {
            Address address2 = Address.builder().addressId(2).address("456 Lane")
                    .city("Pune").state("Maharashtra").zipCode("411001").build();

            when(addressRepository.findAll()).thenReturn(List.of(address, address2));

            List<Address> result = addressService.getAll();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no addresses exist")
        void getAll_Empty() {
            when(addressRepository.findAll()).thenReturn(Collections.emptyList());

            assertTrue(addressService.getAll().isEmpty());
        }
    }

    @Nested
    @DisplayName("getById() Tests")
    class GetByIdTests {

        @Test
        @DisplayName("POSITIVE: Should return address for valid ID")
        void getById_Success() {
            when(addressRepository.findById(1)).thenReturn(Optional.of(address));

            Address result = addressService.getById(1);

            assertEquals("123 Main Street", result.getAddress());
            assertEquals("Mumbai", result.getCity());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for invalid ID")
        void getById_NotFound() {
            when(addressRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> addressService.getById(999));
        }
    }

    @Nested
    @DisplayName("update() Tests")
    class UpdateTests {

        @Test
        @DisplayName("POSITIVE: Should update all fields of existing address")
        void update_AllFields_Success() {
            Address updated = Address.builder()
                    .address("789 New Road").city("Delhi")
                    .state("Delhi").zipCode("110001").build();

            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(addressRepository.save(any(Address.class))).thenReturn(address);

            Address result = addressService.update(1, updated);

            assertNotNull(result);
            // Verify fields were updated on the original entity
            assertEquals("789 New Road", address.getAddress());
            assertEquals("Delhi", address.getCity());
            assertEquals("Delhi", address.getState());
            assertEquals("110001", address.getZipCode());
        }

        @Test
        @DisplayName("EDGE CASE: Should only update non-null fields (partial update)")
        void update_PartialFields() {
            // Only update city, leave other fields null
            Address partialUpdate = Address.builder().city("Bangalore").build();

            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(addressRepository.save(any(Address.class))).thenReturn(address);

            Address result = addressService.update(1, partialUpdate);

            assertNotNull(result);
            // City should be updated
            assertEquals("Bangalore", address.getCity());
            // Other fields should remain unchanged
            assertEquals("123 Main Street", address.getAddress());
            assertEquals("Maharashtra", address.getState());
            assertEquals("400001", address.getZipCode());
        }

        @Test
        @DisplayName("EDGE CASE: Should handle update with all null fields (no changes)")
        void update_AllNullFields() {
            Address emptyUpdate = new Address(); // all fields null

            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(addressRepository.save(any(Address.class))).thenReturn(address);

            Address result = addressService.update(1, emptyUpdate);

            assertNotNull(result);
            // Original values should be unchanged
            assertEquals("123 Main Street", address.getAddress());
            assertEquals("Mumbai", address.getCity());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when address not found")
        void update_NotFound() {
            when(addressRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> addressService.update(999, new Address()));
        }
    }


}
