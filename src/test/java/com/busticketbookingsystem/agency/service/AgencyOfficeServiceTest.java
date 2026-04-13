package com.busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.AgencyOfficeDTO;
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
class AgencyOfficeServiceTest {

    @Mock
    private AgencyOfficeRepository agencyOfficeRepository;

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private BusRepository busRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private AgencyOfficeService agencyOfficeService;

    private Agency agency;
    private Address address;
    private AgencyOffice office;
    private AgencyOfficeDTO officeDTO;

    @BeforeEach
    void setUp() {
        agency = Agency.builder()
                .agencyId(1)
                .name("Express Travels")
                .contactPersonName("Rahul")
                .email("express@travels.com")
                .phone("9876543210")
                .build();

        address = Address.builder()
                .addressId(1)
                .address("123 Main Street")
                .city("Pune")
                .state("Maharashtra")
                .zipCode("411001")
                .build();

        office = AgencyOffice.builder()
                .officeId(1)
                .agency(agency)
                .officeMail("office@express.com")
                .officeContactPersonName("Amit")
                .officeContactNumber("1234567890")
                .officeAddress(address)
                .build();

        officeDTO = AgencyOfficeDTO.builder()
                .officeId(1)
                .agencyId(1)
                .officeMail("office@express.com")
                .officeContactPersonName("Amit")
                .officeContactNumber("1234567890")
                .officeAddressId(1)
                .build();
    }

    // ==================== create ====================

