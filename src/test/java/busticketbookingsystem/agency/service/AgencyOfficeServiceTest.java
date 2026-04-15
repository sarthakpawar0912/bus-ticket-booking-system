package busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.AgencyOfficeDTO;
import com.busticketbookingsystem.agency.entity.Agency;
import com.busticketbookingsystem.agency.entity.AgencyOffice;
import com.busticketbookingsystem.agency.repository.AgencyOfficeRepository;
import com.busticketbookingsystem.agency.repository.AgencyRepository;
import com.busticketbookingsystem.agency.repository.BusRepository;
import com.busticketbookingsystem.agency.repository.DriverRepository;
import com.busticketbookingsystem.agency.service.AgencyOfficeService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AgencyOfficeService (the standalone office service).
 * Covers create, getAll, getById, getByAgencyId, update, delete.
 */
@ExtendWith(MockitoExtension.class)
class AgencyOfficeServiceTest {

    @Mock private AgencyOfficeRepository agencyOfficeRepository;
    @Mock private AgencyRepository agencyRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private BusRepository busRepository;
    @Mock private DriverRepository driverRepository;

    @InjectMocks
    private AgencyOfficeService agencyOfficeService;

    private Agency agency;
    private Address address;
    private AgencyOffice office;
    private AgencyOfficeDTO dto;

    @BeforeEach
    void setUp() {
        agency = Agency.builder().agencyId(1).name("Star Travels")
                .contactPersonName("Admin").email("star@travels.com").phone("1234567890").build();

        address = Address.builder().addressId(1).address("456 Office Lane")
                .city("Delhi").state("Delhi").zipCode("110001").build();

        office = AgencyOffice.builder()
                .officeId(1).agency(agency).officeMail("delhi@star.com")
                .officeContactPersonName("Branch Manager")
                .officeContactNumber("9876543210").officeAddress(address).build();

        dto = AgencyOfficeDTO.builder()
                .agencyId(1).officeAddressId(1).officeMail("delhi@star.com")
                .officeContactPersonName("Branch Manager").officeContactNumber("9876543210").build();
    }

    @Nested
    @DisplayName("create() Tests")
    class CreateTests {

