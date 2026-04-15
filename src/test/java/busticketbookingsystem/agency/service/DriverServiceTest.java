package busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.DriverRequestDTO;
import com.busticketbookingsystem.agency.dto.DriverResponseDTO;
import com.busticketbookingsystem.agency.entity.Agency;
import com.busticketbookingsystem.agency.entity.AgencyOffice;
import com.busticketbookingsystem.agency.entity.Driver;
import com.busticketbookingsystem.agency.repository.AgencyOfficeRepository;
import com.busticketbookingsystem.agency.repository.DriverRepository;
import com.busticketbookingsystem.agency.service.DriverService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DriverService covering all CRUD operations.
 * Tests positive, negative, and edge cases for each method.
 */
@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;
    @Mock
    private AgencyOfficeRepository officeRepository;
    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver driver;
    private DriverRequestDTO driverRequestDTO;
    private AgencyOffice office;
    private Address address;

    @BeforeEach
    void setUp() {
        Agency agency = Agency.builder().agencyId(1).name("Test Agency")
                .contactPersonName("Owner").email("test@agency.com").phone("1234567890").build();

        office = AgencyOffice.builder()
                .officeId(1).agency(agency).officeMail("office@test.com")
                .officeContactPersonName("Manager").officeContactNumber("9876543210").build();

        address = Address.builder()
                .addressId(1).address("123 Driver St").city("Pune")
                .state("Maharashtra").zipCode("411001").build();

        driver = Driver.builder()
                .driverId(1).licenseNumber("DL12345").name("Ramesh Kumar")
                .phone("9998887770").office(office).address(address).build();

        driverRequestDTO = new DriverRequestDTO();
        driverRequestDTO.setLicenseNumber("DL12345");
        driverRequestDTO.setName("Ramesh Kumar");
        driverRequestDTO.setPhone("9998887770");
        driverRequestDTO.setOfficeId(1);
        driverRequestDTO.setAddressId(1);
    }

    @Nested
    @DisplayName("addDriver() Tests")
    class AddDriverTests {

        @Test
        @DisplayName("POSITIVE: Should add driver with unique license and phone")
        void addDriver_Success() {
            when(driverRepository.existsByLicenseNumber("DL12345")).thenReturn(false);
            when(driverRepository.existsByPhone("9998887770")).thenReturn(false);
            when(officeRepository.findById(1)).thenReturn(Optional.of(office));
            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(driverRepository.save(any(Driver.class))).thenReturn(driver);

            DriverResponseDTO result = driverService.addDriver(driverRequestDTO);

            assertNotNull(result);
            assertEquals(1, result.getDriverId());
            assertEquals("DL12345", result.getLicenseNumber());
            assertEquals("Ramesh Kumar", result.getName());
            assertEquals("9998887770", result.getPhone());
            assertEquals(1, result.getOfficeId());
            assertEquals(1, result.getAddressId());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException for duplicate license number")
        void addDriver_DuplicateLicense_Throws() {
            when(driverRepository.existsByLicenseNumber("DL12345")).thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> driverService.addDriver(driverRequestDTO));

            assertEquals("A driver with this license number already exists.", ex.getMessage());
            verify(driverRepository, never()).save(any());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException for duplicate phone number")
        void addDriver_DuplicatePhone_Throws() {
            when(driverRepository.existsByLicenseNumber("DL12345")).thenReturn(false);
            when(driverRepository.existsByPhone("9998887770")).thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> driverService.addDriver(driverRequestDTO));

            assertEquals("A driver with this phone number is already registered.", ex.getMessage());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when office not found")
        void addDriver_OfficeNotFound() {
            when(driverRepository.existsByLicenseNumber("DL12345")).thenReturn(false);
            when(driverRepository.existsByPhone("9998887770")).thenReturn(false);
            when(officeRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> driverService.addDriver(driverRequestDTO));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when address not found")
        void addDriver_AddressNotFound() {
            when(driverRepository.existsByLicenseNumber("DL12345")).thenReturn(false);
            when(driverRepository.existsByPhone("9998887770")).thenReturn(false);
            when(officeRepository.findById(1)).thenReturn(Optional.of(office));
            when(addressRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> driverService.addDriver(driverRequestDTO));
        }
    }

    @Nested
    @DisplayName("getAllDrivers() Tests")
    class GetAllDriversTests {

        @Test
        @DisplayName("POSITIVE: Should return all drivers with details")
        void getAllDrivers_ReturnsList() {
            when(driverRepository.findAllWithDetails()).thenReturn(List.of(driver));

            List<DriverResponseDTO> result = driverService.getAllDrivers();

            assertEquals(1, result.size());
            assertEquals("Ramesh Kumar", result.get(0).getName());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no drivers exist")
        void getAllDrivers_Empty() {
            when(driverRepository.findAllWithDetails()).thenReturn(Collections.emptyList());

            assertTrue(driverService.getAllDrivers().isEmpty());
        }
    }

    @Nested
    @DisplayName("getDriverById() Tests")
    class GetDriverByIdTests {

        @Test
        @DisplayName("POSITIVE: Should return driver for valid ID")
        void getDriverById_Success() {
            when(driverRepository.findById(1)).thenReturn(Optional.of(driver));

            DriverResponseDTO result = driverService.getDriverById(1);

            assertEquals("DL12345", result.getLicenseNumber());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for invalid ID")
        void getDriverById_NotFound() {
            when(driverRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> driverService.getDriverById(999));
        }
    }

    @Nested
    @DisplayName("updateDriver() Tests")
    class UpdateDriverTests {

        @Test
        @DisplayName("POSITIVE: Should update driver keeping same phone, license, and office")
        void updateDriver_NoChanges_Success() {
            when(driverRepository.findById(1)).thenReturn(Optional.of(driver));
            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(driverRepository.save(any(Driver.class))).thenReturn(driver);

            DriverResponseDTO result = driverService.updateDriver(1, driverRequestDTO);

            assertNotNull(result);
            // No duplicate checks since phone/license are the same
            verify(driverRepository, never()).existsByPhone(anyString());
            verify(driverRepository, never()).existsByLicenseNumber(anyString());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when changing phone to an already taken number")
        void updateDriver_DuplicatePhone_Throws() {
            driverRequestDTO.setPhone("1111111111"); // different from current
            when(driverRepository.findById(1)).thenReturn(Optional.of(driver));
            when(driverRepository.existsByPhone("1111111111")).thenReturn(true);

            assertThrows(BadRequestException.class,
                    () -> driverService.updateDriver(1, driverRequestDTO));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when changing license to an already taken number")
        void updateDriver_DuplicateLicense_Throws() {
            driverRequestDTO.setLicenseNumber("TAKEN_LIC");
            when(driverRepository.findById(1)).thenReturn(Optional.of(driver));
            when(driverRepository.existsByLicenseNumber("TAKEN_LIC")).thenReturn(true);

            assertThrows(BadRequestException.class,
                    () -> driverService.updateDriver(1, driverRequestDTO));
        }

        @Test
        @DisplayName("POSITIVE: Should move driver to a different office successfully")
        void updateDriver_DifferentOffice_Success() {
            AgencyOffice newOffice = AgencyOffice.builder().officeId(2)
                    .agency(office.getAgency()).build();
            driverRequestDTO.setOfficeId(2);

            when(driverRepository.findById(1)).thenReturn(Optional.of(driver));
            when(officeRepository.findById(2)).thenReturn(Optional.of(newOffice));
            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(driverRepository.save(any(Driver.class))).thenReturn(driver);

            DriverResponseDTO result = driverService.updateDriver(1, driverRequestDTO);

            assertNotNull(result);
            verify(officeRepository).findById(2);
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when driver not found for update")
        void updateDriver_NotFound() {
            when(driverRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> driverService.updateDriver(999, driverRequestDTO));
        }
    }

    @Nested
    @DisplayName("deleteDriver() Tests")
    class DeleteDriverTests {

        @Test
        @DisplayName("POSITIVE: Should delete driver when exists")
        void deleteDriver_Success() {
            when(driverRepository.existsById(1)).thenReturn(true);

            driverService.deleteDriver(1);

            verify(driverRepository).deleteById(1);
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when driver not found")
        void deleteDriver_NotFound() {
            when(driverRepository.existsById(999)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> driverService.deleteDriver(999));
        }
    }

    @Nested
    @DisplayName("getDriversByOfficeId() Tests")
    class GetDriversByOfficeIdTests {

        @Test
        @DisplayName("POSITIVE: Should return drivers for a given office")
        void getDriversByOfficeId_ReturnsList() {
            when(driverRepository.findByOffice_OfficeId(1)).thenReturn(List.of(driver));

            List<DriverResponseDTO> result = driverService.getDriversByOfficeId(1);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no drivers in office")
        void getDriversByOfficeId_Empty() {
            when(driverRepository.findByOffice_OfficeId(99)).thenReturn(Collections.emptyList());

            assertTrue(driverService.getDriversByOfficeId(99).isEmpty());
        }
    }

    @Test
    @DisplayName("EDGE CASE: mapToDriverResponseDTO should handle null address gracefully")
    void mapToDriverResponseDTO_NullAddress() {
        // Driver with null address
        Driver driverNoAddr = Driver.builder()
                .driverId(2).licenseNumber("DL99999").name("No Addr Driver")
                .phone("5555555555").office(office).address(null).build();

        when(driverRepository.findById(2)).thenReturn(Optional.of(driverNoAddr));

        DriverResponseDTO result = driverService.getDriverById(2);

        assertNotNull(result);
        assertNull(result.getAddressId()); // null address should not cause NPE
    }
}
