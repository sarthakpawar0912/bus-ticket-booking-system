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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgencyOfficeService {

    private static final String AGENCY_NOT_FOUND = "Agency not found with id: ";
    private static final String AGENCY_OFFICE_NOT_FOUND = "Agency office not found with id: ";

    private final AgencyOfficeRepository agencyOfficeRepository;
    private final AgencyRepository agencyRepository;
    private final AddressRepository addressRepository;
    private final BusRepository busRepository;
    private final DriverRepository driverRepository;

    private AgencyOfficeDTO mapToDTO(AgencyOffice office) {
        return AgencyOfficeDTO.builder()
                .officeId(office.getOfficeId())
                .agencyId(office.getAgency().getAgencyId())
                .agencyName(office.getAgency().getName())
                .officeMail(office.getOfficeMail())
                .officeContactPersonName(office.getOfficeContactPersonName())
                .officeContactNumber(office.getOfficeContactNumber())
                .officeAddressId(office.getOfficeAddress() != null ? office.getOfficeAddress().getAddressId() : null)
                .officeCity(office.getOfficeAddress() != null ? office.getOfficeAddress().getCity() : null)
                .build();
    }

    @Transactional
    public AgencyOfficeDTO create(AgencyOfficeDTO dto) {
        Agency agency = agencyRepository.findById(dto.getAgencyId())
                .orElseThrow(() -> new ResourceNotFoundException(AGENCY_NOT_FOUND + dto.getAgencyId()));

        Address address = addressRepository.findById(dto.getOfficeAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + dto.getOfficeAddressId()));

        AgencyOffice office = AgencyOffice.builder()
                .agency(agency)
                .officeMail(dto.getOfficeMail())
                .officeContactPersonName(dto.getOfficeContactPersonName())
                .officeContactNumber(dto.getOfficeContactNumber())
                .officeAddress(address)
                .build();

        return mapToDTO(agencyOfficeRepository.save(office));
    }

    public List<AgencyOfficeDTO> getAll() {
        return agencyOfficeRepository.findAllWithDetails().stream()
                .map(this::mapToDTO)
                .toList();
    }

    public AgencyOfficeDTO getById(Integer id) {
        AgencyOffice office = agencyOfficeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AGENCY_OFFICE_NOT_FOUND + id));
        return mapToDTO(office);
    }

    public List<AgencyOfficeDTO> getByAgencyId(Integer agencyId) {
        agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException(AGENCY_NOT_FOUND + agencyId));
        return agencyOfficeRepository.findByAgencyIdWithDetails(agencyId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public AgencyOfficeDTO update(Integer id, AgencyOfficeDTO dto) {
        AgencyOffice office = agencyOfficeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AGENCY_OFFICE_NOT_FOUND + id));

        if (dto.getAgencyId() != null) {
            Agency agency = agencyRepository.findById(dto.getAgencyId())
                    .orElseThrow(() -> new ResourceNotFoundException(AGENCY_NOT_FOUND + dto.getAgencyId()));
            office.setAgency(agency);
        }

        if (dto.getOfficeAddressId() != null) {
            Address address = addressRepository.findById(dto.getOfficeAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + dto.getOfficeAddressId()));
            office.setOfficeAddress(address);
        }

        if (dto.getOfficeMail() != null) {
            office.setOfficeMail(dto.getOfficeMail());
        }
        if (dto.getOfficeContactPersonName() != null) {
            office.setOfficeContactPersonName(dto.getOfficeContactPersonName());
        }
        if (dto.getOfficeContactNumber() != null) {
            office.setOfficeContactNumber(dto.getOfficeContactNumber());
        }

        return mapToDTO(agencyOfficeRepository.save(office));
    }

    @Transactional
    public void delete(Integer id) {
        AgencyOffice office = agencyOfficeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AGENCY_OFFICE_NOT_FOUND + id));

        if (busRepository.existsByOffice_OfficeId(id)) {
            throw new BadRequestException("Office has buses assigned. Cannot delete.");
        }

        agencyOfficeRepository.delete(office);
    }
}
