package com.busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.AgencyRequestDTO;
import com.busticketbookingsystem.agency.dto.AgencyResponseDTO;
import com.busticketbookingsystem.agency.dto.OfficeRequestDTO;
import com.busticketbookingsystem.agency.dto.OfficeResponseDTO;
import com.busticketbookingsystem.agency.entity.Agency;
import com.busticketbookingsystem.agency.entity.AgencyOffice;
import com.busticketbookingsystem.agency.repository.AgencyOfficeRepository;
import com.busticketbookingsystem.agency.repository.AgencyRepository;
import com.busticketbookingsystem.agency.repository.BusRepository;
import com.busticketbookingsystem.agency.repository.DriverRepository;
import com.busticketbookingsystem.customer.entity.Address;
import com.busticketbookingsystem.customer.repository.AddressRepository;
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
 * Comprehensive unit tests for AgencyService.
 * Covers Agency CRUD + Office CRUD methods with positive, negative, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class AgencyServiceTest {

    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private AgencyOfficeRepository officeRepository;
    @Mock
    private BusRepository busRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AgencyService agencyService;

    // Reusable test data
    private Agency agency;
    private AgencyRequestDTO agencyRequestDTO;
    private AgencyOffice office;
    private OfficeRequestDTO officeRequestDTO;
    private Address address;

    @BeforeEach
    void setUp() {
        // Build a sample Agency entity
        agency = Agency.builder()
                .agencyId(1)
                .name("City Travels")
                .contactPersonName("John Doe")
                .email("city@travels.com")
                .phone("9876543210")
                .build();

        // Build a sample request DTO for creating/updating agencies
        agencyRequestDTO = new AgencyRequestDTO();
        agencyRequestDTO.setName("City Travels");
        agencyRequestDTO.setContactPersonName("John Doe");
        agencyRequestDTO.setEmail("city@travels.com");
        agencyRequestDTO.setPhone("9876543210");

        // Build a sample Address (used by offices)
        address = Address.builder()
                .addressId(1)
                .address("123 Main St")
                .city("Mumbai")
                .state("Maharashtra")
                .zipCode("400001")
                .build();

        // Build a sample AgencyOffice entity
        office = AgencyOffice.builder()
                .officeId(1)
                .agency(agency)
                .officeMail("office@city.com")
                .officeContactPersonName("Jane Doe")
                .officeContactNumber("9876543211")
                .officeAddress(address)
                .build();

        // Build a sample request DTO for creating/updating offices
        officeRequestDTO = new OfficeRequestDTO();
        officeRequestDTO.setAgencyId(1);
        officeRequestDTO.setOfficeMail("office@city.com");
        officeRequestDTO.setOfficeContactPersonName("Jane Doe");
        officeRequestDTO.setOfficeContactNumber("9876543211");
        officeRequestDTO.setOfficeAddressId(1);
    }

    // ========================================================
    // AGENCY CRUD TESTS
    // ========================================================

    @Nested
    @DisplayName("createAgency() Tests")
    class CreateAgencyTests {

        @Test
        @DisplayName("POSITIVE: Should create agency successfully when email is unique")
        void createAgency_Success() {
            // Arrange: email does not exist yet
            when(agencyRepository.existsByEmail("city@travels.com")).thenReturn(false);
            when(agencyRepository.save(any(Agency.class))).thenReturn(agency);

            // Act
            AgencyResponseDTO result = agencyService.createAgency(agencyRequestDTO);

            // Assert: verify all fields are mapped correctly
            assertNotNull(result);
            assertEquals(1, result.getAgencyId());
            assertEquals("City Travels", result.getName());
            assertEquals("John Doe", result.getContactPersonName());
            assertEquals("city@travels.com", result.getEmail());
            assertEquals("9876543210", result.getPhone());

            // Verify repository interactions
            verify(agencyRepository).existsByEmail("city@travels.com");
            verify(agencyRepository).save(any(Agency.class));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException when email already exists")
        void createAgency_DuplicateEmail_ThrowsBadRequest() {
            // Arrange: email already registered
            when(agencyRepository.existsByEmail("city@travels.com")).thenReturn(true);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> agencyService.createAgency(agencyRequestDTO));

            assertEquals("Email already registered to another agency.", exception.getMessage());

            // Verify save was never called since validation failed
            verify(agencyRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllAgencies() Tests")
    class GetAllAgenciesTests {

        @Test
        @DisplayName("POSITIVE: Should return list of all agencies")
        void getAllAgencies_ReturnsList() {
            // Arrange
            Agency agency2 = Agency.builder()
                    .agencyId(2).name("Metro Bus").contactPersonName("Alice")
                    .email("metro@bus.com").phone("1234567890").build();
            when(agencyRepository.findAll()).thenReturn(List.of(agency, agency2));

            // Act
            List<AgencyResponseDTO> result = agencyService.getAllAgencies();

            // Assert
            assertEquals(2, result.size());
            assertEquals("City Travels", result.get(0).getName());
            assertEquals("Metro Bus", result.get(1).getName());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no agencies exist")
        void getAllAgencies_EmptyList() {
            when(agencyRepository.findAll()).thenReturn(Collections.emptyList());

            List<AgencyResponseDTO> result = agencyService.getAllAgencies();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getAgencyById() Tests")
    class GetAgencyByIdTests {

        @Test
        @DisplayName("POSITIVE: Should return agency when valid ID is given")
        void getAgencyById_Success() {
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));

            AgencyResponseDTO result = agencyService.getAgencyById(1);

            assertNotNull(result);
            assertEquals("City Travels", result.getName());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for non-existent ID")
        void getAgencyById_NotFound() {
            when(agencyRepository.findById(999)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> agencyService.getAgencyById(999));

            assertEquals("Agency not found with ID: 999", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("updateAgency() Tests")
    class UpdateAgencyTests {

        @Test
        @DisplayName("POSITIVE: Should update agency when keeping same email and phone")
        void updateAgency_SameEmailAndPhone_Success() {
            // Arrange: updating name/contact but keeping same email and phone
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
            when(agencyRepository.save(any(Agency.class))).thenReturn(agency);

            // Act
            AgencyResponseDTO result = agencyService.updateAgency(1, agencyRequestDTO);

            // Assert
            assertNotNull(result);
            verify(agencyRepository, never()).existsByEmail(anyString()); // same email, no check needed
            verify(agencyRepository, never()).existsByPhone(anyString());
        }

        @Test
        @DisplayName("POSITIVE: Should update agency with new unique email")
        void updateAgency_NewUniqueEmail_Success() {
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));

            // Change email to a new one that doesn't exist
            agencyRequestDTO.setEmail("newemail@travels.com");
            when(agencyRepository.existsByEmail("newemail@travels.com")).thenReturn(false);
            when(agencyRepository.save(any(Agency.class))).thenReturn(agency);

            AgencyResponseDTO result = agencyService.updateAgency(1, agencyRequestDTO);

            assertNotNull(result);
            verify(agencyRepository).existsByEmail("newemail@travels.com");
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException when new email is already taken")
        void updateAgency_DuplicateEmail_ThrowsBadRequest() {
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
            agencyRequestDTO.setEmail("taken@email.com");
            when(agencyRepository.existsByEmail("taken@email.com")).thenReturn(true);

            assertThrows(BadRequestException.class,
                    () -> agencyService.updateAgency(1, agencyRequestDTO));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException when new phone is already taken")
        void updateAgency_DuplicatePhone_ThrowsBadRequest() {
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
            // Keep email same so it passes email check
            agencyRequestDTO.setPhone("1111111111");
            when(agencyRepository.existsByPhone("1111111111")).thenReturn(true);

            assertThrows(BadRequestException.class,
                    () -> agencyService.updateAgency(1, agencyRequestDTO));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for non-existent agency")
        void updateAgency_NotFound() {
            when(agencyRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> agencyService.updateAgency(999, agencyRequestDTO));
        }
    }

    @Nested
    @DisplayName("deleteAgency() Tests")
    class DeleteAgencyTests {

        @Test
        @DisplayName("POSITIVE: Should delete agency when no offices exist")
        void deleteAgency_Success() {
            // Arrange: agency has no offices
            when(officeRepository.existsByAgency_AgencyId(1)).thenReturn(false);

            // Act
            agencyService.deleteAgency(1);

            // Assert: deleteById was called
            verify(agencyRepository).deleteById(1);
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException when agency still has offices")
        void deleteAgency_HasOffices_ThrowsBadRequest() {
            when(officeRepository.existsByAgency_AgencyId(1)).thenReturn(true);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> agencyService.deleteAgency(1));

            assertEquals("Cannot delete Agency. Remove all Offices first.", exception.getMessage());
            verify(agencyRepository, never()).deleteById(anyInt());
        }
    }

    // ========================================================
    // OFFICE CRUD TESTS (inside AgencyService)
    // ========================================================

    @Nested
    @DisplayName("createOffice() Tests")
    class CreateOfficeTests {

        @Test
        @DisplayName("POSITIVE: Should create office successfully with valid agency and address")
        void createOffice_Success() {
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(officeRepository.save(any(AgencyOffice.class))).thenReturn(office);

            OfficeResponseDTO result = agencyService.createOffice(officeRequestDTO);

            assertNotNull(result);
            assertEquals(1, result.getOfficeId());
            assertEquals(1, result.getAgencyId());
            assertEquals("office@city.com", result.getOfficeMail());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when parent agency not found")
        void createOffice_AgencyNotFound() {
            when(agencyRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> agencyService.createOffice(officeRequestDTO));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when address not found")
        void createOffice_AddressNotFound() {
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
            when(addressRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> agencyService.createOffice(officeRequestDTO));
        }
    }

    @Nested
    @DisplayName("getAllOffices() Tests")
    class GetAllOfficesTests {

        @Test
        @DisplayName("POSITIVE: Should return all offices with details")
        void getAllOffices_ReturnsList() {
            when(officeRepository.findAllWithDetails()).thenReturn(List.of(office));

            List<OfficeResponseDTO> result = agencyService.getAllOffices();

            assertEquals(1, result.size());
            assertEquals("office@city.com", result.get(0).getOfficeMail());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no offices exist")
        void getAllOffices_EmptyList() {
            when(officeRepository.findAllWithDetails()).thenReturn(Collections.emptyList());

            assertTrue(agencyService.getAllOffices().isEmpty());
        }
    }

    @Nested
    @DisplayName("getOfficeById() Tests")
    class GetOfficeByIdTests {

        @Test
        @DisplayName("POSITIVE: Should return office for valid ID")
        void getOfficeById_Success() {
            when(officeRepository.findById(1)).thenReturn(Optional.of(office));

            OfficeResponseDTO result = agencyService.getOfficeById(1);

            assertNotNull(result);
            assertEquals(1, result.getOfficeId());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for invalid ID")
        void getOfficeById_NotFound() {
            when(officeRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> agencyService.getOfficeById(999));
        }
    }

    @Nested
    @DisplayName("updateOffice() Tests")
    class UpdateOfficeTests {

        @Test
        @DisplayName("POSITIVE: Should update office keeping same agency")
        void updateOffice_SameAgency_Success() {
            when(officeRepository.findById(1)).thenReturn(Optional.of(office));
            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(officeRepository.save(any(AgencyOffice.class))).thenReturn(office);

            OfficeResponseDTO result = agencyService.updateOffice(1, officeRequestDTO);

            assertNotNull(result);
            // Agency findById should NOT be called since agency didn't change
            verify(agencyRepository, never()).findById(anyInt());
        }

        @Test
        @DisplayName("POSITIVE: Should update office and move to a different agency")
        void updateOffice_DifferentAgency_Success() {
            Agency newAgency = Agency.builder().agencyId(2).name("New Agency")
                    .contactPersonName("Bob").email("bob@new.com").phone("1112223333").build();

            officeRequestDTO.setAgencyId(2); // moving to different agency
            when(officeRepository.findById(1)).thenReturn(Optional.of(office));
            when(agencyRepository.findById(2)).thenReturn(Optional.of(newAgency));
            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(officeRepository.save(any(AgencyOffice.class))).thenReturn(office);

            OfficeResponseDTO result = agencyService.updateOffice(1, officeRequestDTO);

            assertNotNull(result);
            verify(agencyRepository).findById(2);
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when new parent agency not found")
        void updateOffice_NewAgencyNotFound() {
            officeRequestDTO.setAgencyId(999);
            when(officeRepository.findById(1)).thenReturn(Optional.of(office));
            when(agencyRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> agencyService.updateOffice(1, officeRequestDTO));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when address not found during update")
        void updateOffice_AddressNotFound() {
            officeRequestDTO.setOfficeAddressId(999);
            when(officeRepository.findById(1)).thenReturn(Optional.of(office));
            when(addressRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> agencyService.updateOffice(1, officeRequestDTO));
        }

        @Test
        @DisplayName("EDGE CASE: Should skip address update when addressId is null")
        void updateOffice_NullAddressId_SkipsAddressUpdate() {
            officeRequestDTO.setOfficeAddressId(null);
            when(officeRepository.findById(1)).thenReturn(Optional.of(office));
            when(officeRepository.save(any(AgencyOffice.class))).thenReturn(office);

            OfficeResponseDTO result = agencyService.updateOffice(1, officeRequestDTO);

            assertNotNull(result);
            // addressRepository.findById should NOT be called
            verify(addressRepository, never()).findById(anyInt());
        }
    }

    @Nested
    @DisplayName("deleteOffice() Tests")
    class DeleteOfficeTests {

        @Test
        @DisplayName("POSITIVE: Should delete office when no buses or drivers linked")
        void deleteOffice_Success() {
            when(busRepository.existsByOffice_OfficeId(1)).thenReturn(false);
            when(driverRepository.existsByOffice_OfficeId(1)).thenReturn(false);

            agencyService.deleteOffice(1);

            verify(officeRepository).deleteById(1);
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException when buses are linked to office")
        void deleteOffice_HasBuses_ThrowsBadRequest() {
            when(busRepository.existsByOffice_OfficeId(1)).thenReturn(true);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> agencyService.deleteOffice(1));

            assertEquals("Cannot delete Office. Move or delete registered Buses first.",
                    exception.getMessage());
            verify(officeRepository, never()).deleteById(anyInt());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException when drivers are linked to office")
        void deleteOffice_HasDrivers_ThrowsBadRequest() {
            // No buses, but drivers exist
            when(busRepository.existsByOffice_OfficeId(1)).thenReturn(false);
            when(driverRepository.existsByOffice_OfficeId(1)).thenReturn(true);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> agencyService.deleteOffice(1));

            assertEquals("Cannot delete Office. Reassign active Drivers first.",
                    exception.getMessage());
            verify(officeRepository, never()).deleteById(anyInt());
        }
    }

    // ========================================================
    // EDGE CASE: Mapping helper with null officeAddress
    // ========================================================

    @Test
    @DisplayName("EDGE CASE: mapToOfficeResponseDTO should handle null officeAddress gracefully")
    void mapToOfficeResponseDTO_NullAddress() {
        // Create office with no address
        AgencyOffice officeNoAddr = AgencyOffice.builder()
                .officeId(2).agency(agency).officeMail("no-addr@test.com")
                .officeContactPersonName("Test").officeContactNumber("1234567890")
                .officeAddress(null) // null address
                .build();

        when(officeRepository.findById(2)).thenReturn(Optional.of(officeNoAddr));

        OfficeResponseDTO result = agencyService.getOfficeById(2);

        assertNotNull(result);
        assertNull(result.getOfficeAddressId()); // should be null, not crash
    }
}