    @Test
    void create_success() {
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));
        when(agencyOfficeRepository.save(any(AgencyOffice.class))).thenReturn(office);

        AgencyOfficeDTO result = agencyOfficeService.create(officeDTO);

        assertNotNull(result);
        assertEquals("office@express.com", result.getOfficeMail());
        assertEquals("Express Travels", result.getAgencyName());
        verify(agencyOfficeRepository, times(1)).save(any(AgencyOffice.class));
    }

    @Test
    void create_agencyNotFound() {
        when(agencyRepository.findById(999)).thenReturn(Optional.empty());

        AgencyOfficeDTO dto = AgencyOfficeDTO.builder().agencyId(999).officeAddressId(1).build();

        assertThrows(ResourceNotFoundException.class,
                () -> agencyOfficeService.create(dto));

        verify(agencyOfficeRepository, never()).save(any());
    }

    @Test
    void create_addressNotFound() {
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(addressRepository.findById(999)).thenReturn(Optional.empty());

        AgencyOfficeDTO dto = AgencyOfficeDTO.builder().agencyId(1).officeAddressId(999).build();

        assertThrows(ResourceNotFoundException.class,
                () -> agencyOfficeService.create(dto));

        verify(agencyOfficeRepository, never()).save(any());
    }

    // ==================== getAll ====================

    @Test
    void getAll_success() {
        when(agencyOfficeRepository.findAll()).thenReturn(List.of(office));

        List<AgencyOfficeDTO> result = agencyOfficeService.getAll();

        assertEquals(1, result.size());
        assertEquals("Amit", result.get(0).getOfficeContactPersonName());
    }

    @Test
    void getAll_emptyList() {
        when(agencyOfficeRepository.findAll()).thenReturn(Collections.emptyList());

        List<AgencyOfficeDTO> result = agencyOfficeService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_multipleOffices() {
        AgencyOffice office2 = AgencyOffice.builder()
                .officeId(2)
                .agency(agency)
                .officeMail("office2@express.com")
                .officeContactPersonName("Ravi")
                .officeContactNumber("9999999999")
                .officeAddress(address)
                .build();
        when(agencyOfficeRepository.findAll()).thenReturn(List.of(office, office2));

        List<AgencyOfficeDTO> result = agencyOfficeService.getAll();

        assertEquals(2, result.size());
    }

    // ==================== getById ====================

    @Test
    void getById_success() {
        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));

        AgencyOfficeDTO result = agencyOfficeService.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getOfficeId());
        assertEquals("Pune", result.getOfficeCity());
    }

    @Test
    void getById_notFound() {
        when(agencyOfficeRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> agencyOfficeService.getById(999));
    }

    @Test
    void getById_officeWithNullAddress() {
        AgencyOffice officeNoAddr = AgencyOffice.builder()
                .officeId(3)
                .agency(agency)
                .officeMail("test@test.com")
                .officeContactPersonName("Test")
                .officeContactNumber("0000000000")
                .officeAddress(null)
                .build();
        when(agencyOfficeRepository.findById(3)).thenReturn(Optional.of(officeNoAddr));

        AgencyOfficeDTO result = agencyOfficeService.getById(3);

        assertNotNull(result);
        assertNull(result.getOfficeAddressId());
        assertNull(result.getOfficeCity());
    }

    // ==================== getByAgencyId ====================

    @Test
    void getByAgencyId_success() {
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(agencyOfficeRepository.findByAgency_AgencyId(1)).thenReturn(List.of(office));

        List<AgencyOfficeDTO> result = agencyOfficeService.getByAgencyId(1);

        assertEquals(1, result.size());
    }

    @Test
    void getByAgencyId_agencyNotFound() {
        when(agencyRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> agencyOfficeService.getByAgencyId(999));
    }

    @Test
    void getByAgencyId_noOfficesForAgency() {
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(agencyOfficeRepository.findByAgency_AgencyId(1)).thenReturn(Collections.emptyList());

        List<AgencyOfficeDTO> result = agencyOfficeService.getByAgencyId(1);

        assertTrue(result.isEmpty());
    }

    // ==================== update ====================

    @Test
    void update_success() {
        AgencyOfficeDTO updateDto = AgencyOfficeDTO.builder()
                .officeMail("updated@express.com")
                .officeContactPersonName("Updated Person")
                .officeContactNumber("5555555555")
                .build();

        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
        when(agencyOfficeRepository.save(any(AgencyOffice.class))).thenReturn(office);

        AgencyOfficeDTO result = agencyOfficeService.update(1, updateDto);

        assertNotNull(result);
        assertEquals("updated@express.com", office.getOfficeMail());
        verify(agencyOfficeRepository, times(1)).save(office);
    }

    @Test
    void update_notFound() {
        when(agencyOfficeRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> agencyOfficeService.update(999, officeDTO));

        verify(agencyOfficeRepository, never()).save(any());
    }

    @Test
    void update_changeAgencyAndAddress() {
        Agency newAgency = Agency.builder().agencyId(2).name("New Agency").contactPersonName("X").email("x@x.com").phone("1111111111").build();
        Address newAddress = Address.builder().addressId(2).address("New Addr").city("Mumbai").state("MH").zipCode("400001").build();

        AgencyOfficeDTO updateDto = AgencyOfficeDTO.builder()
                .agencyId(2)
                .officeAddressId(2)
                .build();

        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
        when(agencyRepository.findById(2)).thenReturn(Optional.of(newAgency));
        when(addressRepository.findById(2)).thenReturn(Optional.of(newAddress));
        when(agencyOfficeRepository.save(any(AgencyOffice.class))).thenReturn(office);

        AgencyOfficeDTO result = agencyOfficeService.update(1, updateDto);

        assertNotNull(result);
        assertEquals(newAgency, office.getAgency());
        assertEquals(newAddress, office.getOfficeAddress());
    }

    // ==================== delete ====================

    @Test
    void delete_success() {
        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
        when(busRepository.existsByOffice_OfficeId(1)).thenReturn(false);

        assertDoesNotThrow(() -> agencyOfficeService.delete(1));

        verify(agencyOfficeRepository, times(1)).delete(office);
    }

    @Test
    void delete_notFound() {
        when(agencyOfficeRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> agencyOfficeService.delete(999));

        verify(agencyOfficeRepository, never()).delete(any());
    }

    @Test
    void delete_officeHasBuses() {
        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
        when(busRepository.existsByOffice_OfficeId(1)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> agencyOfficeService.delete(1));

        assertEquals("Office has buses assigned. Cannot delete.", exception.getMessage());
        verify(agencyOfficeRepository, never()).delete(any());
    }
}
