package com.busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.AgencyRequestDTO;
import com.busticketbookingsystem.agency.dto.AgencyResponseDTO;
import com.busticketbookingsystem.agency.dto.OfficeRequestDTO;
import com.busticketbookingsystem.agency.dto.OfficeResponseDTO;
import com.busticketbookingsystem.agency.entity.Agency;
import com.busticketbookingsystem.agency.entity.AgencyOffice;
import com.busticketbookingsystem.agency.exception.BadRequestException;
import com.busticketbookingsystem.agency.exception.ResourceNotFoundException;
import com.busticketbookingsystem.agency.repository.AgencyOfficeRepository;
import com.busticketbookingsystem.agency.repository.AgencyRepository;
import com.busticketbookingsystem.agency.repository.BusRepository;
import com.busticketbookingsystem.agency.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyOfficeRepository officeRepository;
    private final BusRepository busRepository;
    private final DriverRepository driverRepository;

    // ✅ Constructor Injection (Industry Standard)
    public AgencyService(AgencyRepository agencyRepository,
                         AgencyOfficeRepository officeRepository,
                         BusRepository busRepository,
                         DriverRepository driverRepository) {
        this.agencyRepository = agencyRepository;
        this.officeRepository = officeRepository;
        this.busRepository = busRepository;
        this.driverRepository = driverRepository;
    }

    // ==========================================
    // AGENCY LOGIC
    // ==========================================

    @Transactional
    public AgencyResponseDTO createAgency(AgencyRequestDTO dto) {
        // 1. Business Rule: Prevent duplicate emails and phones
        if (agencyRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("An agency with this email already exists.");
        }
        if (agencyRepository.existsByPhone(dto.getPhone())) {
            throw new BadRequestException("An agency with this phone number already exists.");
        }

        // 2. Map DTO to Entity using @Builder
        Agency agency = Agency.builder()
                .name(dto.getName())
                .contactPersonName(dto.getContactPersonName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();

        // 3. Save and return mapped response
        Agency savedAgency = agencyRepository.save(agency);
        return mapToAgencyResponseDTO(savedAgency);
    }



    public List<AgencyResponseDTO> getAllAgencies() {
        return agencyRepository.findAll()
                .stream()
                .map(this::mapToAgencyResponseDTO)
                .collect(Collectors.toList());
    }

    public AgencyResponseDTO getAgencyById(Integer id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with ID: " + id));
        return mapToAgencyResponseDTO(agency);
    }

    @Transactional
    public AgencyResponseDTO updateAgency(Integer id, AgencyRequestDTO dto) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with ID: " + id));

        // Update fields
        agency.setName(dto.getName());
        agency.setContactPersonName(dto.getContactPersonName());
        agency.setEmail(dto.getEmail());
        agency.setPhone(dto.getPhone());

        return mapToAgencyResponseDTO(agencyRepository.save(agency));
    }

    @Transactional
    public void deleteAgency(Integer id) {
        if (!agencyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agency not found with ID: " + id);
        }

        // ✅ Safety Net: Check if offices exist before deleting!
        if (officeRepository.existsByAgency_AgencyId(id)) {
            throw new BadRequestException("Cannot delete Agency. It still has active offices assigned to it.");
        }

        agencyRepository.deleteById(id);
    }

    // ==========================================
    // OFFICE LOGIC
    // ==========================================

    @Transactional
    public OfficeResponseDTO createOffice(OfficeRequestDTO dto) {
        // 1. Verify parent agency exists
        Agency agency = agencyRepository.findById(dto.getAgencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent Agency not found with ID: " + dto.getAgencyId()));

        // 2. Build Entity
        AgencyOffice office = AgencyOffice.builder()
                .agency(agency)
                .officeMail(dto.getOfficeMail())
                .officeContactPersonName(dto.getOfficeContactPersonName())
                .officeContactNumber(dto.getOfficeContactNumber())
                .officeAddressId(dto.getOfficeAddressId())
                .build();

        AgencyOffice savedOffice = officeRepository.save(office);
        return mapToOfficeResponseDTO(savedOffice);
    }

    @Transactional
    public void deleteOffice(Integer officeId) {
        if (!officeRepository.existsById(officeId)) {
            throw new ResourceNotFoundException("Office not found with ID: " + officeId);
        }

        // ✅ Safety Net: Check for assigned Buses or Drivers!
        if (busRepository.existsByOffice_OfficeId(officeId)) {
            throw new BadRequestException("Cannot delete Office. Buses are still registered to this location.");
        }
        if (driverRepository.existsByOffice_OfficeId(officeId)) {
            throw new BadRequestException("Cannot delete Office. Drivers are still assigned to this location.");
        }

        officeRepository.deleteById(officeId);
    }

    // ----------------------------------------------------
    // MISSING OFFICE LOGIC (Paste this into AgencyService)
    // ----------------------------------------------------

    public OfficeResponseDTO getOfficeById(Integer officeId) {
        AgencyOffice office = officeRepository.findById(officeId)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with ID: " + officeId));
        return mapToOfficeResponseDTO(office);
    }

    public List<OfficeResponseDTO> getAllOffices() {
        return officeRepository.findAll()
                .stream()
                .map(this::mapToOfficeResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OfficeResponseDTO updateOffice(Integer officeId, OfficeRequestDTO dto) {
        AgencyOffice office = officeRepository.findById(officeId)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with ID: " + officeId));

        // If they are trying to move the office to a different parent agency
        if (!office.getAgency().getAgencyId().equals(dto.getAgencyId())) {
            Agency newAgency = agencyRepository.findById(dto.getAgencyId())
                    .orElseThrow(() -> new ResourceNotFoundException("New Parent Agency not found"));
            office.setAgency(newAgency);
        }

        // Update the rest of the fields
        office.setOfficeMail(dto.getOfficeMail());
        office.setOfficeContactPersonName(dto.getOfficeContactPersonName());
        office.setOfficeContactNumber(dto.getOfficeContactNumber());
        office.setOfficeAddressId(dto.getOfficeAddressId());

        return mapToOfficeResponseDTO(officeRepository.save(office));
    }
    // ==========================================
    // HELPER MAPPING METHODS
    // ==========================================

    private AgencyResponseDTO mapToAgencyResponseDTO(Agency agency) {
        return AgencyResponseDTO.builder()
                .agencyId(agency.getAgencyId())
                .name(agency.getName())
                .contactPersonName(agency.getContactPersonName())
                .email(agency.getEmail())
                .phone(agency.getPhone())
                .build();
    }

    private OfficeResponseDTO mapToOfficeResponseDTO(AgencyOffice office) {
        return OfficeResponseDTO.builder()
                .officeId(office.getOfficeId())
                .agencyId(office.getAgency().getAgencyId())
                .officeAddressId(office.getOfficeAddressId())
                .officeMail(office.getOfficeMail())
                .officeContactPersonName(office.getOfficeContactPersonName())
                .officeContactNumber(office.getOfficeContactNumber())
                .build();
    }
}