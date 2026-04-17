package com.busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.BusRequestDTO;
import com.busticketbookingsystem.agency.dto.BusResponseDTO;
import com.busticketbookingsystem.agency.entity.AgencyOffice;
import com.busticketbookingsystem.agency.entity.Agency;
import com.busticketbookingsystem.agency.entity.Bus;
import com.busticketbookingsystem.agency.repository.AgencyOfficeRepository;
import com.busticketbookingsystem.agency.repository.BusRepository;
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
 * Unit tests for BusService covering all CRUD operations.
 * Positive, Negative, and Edge cases for each method.
 */
@ExtendWith(MockitoExtension.class)
class BusServiceTest {

    @Mock
    private BusRepository busRepository;
    @Mock
    private AgencyOfficeRepository officeRepository;

    @InjectMocks
    private BusService busService;

    private Bus bus;
    private BusRequestDTO busRequestDTO;
    private AgencyOffice office;

    @BeforeEach
    void setUp() {
        Agency agency = Agency.builder().agencyId(1).name("Test Agency")
                .contactPersonName("Owner").email("test@agency.com").phone("1234567890").build();

        office = AgencyOffice.builder()
                .officeId(1).agency(agency).officeMail("office@test.com")
                .officeContactPersonName("Manager").officeContactNumber("9876543210").build();

        bus = Bus.builder()
                .busId(1).office(office).registrationNumber("MH12AB1234")
                .capacity(40).type("AC Sleeper").build();

        busRequestDTO = new BusRequestDTO();
        busRequestDTO.setOfficeId(1);
        busRequestDTO.setRegistrationNumber("MH12AB1234");
        busRequestDTO.setCapacity(40);
        busRequestDTO.setType("AC Sleeper");
    }

    @Nested
    @DisplayName("addBus() Tests")
    class AddBusTests {

        @Test
        @DisplayName("POSITIVE: Should add bus with unique registration number")
        void addBus_Success() {
            when(busRepository.existsByRegistrationNumber("MH12AB1234")).thenReturn(false);
            when(officeRepository.findById(1)).thenReturn(Optional.of(office));
            when(busRepository.save(any(Bus.class))).thenReturn(bus);

            BusResponseDTO result = busService.addBus(busRequestDTO);

            assertNotNull(result);
            assertEquals(1, result.getBusId());
            assertEquals("MH12AB1234", result.getRegistrationNumber());
            assertEquals(40, result.getCapacity());
            assertEquals("AC Sleeper", result.getType());
            assertEquals(1, result.getOfficeId());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException for duplicate registration number")
        void addBus_DuplicateRegistration_Throws() {
            when(busRepository.existsByRegistrationNumber("MH12AB1234")).thenReturn(true);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> busService.addBus(busRequestDTO));

            assertEquals("A bus with this registration number already exists.", exception.getMessage());
            verify(busRepository, never()).save(any());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException when office not found")
        void addBus_OfficeNotFound_Throws() {
            when(busRepository.existsByRegistrationNumber("MH12AB1234")).thenReturn(false);
            when(officeRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> busService.addBus(busRequestDTO));
        }
    }

    @Nested
    @DisplayName("getAllBuses() Tests")
    class GetAllBusesTests {

        @Test
        @DisplayName("POSITIVE: Should return all buses")
        void getAllBuses_ReturnsList() {
            when(busRepository.findAllWithOffice()).thenReturn(List.of(bus));

            List<BusResponseDTO> result = busService.getAllBuses();

            assertEquals(1, result.size());
            assertEquals("MH12AB1234", result.get(0).getRegistrationNumber());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when no buses exist")
        void getAllBuses_Empty() {
            when(busRepository.findAllWithOffice()).thenReturn(Collections.emptyList());

            assertTrue(busService.getAllBuses().isEmpty());
        }
    }

    @Nested
    @DisplayName("getBusById() Tests")
    class GetBusByIdTests {

