package com.busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.AgencyDTO;
import com.busticketbookingsystem.agency.entity.Agency;
import com.busticketbookingsystem.agency.entity.AgencyOffice;
import com.busticketbookingsystem.agency.repository.AgencyOfficeRepository;
import com.busticketbookingsystem.agency.repository.AgencyRepository;
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
class AgencyServiceTest {

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private AgencyOfficeRepository agencyOfficeRepository;

    @InjectMocks
    private AgencyService agencyService;

    private Agency agency;
    private AgencyDTO agencyDTO;

    @BeforeEach
    void setUp() {
        agency = Agency.builder()
                .agencyId(1)
                .name("Express Travels")
                .contactPersonName("Rahul Sharma")
                .email("express@travels.com")
                .phone("9876543210")
                .build();

        agencyDTO = AgencyDTO.builder()
                .agencyId(1)
                .name("Express Travels")
                .contactPersonName("Rahul Sharma")
                .email("express@travels.com")
                .phone("9876543210")
                .build();
    }

    // ==================== create ====================

    @Test
    void create_success() {
        when(agencyRepository.save(any(Agency.class))).thenReturn(agency);

        AgencyDTO result = agencyService.create(agencyDTO);

        assertNotNull(result);
        assertEquals("Express Travels", result.getName());
        assertEquals("Rahul Sharma", result.getContactPersonName());
        verify(agencyRepository, times(1)).save(any(Agency.class));
    }

    @Test
    void create_withNullFields() {
        AgencyDTO nullDto = AgencyDTO.builder()
                .name(null)
                .contactPersonName(null)
                .email(null)
                .phone(null)
                .build();

        Agency nullAgency = Agency.builder().agencyId(2).build();
        when(agencyRepository.save(any(Agency.class))).thenReturn(nullAgency);

        AgencyDTO result = agencyService.create(nullDto);

        assertNotNull(result);
        assertNull(result.getName());
        verify(agencyRepository, times(1)).save(any(Agency.class));
    }

    @Test
    void create_withEmptyName() {
        AgencyDTO emptyDto = AgencyDTO.builder()
                .name("")
                .contactPersonName("Test")
                .email("test@test.com")
                .phone("1234567890")
                .build();

        Agency emptyAgency = Agency.builder()
                .agencyId(3)
                .name("")
                .contactPersonName("Test")
                .email("test@test.com")
                .phone("1234567890")
                .build();
        when(agencyRepository.save(any(Agency.class))).thenReturn(emptyAgency);

        AgencyDTO result = agencyService.create(emptyDto);

        assertEquals("", result.getName());
    }

    // ==================== getAll ====================

    @Test
    void getAll_success() {
        Agency agency2 = Agency.builder()
                .agencyId(2)
                .name("Speed Bus")
                .contactPersonName("Amit")
                .email("speed@bus.com")
                .phone("1112223333")
                .build();
        when(agencyRepository.findAll()).thenReturn(List.of(agency, agency2));

        List<AgencyDTO> result = agencyService.getAll();

        assertEquals(2, result.size());
        verify(agencyRepository, times(1)).findAll();
    }

    @Test
    void getAll_emptyList() {
        when(agencyRepository.findAll()).thenReturn(Collections.emptyList());

        List<AgencyDTO> result = agencyService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_singleElement() {
        when(agencyRepository.findAll()).thenReturn(List.of(agency));

        List<AgencyDTO> result = agencyService.getAll();

        assertEquals(1, result.size());
        assertEquals("Express Travels", result.get(0).getName());
    }

    // ==================== getById ====================

    @Test
    void getById_success() {
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));

        AgencyDTO result = agencyService.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getAgencyId());
        assertEquals("Express Travels", result.getName());
    }

    @Test
    void getById_notFound() {
        when(agencyRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> agencyService.getById(999));

        assertEquals("Agency not found with id: 999", exception.getMessage());
    }

    @Test
    void getById_withNullId() {
        when(agencyRepository.findById(null)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> agencyService.getById(null));
    }

    // ==================== update ====================

    @Test
    void update_success() {
        AgencyDTO updateDto = AgencyDTO.builder()
                .name("Updated Travels")
                .contactPersonName("New Person")
                .email("new@travels.com")
                .phone("5555555555")
                .build();

        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(agencyRepository.save(any(Agency.class))).thenReturn(agency);

        AgencyDTO result = agencyService.update(1, updateDto);

        assertNotNull(result);
        assertEquals("Updated Travels", agency.getName());
        verify(agencyRepository, times(1)).save(agency);
    }

    @Test
    void update_notFound() {
        when(agencyRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> agencyService.update(999, agencyDTO));

        verify(agencyRepository, never()).save(any());
    }

    @Test
    void update_partialFields() {
        AgencyDTO partialDto = AgencyDTO.builder()
                .name("Only Name Updated")
                .build();

        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(agencyRepository.save(any(Agency.class))).thenReturn(agency);

        AgencyDTO result = agencyService.update(1, partialDto);

        assertNotNull(result);
        assertEquals("Only Name Updated", agency.getName());
        assertEquals("Rahul Sharma", agency.getContactPersonName()); // unchanged
    }

    // ==================== delete ====================

    @Test
    void delete_success() {
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(agencyOfficeRepository.findByAgency_AgencyId(1)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> agencyService.delete(1));

        verify(agencyRepository, times(1)).delete(agency);
    }

    @Test
    void delete_notFound() {
        when(agencyRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> agencyService.delete(999));

        verify(agencyRepository, never()).delete(any());
    }

    @Test
    void delete_agencyHasOffices() {
        AgencyOffice office = AgencyOffice.builder().officeId(1).agency(agency).build();
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(agencyOfficeRepository.findByAgency_AgencyId(1)).thenReturn(List.of(office));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> agencyService.delete(1));

        assertEquals("Agency has offices assigned. Cannot delete.", exception.getMessage());
        verify(agencyRepository, never()).delete(any());
    }
}
