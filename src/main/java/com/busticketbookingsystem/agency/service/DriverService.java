package com.busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.DriverRequestDTO;
import com.busticketbookingsystem.agency.dto.DriverResponseDTO;
import com.busticketbookingsystem.agency.entity.AgencyOffice;
import com.busticketbookingsystem.agency.entity.Driver;
import com.busticketbookingsystem.agency.exception.BadRequestException;
import com.busticketbookingsystem.agency.exception.ResourceNotFoundException;
import com.busticketbookingsystem.agency.repository.AgencyOfficeRepository;
import com.busticketbookingsystem.agency.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final AgencyOfficeRepository officeRepository;

    public DriverService(DriverRepository driverRepository, AgencyOfficeRepository officeRepository) {
        this.driverRepository = driverRepository;
        this.officeRepository = officeRepository;
    }

    @Transactional
    public DriverResponseDTO addDriver(DriverRequestDTO dto) {
        if (driverRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new BadRequestException("A driver with this license number already exists.");
        }
        if (driverRepository.existsByPhone(dto.getPhone())) {
            throw new BadRequestException("A driver with this phone number is already registered.");
        }

        AgencyOffice office = officeRepository.findById(dto.getOfficeId())
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with ID: " + dto.getOfficeId()));

        Driver driver = Driver.builder()
                .licenseNumber(dto.getLicenseNumber())
                .name(dto.getName())
                .phone(dto.getPhone())
                .office(office)
                .addressId(dto.getAddressId())
                .build();

        return mapToDriverResponseDTO(driverRepository.save(driver));
    }

    public List<DriverResponseDTO> getAllDrivers() {
        return driverRepository.findAll()
                .stream()
                .map(this::mapToDriverResponseDTO)
                .collect(Collectors.toList());
    }

    public DriverResponseDTO getDriverById(Integer id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + id));
        return mapToDriverResponseDTO(driver);
    }

    @Transactional
    public DriverResponseDTO updateDriver(Integer id, DriverRequestDTO dto) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + id));

        driver.setName(dto.getName());

        // Check uniqueness only if they are actually changing the phone number or license
        if (!driver.getPhone().equals(dto.getPhone()) && driverRepository.existsByPhone(dto.getPhone())) {
            throw new BadRequestException("Phone number already in use.");
        }
        driver.setPhone(dto.getPhone());

        if (!driver.getLicenseNumber().equals(dto.getLicenseNumber()) && driverRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new BadRequestException("License number already in use.");
        }
        driver.setLicenseNumber(dto.getLicenseNumber());

        if (!driver.getOffice().getOfficeId().equals(dto.getOfficeId())) {
            AgencyOffice newOffice = officeRepository.findById(dto.getOfficeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Office not found"));
            driver.setOffice(newOffice);
        }

        driver.setAddressId(dto.getAddressId());

        return mapToDriverResponseDTO(driverRepository.save(driver));
    }

    @Transactional
    public void deleteDriver(Integer id) {
        if (!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException("Driver not found with ID: " + id);
        }
        driverRepository.deleteById(id);
    }

    // Helper mapping method
    private DriverResponseDTO mapToDriverResponseDTO(Driver driver) {
        return DriverResponseDTO.builder()
                .driverId(driver.getDriverId())
                .licenseNumber(driver.getLicenseNumber())
                .name(driver.getName())
                .phone(driver.getPhone())
                .officeId(driver.getOffice().getOfficeId())
                .addressId(driver.getAddressId())
                .build();
    }
}