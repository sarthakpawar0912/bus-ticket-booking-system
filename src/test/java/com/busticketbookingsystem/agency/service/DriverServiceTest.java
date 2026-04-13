package com.busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.DriverDTO;
import com.busticketbookingsystem.agency.entity.Agency;
import com.busticketbookingsystem.agency.entity.AgencyOffice;
import com.busticketbookingsystem.agency.entity.Driver;
import com.busticketbookingsystem.agency.repository.AgencyOfficeRepository;
import com.busticketbookingsystem.agency.repository.DriverRepository;
import com.busticketbookingsystem.customer.entity.Address;
import com.busticketbookingsystem.customer.repository.AddressRepository;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private AgencyOfficeRepository agencyOfficeRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private DriverService driverService;

    private AgencyOffice office;
    private Address address;
    private Driver driver;
    private DriverDTO driverDTO;

    @BeforeEach
    void setUp() {
        Agency agency = Agency.builder()
                .agencyId(1)
                .name("Express Travels")
                .contactPersonName("Rahul")
                .email("express@travels.com")
                .phone("9876543210")
                .build();

        office = AgencyOffice.builder()
                .officeId(1)
                .agency(agency)
                .officeMail("office@express.com")
                .officeContactPersonName("Amit")
                .officeContactNumber("1234567890")
                .build();

        address = Address.builder()
                .addressId(1)
                .address("123 Main Street")
                .city("Pune")
                .state("Maharashtra")
                .zipCode("411001")
                .build();

        driver = Driver.builder()
                .driverId(1)
                .licenseNumber("MH12-2024-001234")
                .name("Raju Kumar")
                .phone("9876543210")
                .office(office)
                .address(address)
                .build();

        driverDTO = DriverDTO.builder()
                .driverId(1)
                .licenseNumber("MH12-2024-001234")
                .name("Raju Kumar")
                .phone("9876543210")
                .officeId(1)
                .addressId(1)
                .build();
    }

    // ==================== create ====================

    @Test
    void create_success() {
        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverDTO result = driverService.create(driverDTO);

        assertNotNull(result);
        assertEquals("Raju Kumar", result.getName());
        assertEquals("MH12-2024-001234", result.getLicenseNumber());
        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    void create_officeNotFound() {
        when(agencyOfficeRepository.findById(999)).thenReturn(Optional.empty());

        DriverDTO dto = DriverDTO.builder()
                .licenseNumber("XX")
                .name("Test")
                .phone("1234567890")
                .officeId(999)
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> driverService.create(dto));

        verify(driverRepository, never()).save(any());
    }

    @Test
    void create_withoutOfficeAndAddress() {
        DriverDTO dtoNoRefs = DriverDTO.builder()
                .licenseNumber("MH01-2024-000001")
                .name("Solo Driver")
                .phone("5555555555")
                .officeId(null)
                .addressId(null)
                .build();

        Driver driverNoRefs = Driver.builder()
                .driverId(2)
                .licenseNumber("MH01-2024-000001")
                .name("Solo Driver")
                .phone("5555555555")
                .office(null)
                .address(null)
                .build();

        when(driverRepository.save(any(Driver.class))).thenReturn(driverNoRefs);

        DriverDTO result = driverService.create(dtoNoRefs);

        assertNotNull(result);
        assertNull(result.getOfficeId());
        assertNull(result.getAddressId());
    }

    // ==================== getAll ====================

    @Test
    void getAll_success() {
        when(driverRepository.findAll()).thenReturn(List.of(driver));

        List<DriverDTO> result = driverService.getAll();

        assertEquals(1, result.size());
        assertEquals("Raju Kumar", result.get(0).getName());
    }

    @Test
    void getAll_emptyList() {
        when(driverRepository.findAll()).thenReturn(Collections.emptyList());

        List<DriverDTO> result = driverService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_driverWithNullOffice() {
        Driver driverNoOffice = Driver.builder()
                .driverId(2)
                .licenseNumber("XX")
                .name("No Office")
                .phone("0000000000")
                .office(null)
                .address(null)
                .build();
        when(driverRepository.findAll()).thenReturn(List.of(driverNoOffice));

        List<DriverDTO> result = driverService.getAll();

        assertEquals(1, result.size());
        assertNull(result.get(0).getOfficeId());
        assertNull(result.get(0).getAddressId());
    }

    // ==================== getById ====================

    @Test
    void getById_success() {
        when(driverRepository.findById(1)).thenReturn(Optional.of(driver));

        DriverDTO result = driverService.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getDriverId());
        assertEquals("Raju Kumar", result.getName());
    }

    @Test
    void getById_notFound() {
        when(driverRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> driverService.getById(999));

        assertEquals("Driver not found with id: 999", exception.getMessage());
    }

    @Test
    void getById_withNullId() {
        when(driverRepository.findById(null)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> driverService.getById(null));
    }

    // ==================== getByOfficeId ====================

    @Test
    void getByOfficeId_success() {
        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
        when(driverRepository.findByOffice_OfficeId(1)).thenReturn(List.of(driver));

        List<DriverDTO> result = driverService.getByOfficeId(1);

        assertEquals(1, result.size());
    }

    @Test
    void getByOfficeId_officeNotFound() {
        when(agencyOfficeRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> driverService.getByOfficeId(999));
    }

    @Test
    void getByOfficeId_noDriversForOffice() {
        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
        when(driverRepository.findByOffice_OfficeId(1)).thenReturn(Collections.emptyList());

        List<DriverDTO> result = driverService.getByOfficeId(1);

        assertTrue(result.isEmpty());
    }

    // ==================== update ====================

    @Test
    void update_success() {
        DriverDTO updateDto = DriverDTO.builder()
                .licenseNumber("MH12-2025-999999")
                .name("Updated Name")
                .phone("1111111111")
                .build();

        when(driverRepository.findById(1)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverDTO result = driverService.update(1, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", driver.getName());
        assertEquals("MH12-2025-999999", driver.getLicenseNumber());
    }

    @Test
    void update_notFound() {
        when(driverRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> driverService.update(999, driverDTO));

        verify(driverRepository, never()).save(any());
    }

    @Test
    void update_changeOfficeAndAddress() {
        AgencyOffice newOffice = AgencyOffice.builder().officeId(2).agency(office.getAgency()).officeMail("n@n.com").officeContactPersonName("N").officeContactNumber("9999999999").build();
        Address newAddress = Address.builder().addressId(2).address("New").city("Mumbai").state("MH").zipCode("400001").build();

        DriverDTO updateDto = DriverDTO.builder()
                .officeId(2)
                .addressId(2)
                .build();

        when(driverRepository.findById(1)).thenReturn(Optional.of(driver));
        when(agencyOfficeRepository.findById(2)).thenReturn(Optional.of(newOffice));
        when(addressRepository.findById(2)).thenReturn(Optional.of(newAddress));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverDTO result = driverService.update(1, updateDto);

        assertNotNull(result);
        assertEquals(newOffice, driver.getOffice());
        assertEquals(newAddress, driver.getAddress());
    }

    // ==================== delete ====================

    @Test
    void delete_success() {
        when(driverRepository.findById(1)).thenReturn(Optional.of(driver));

        assertDoesNotThrow(() -> driverService.delete(1));

        verify(driverRepository, times(1)).delete(driver);
    }

    @Test
    void delete_notFound() {
        when(driverRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> driverService.delete(999));

        verify(driverRepository, never()).delete(any());
    }

    @Test
    void delete_verifyInteractions() {
        when(driverRepository.findById(1)).thenReturn(Optional.of(driver));

        driverService.delete(1);

        verify(driverRepository).findById(1);
        verify(driverRepository).delete(driver);
        verifyNoMoreInteractions(driverRepository);
    }
}
