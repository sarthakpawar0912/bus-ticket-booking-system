package com.busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.BusDTO;
import com.busticketbookingsystem.agency.entity.Agency;
import com.busticketbookingsystem.agency.entity.AgencyOffice;
import com.busticketbookingsystem.agency.entity.Bus;
import com.busticketbookingsystem.agency.repository.AgencyOfficeRepository;
import com.busticketbookingsystem.agency.repository.BusRepository;
import com.busticketbookingsystem.exception.BadRequestException;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.trip.repository.TripRepository;
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
class BusServiceTest {

    @Mock
    private BusRepository busRepository;

    @Mock
    private AgencyOfficeRepository agencyOfficeRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private BusService busService;

    private AgencyOffice office;
    private Bus bus;
    private BusDTO busDTO;

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

        bus = Bus.builder()
                .busId(1)
                .office(office)
                .registrationNumber("MH12AB1234")
                .capacity(40)
                .type("AC Sleeper")
                .build();

        busDTO = BusDTO.builder()
                .busId(1)
                .officeId(1)
                .registrationNumber("MH12AB1234")
                .capacity(40)
                .type("AC Sleeper")
                .build();
    }

    // ==================== create ====================

    @Test
    void create_success() {
        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
        when(busRepository.save(any(Bus.class))).thenReturn(bus);

        BusDTO result = busService.create(busDTO);

        assertNotNull(result);
        assertEquals("MH12AB1234", result.getRegistrationNumber());
        assertEquals(40, result.getCapacity());
        assertEquals("AC Sleeper", result.getType());
        verify(busRepository, times(1)).save(any(Bus.class));
    }

    @Test
    void create_officeNotFound() {
        when(agencyOfficeRepository.findById(999)).thenReturn(Optional.empty());

        BusDTO dto = BusDTO.builder().officeId(999).registrationNumber("XX").capacity(10).type("AC").build();

        assertThrows(ResourceNotFoundException.class,
                () -> busService.create(dto));

        verify(busRepository, never()).save(any());
    }

    @Test
    void create_withMinCapacity() {
        BusDTO minDto = BusDTO.builder()
                .officeId(1)
                .registrationNumber("MH01XX0001")
                .capacity(1)
                .type("Mini")
                .build();

        Bus minBus = Bus.builder()
                .busId(2)
                .office(office)
                .registrationNumber("MH01XX0001")
                .capacity(1)
                .type("Mini")
                .build();

        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
        when(busRepository.save(any(Bus.class))).thenReturn(minBus);

        BusDTO result = busService.create(minDto);

        assertEquals(1, result.getCapacity());
    }

    // ==================== getAll ====================

    @Test
    void getAll_success() {
        when(busRepository.findAll()).thenReturn(List.of(bus));

        List<BusDTO> result = busService.getAll();

        assertEquals(1, result.size());
        assertEquals("MH12AB1234", result.get(0).getRegistrationNumber());
    }

    @Test
    void getAll_emptyList() {
        when(busRepository.findAll()).thenReturn(Collections.emptyList());

        List<BusDTO> result = busService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_multipleBuses() {
        Bus bus2 = Bus.builder()
                .busId(2)
                .office(office)
                .registrationNumber("MH14CD5678")
                .capacity(50)
                .type("Non-AC Seater")
                .build();
        when(busRepository.findAll()).thenReturn(List.of(bus, bus2));

        List<BusDTO> result = busService.getAll();

        assertEquals(2, result.size());
    }

    // ==================== getById ====================

    @Test
    void getById_success() {
        when(busRepository.findById(1)).thenReturn(Optional.of(bus));

        BusDTO result = busService.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getBusId());
        assertEquals("AC Sleeper", result.getType());
    }

    @Test
    void getById_notFound() {
        when(busRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> busService.getById(999));

        assertEquals("Bus not found with id: 999", exception.getMessage());
    }

    @Test
    void getById_withNullId() {
        when(busRepository.findById(null)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> busService.getById(null));
    }

    // ==================== getByOfficeId ====================

    @Test
    void getByOfficeId_success() {
        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
        when(busRepository.findByOffice_OfficeId(1)).thenReturn(List.of(bus));

        List<BusDTO> result = busService.getByOfficeId(1);

        assertEquals(1, result.size());
    }

    @Test
    void getByOfficeId_officeNotFound() {
        when(agencyOfficeRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> busService.getByOfficeId(999));
    }

    @Test
    void getByOfficeId_noBusesForOffice() {
        when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
        when(busRepository.findByOffice_OfficeId(1)).thenReturn(Collections.emptyList());

        List<BusDTO> result = busService.getByOfficeId(1);

        assertTrue(result.isEmpty());
    }

    // ==================== update ====================

    @Test
    void update_success() {
        BusDTO updateDto = BusDTO.builder()
                .registrationNumber("MH12XX9999")
                .capacity(50)
                .type("Non-AC Seater")
                .build();

        when(busRepository.findById(1)).thenReturn(Optional.of(bus));
        when(busRepository.save(any(Bus.class))).thenReturn(bus);

        BusDTO result = busService.update(1, updateDto);

        assertNotNull(result);
        assertEquals("MH12XX9999", bus.getRegistrationNumber());
        assertEquals(50, bus.getCapacity());
    }

    @Test
    void update_notFound() {
        when(busRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> busService.update(999, busDTO));

        verify(busRepository, never()).save(any());
    }

    @Test
    void update_changeOffice() {
        AgencyOffice newOffice = AgencyOffice.builder()
                .officeId(2)
                .agency(office.getAgency())
                .officeMail("new@office.com")
                .officeContactPersonName("New")
                .officeContactNumber("9999999999")
                .build();

        BusDTO updateDto = BusDTO.builder().officeId(2).build();

        when(busRepository.findById(1)).thenReturn(Optional.of(bus));
        when(agencyOfficeRepository.findById(2)).thenReturn(Optional.of(newOffice));
        when(busRepository.save(any(Bus.class))).thenReturn(bus);

        BusDTO result = busService.update(1, updateDto);

        assertNotNull(result);
        assertEquals(newOffice, bus.getOffice());
    }

    // ==================== delete ====================

    @Test
    void delete_success() {
        when(busRepository.findById(1)).thenReturn(Optional.of(bus));
        when(tripRepository.existsByBus_BusId(1)).thenReturn(false);

        assertDoesNotThrow(() -> busService.delete(1));

        verify(busRepository, times(1)).delete(bus);
    }

    @Test
    void delete_notFound() {
        when(busRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> busService.delete(999));

        verify(busRepository, never()).delete(any());
    }

    @Test
    void delete_busHasTrips() {
        when(busRepository.findById(1)).thenReturn(Optional.of(bus));
        when(tripRepository.existsByBus_BusId(1)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> busService.delete(1));

        assertEquals("Bus has trips assigned. Cannot delete.", exception.getMessage());
        verify(busRepository, never()).delete(any());
    }
}