        @Test
        @DisplayName("POSITIVE: Should create office successfully")
        void create_Success() {
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(agencyOfficeRepository.save(any(AgencyOffice.class))).thenReturn(office);

            AgencyOfficeDTO result = agencyOfficeService.create(dto);

            assertNotNull(result);
            assertEquals("delhi@star.com", result.getOfficeMail());
            assertEquals("Star Travels", result.getAgencyName());
            assertEquals("Delhi", result.getOfficeCity());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when agency not found")
        void create_AgencyNotFound() {
            when(agencyRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> agencyOfficeService.create(dto));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when address not found")
        void create_AddressNotFound() {
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
            when(addressRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> agencyOfficeService.create(dto));
        }
    }

    @Nested
    @DisplayName("getAll() Tests")
    class GetAllTests {

        @Test
        @DisplayName("POSITIVE: Should return all offices")
        void getAll_ReturnsList() {
            when(agencyOfficeRepository.findAllWithDetails()).thenReturn(List.of(office));

            List<AgencyOfficeDTO> result = agencyOfficeService.getAll();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list")
        void getAll_Empty() {
            when(agencyOfficeRepository.findAllWithDetails()).thenReturn(Collections.emptyList());

            assertTrue(agencyOfficeService.getAll().isEmpty());
        }
    }

    @Nested
    @DisplayName("getById() Tests")
    class GetByIdTests {

        @Test
        @DisplayName("POSITIVE: Should return office for valid ID")
        void getById_Success() {
            when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));

            AgencyOfficeDTO result = agencyOfficeService.getById(1);

            assertEquals(1, result.getOfficeId());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw for invalid ID")
        void getById_NotFound() {
            when(agencyOfficeRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> agencyOfficeService.getById(999));
        }
    }

    @Nested
    @DisplayName("getByAgencyId() Tests")
    class GetByAgencyIdTests {

        @Test
        @DisplayName("POSITIVE: Should return offices for valid agency")
        void getByAgencyId_Success() {
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
            when(agencyOfficeRepository.findByAgencyIdWithDetails(1)).thenReturn(List.of(office));

            List<AgencyOfficeDTO> result = agencyOfficeService.getByAgencyId(1);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when agency not found")
        void getByAgencyId_AgencyNotFound() {
            when(agencyRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> agencyOfficeService.getByAgencyId(999));
        }

        @Test
        @DisplayName("EDGE CASE: Should return empty list when agency has no offices")
        void getByAgencyId_NoOffices() {
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
            when(agencyOfficeRepository.findByAgencyIdWithDetails(1)).thenReturn(Collections.emptyList());

            assertTrue(agencyOfficeService.getByAgencyId(1).isEmpty());
        }
    }

    @Nested
    @DisplayName("update() Tests")
    class UpdateTests {

        @Test
        @DisplayName("POSITIVE: Should update office with all new fields")
        void update_AllFields_Success() {
            AgencyOfficeDTO updateDto = AgencyOfficeDTO.builder()
                    .agencyId(1).officeAddressId(1).officeMail("updated@star.com")
                    .officeContactPersonName("New Manager").officeContactNumber("1111111111").build();

            when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
            when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
            when(addressRepository.findById(1)).thenReturn(Optional.of(address));
            when(agencyOfficeRepository.save(any(AgencyOffice.class))).thenReturn(office);

            AgencyOfficeDTO result = agencyOfficeService.update(1, updateDto);

            assertNotNull(result);
        }

        @Test
        @DisplayName("EDGE CASE: Should only update non-null fields (partial update)")
        void update_PartialFields() {
            // Only update officeMail, leave others null
            AgencyOfficeDTO partialDto = AgencyOfficeDTO.builder()
                    .officeMail("partial@star.com").build();

            when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
            when(agencyOfficeRepository.save(any(AgencyOffice.class))).thenReturn(office);

            AgencyOfficeDTO result = agencyOfficeService.update(1, partialDto);

            assertNotNull(result);
            // agencyRepository and addressRepository should NOT be called since IDs are null
            verify(agencyRepository, never()).findById(anyInt());
            verify(addressRepository, never()).findById(anyInt());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when office not found for update")
        void update_OfficeNotFound() {
            when(agencyOfficeRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> agencyOfficeService.update(999, dto));
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when new agency ID is invalid")
        void update_InvalidNewAgency() {
            AgencyOfficeDTO updateDto = AgencyOfficeDTO.builder().agencyId(999).build();
            when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
            when(agencyRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> agencyOfficeService.update(1, updateDto));
        }
    }

    @Nested
    @DisplayName("delete() Tests")
    class DeleteTests {

        @Test
        @DisplayName("POSITIVE: Should delete office when no buses assigned")
        void delete_Success() {
            when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
            when(busRepository.existsByOffice_OfficeId(1)).thenReturn(false);

            agencyOfficeService.delete(1);

            verify(agencyOfficeRepository).delete(office);
        }

        @Test
        @DisplayName("NEGATIVE: Should throw BadRequestException when office has buses")
        void delete_HasBuses_Throws() {
            when(agencyOfficeRepository.findById(1)).thenReturn(Optional.of(office));
            when(busRepository.existsByOffice_OfficeId(1)).thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> agencyOfficeService.delete(1));

            assertEquals("Office has buses assigned. Cannot delete.", ex.getMessage());
            verify(agencyOfficeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("NEGATIVE: Should throw when office not found for deletion")
        void delete_OfficeNotFound() {
            when(agencyOfficeRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> agencyOfficeService.delete(999));
        }
    }

    @Test
    @DisplayName("EDGE CASE: mapToDTO should handle null officeAddress gracefully")
    void mapToDTO_NullAddress() {
        AgencyOffice noAddrOffice = AgencyOffice.builder()
                .officeId(2).agency(agency).officeMail("test@test.com")
                .officeContactPersonName("Test").officeContactNumber("1234567890")
                .officeAddress(null).build();

        when(agencyOfficeRepository.findById(2)).thenReturn(Optional.of(noAddrOffice));

        AgencyOfficeDTO result = agencyOfficeService.getById(2);

        assertNull(result.getOfficeAddressId());
        assertNull(result.getOfficeCity());
    }
}
