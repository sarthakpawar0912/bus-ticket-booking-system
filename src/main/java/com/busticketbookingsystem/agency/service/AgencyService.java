package com.busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.*;
import com.busticketbookingsystem.agency.entity.*;
import com.busticketbookingsystem.agency.exception.BadRequestException;
import com.busticketbookingsystem.agency.exception.ResourceNotFoundException;
import com.busticketbookingsystem.agency.repository.*;
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

    // ✅ Constructor Injection: Standard practice for safety and testing
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
    // AGENCY BUSINESS LOGIC [cite: 2]
    // ==========================================

    @Transactional
    public AgencyResponseDTO createAgency(AgencyRequestDTO dto) {
        if (agencyRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already registered to another agency.");
        }

        Agency agency = Agency.builder()
                .name(dto.getName())
                .contactPersonName(dto.getContactPersonName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();

        return mapToAgencyResponseDTO(agencyRepository.save(agency));
    }

    public List<AgencyResponseDTO> getAllAgencies() {
        return agencyRepository.findAll().stream()
                .map(this::mapToAgencyResponseDTO)
                .collect(Collectors.toList());
    }

    public AgencyResponseDTO getAgencyById(Integer id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with ID: " + id));
        return mapToAgencyResponseDTO(agency);
    }

    @Transactional
    public void deleteAgency(Integer id) {
        // ✅ Safety Check: Cannot delete if offices exist [cite: 4]
        if (officeRepository.existsByAgency_AgencyId(id)) {
            throw new BadRequestException("Cannot delete Agency. Remove all Offices first.");
        }
        agencyRepository.deleteById(id);
    }

    // ==========================================
    // OFFICE BUSINESS LOGIC [cite: 4]
    // ==========================================

    @Transactional
    public OfficeResponseDTO createOffice(OfficeRequestDTO dto) {
        Agency agency = agencyRepository.findById(dto.getAgencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent Agency not found"));

        AgencyOffice office = AgencyOffice.builder()
                .agency(agency)
                .officeMail(dto.getOfficeMail())
                .officeContactPersonName(dto.getOfficeContactPersonName())
                .officeContactNumber(dto.getOfficeContactNumber())
                .officeAddressId(dto.getOfficeAddressId())
                .build();

        return mapToOfficeResponseDTO(officeRepository.save(office));
    }

    public List<OfficeResponseDTO> getAllOffices() {
        return officeRepository.findAll().stream()
                .map(this::mapToOfficeResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteOffice(Integer officeId) {
        // ✅ Safety Check: Prevents crashing if Buses or Drivers are linked [cite: 6, 7]
        if (busRepository.existsByOffice_OfficeId(officeId)) {
            throw new BadRequestException("Cannot delete Office. Move or delete registered Buses first.");
        }
        if (driverRepository.existsByOffice_OfficeId(officeId)) {
            throw new BadRequestException("Cannot delete Office. Reassign active Drivers first.");
        }
        officeRepository.deleteById(officeId);
    }

    // ==========================================
    // MISSING AGENCY UPDATE METHOD
    // ==========================================
    @Transactional
    public AgencyResponseDTO updateAgency(Integer id, AgencyRequestDTO dto) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with ID: " + id));

        agency.setName(dto.getName());
        agency.setContactPersonName(dto.getContactPersonName());

        // Safety check: Only throw error if they are changing the email to one that is already taken
        if (!agency.getEmail().equals(dto.getEmail()) && agencyRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already in use.");
        }
        agency.setEmail(dto.getEmail());

        if (!agency.getPhone().equals(dto.getPhone()) && agencyRepository.existsByPhone(dto.getPhone())) {
            throw new BadRequestException("Phone number already in use.");
        }
        agency.setPhone(dto.getPhone());

        return mapToAgencyResponseDTO(agencyRepository.save(agency));
    }

    // ==========================================
    // MISSING OFFICE METHODS
    // ==========================================
    public OfficeResponseDTO getOfficeById(Integer officeId) {
        AgencyOffice office = officeRepository.findById(officeId)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with ID: " + officeId));
        return mapToOfficeResponseDTO(office);
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
    // MAPPING HELPERS (Entity -> DTO)
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