        @Test
        @DisplayName("POSITIVE: Should return bus for valid ID")
        void getBusById_Success() {
            when(busRepository.findById(1)).thenReturn(Optional.of(bus));

            BusResponseDTO result = busService.getBusById(1);

            assertEquals(1, result.getBusId());
            assertEquals("AC Sleeper", result.getType());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for invalid ID")
        void getBusById_NotFound() {
            when(busRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> busService.getBusById(999));
        }
    }

    @Nested
    @DisplayName("updateBus() Tests")
    class UpdateBusTests {

        @Test
        @DisplayName("POSITIVE: Should update bus keeping same registration and office")
        void updateBus_SameRegAndOffice_Success() {
            when(busRepository.findById(1)).thenReturn(Optional.of(bus));
            when(busRepository.save(any(Bus.class))).thenReturn(bus);

            BusResponseDTO result = busService.updateBus(1, busRequestDTO);

            assertNotNull(result);
            // No uniqueness checks or office lookups needed
            verify(busRepository, never()).existsByRegistrationNumber(anyString());
        }

        @Test
        @DisplayName("POSITIVE: Should update bus with new unique registration number")
        void updateBus_NewRegistration_Success() {
            busRequestDTO.setRegistrationNumber("MH14XY5678");
            when(busRepository.findById(1)).thenReturn(Optional.of(bus));
            when(busRepository.existsByRegistrationNumber("MH14XY5678")).thenReturn(false);
            when(busRepository.save(any(Bus.class))).thenReturn(bus);

            BusResponseDTO result = busService.updateBus(1, busRequestDTO);

            assertNotNull(result);
            verify(busRepository).existsByRegistrationNumber("MH14XY5678");
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when new registration number is already taken")
        void updateBus_DuplicateRegistration_Throws() {
            busRequestDTO.setRegistrationNumber("TAKEN123");
            when(busRepository.findById(1)).thenReturn(Optional.of(bus));
            when(busRepository.existsByRegistrationNumber("TAKEN123")).thenReturn(true);

            assertThrows(BadRequestException.class, () -> busService.updateBus(1, busRequestDTO));
        }

        @Test
        @DisplayName("POSITIVE: Should move bus to a different office")
        void updateBus_DifferentOffice_Success() {
            AgencyOffice newOffice = AgencyOffice.builder().officeId(2).agency(office.getAgency()).build();
            busRequestDTO.setOfficeId(2);
            when(busRepository.findById(1)).thenReturn(Optional.of(bus));
            when(officeRepository.findById(2)).thenReturn(Optional.of(newOffice));
            when(busRepository.save(any(Bus.class))).thenReturn(bus);

            BusResponseDTO result = busService.updateBus(1, busRequestDTO);

            assertNotNull(result);
            verify(officeRepository).findById(2);
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when new office not found")
        void updateBus_NewOfficeNotFound_Throws() {
            busRequestDTO.setOfficeId(999);
            when(busRepository.findById(1)).thenReturn(Optional.of(bus));
            when(officeRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> busService.updateBus(1, busRequestDTO));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw ResourceNotFoundException for non-existent bus")
        void updateBus_BusNotFound() {
            when(busRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> busService.updateBus(999, busRequestDTO));
        }
    }

    @Nested
    @DisplayName("getBusesByOfficeId() Tests")
    class GetBusesByOfficeIdTests {

        @Test
        @DisplayName("POSITIVE: Should return buses for a given office")
        void getBusesByOfficeId_ReturnsList() {
            when(busRepository.findByOffice_OfficeId(1)).thenReturn(List.of(bus));

            List<BusResponseDTO> result = busService.getBusesByOfficeId(1);

            assertEquals(1, result.size());
            assertEquals("MH12AB1234", result.get(0).getRegistrationNumber());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when office has no buses")
        void getBusesByOfficeId_Empty() {
            when(busRepository.findByOffice_OfficeId(99)).thenReturn(Collections.emptyList());

            assertTrue(busService.getBusesByOfficeId(99).isEmpty());
        }
    }
}
