package com.busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.dto.BusRequestDTO;
import com.busticketbookingsystem.agency.dto.BusResponseDTO;
import com.busticketbookingsystem.agency.entity.AgencyOffice;
import com.busticketbookingsystem.agency.entity.Bus;
import com.busticketbookingsystem.agency.repository.AgencyOfficeRepository;
import com.busticketbookingsystem.agency.repository.BusRepository;
import com.busticketbookingsystem.exception.BadRequestException;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BusService {

    private final BusRepository busRepository;
    private final AgencyOfficeRepository officeRepository;

    // Constructor Injection
    public BusService(BusRepository busRepository, AgencyOfficeRepository officeRepository) {
        this.busRepository = busRepository;
        this.officeRepository = officeRepository;
    }

    @Transactional
    public BusResponseDTO addBus(BusRequestDTO dto) {
        // 1. Check if the license plate is already registered
        if (busRepository.existsByRegistrationNumber(dto.getRegistrationNumber())) {
            throw new BadRequestException("A bus with this registration number already exists.");
        }

        // 2. Find the office this bus belongs to
        AgencyOffice office = officeRepository.findById(dto.getOfficeId())
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with ID: " + dto.getOfficeId()));

        // 3. Build and save the entity
        Bus bus = Bus.builder()
                .office(office)
                .registrationNumber(dto.getRegistrationNumber())
                .capacity(dto.getCapacity())
                .type(dto.getType())
                .build();

        return mapToBusResponseDTO(busRepository.save(bus));
    }

    public List<BusResponseDTO> getAllBuses() {
        return busRepository.findAllWithOffice()
                .stream()
                .map(this::mapToBusResponseDTO)
                .toList();
    }

    public BusResponseDTO getBusById(Integer id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with ID: " + id));
        return mapToBusResponseDTO(bus);
    }

    @Transactional
    public BusResponseDTO updateBus(Integer id, BusRequestDTO dto) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with ID: " + id));

        // If they are changing the registration number, ensure the new one isn't taken
        if (!bus.getRegistrationNumber().equals(dto.getRegistrationNumber()) &&
                busRepository.existsByRegistrationNumber(dto.getRegistrationNumber())) {
            throw new BadRequestException("The new registration number is already in use.");
        }

        bus.setRegistrationNumber(dto.getRegistrationNumber());
        bus.setCapacity(dto.getCapacity());
        bus.setType(dto.getType());

        // If they are moving the bus to a different office
        if (!bus.getOffice().getOfficeId().equals(dto.getOfficeId())) {
            AgencyOffice newOffice = officeRepository.findById(dto.getOfficeId())
                    .orElseThrow(() -> new ResourceNotFoundException("New Office not found"));
            bus.setOffice(newOffice);
        }

        return mapToBusResponseDTO(busRepository.save(bus));
    }

    @Transactional
    public void deleteBus(Integer id) {
        if (!busRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bus not found with ID: " + id);
        }
        // Note: If Member 3's "Trips" rely on this bus, you might need a check here later!
        busRepository.deleteById(id);
    }

    public List<BusResponseDTO> getBusesByOfficeId(Integer officeId) {
        return busRepository.findByOffice_OfficeId(officeId)
                .stream()
                .map(this::mapToBusResponseDTO)
                .toList();
    }

    // Helper mapping method
    private BusResponseDTO mapToBusResponseDTO(Bus bus) {
        return BusResponseDTO.builder()
                .busId(bus.getBusId())
                .officeId(bus.getOffice().getOfficeId())
                .registrationNumber(bus.getRegistrationNumber())
                .capacity(bus.getCapacity())
                .type(bus.getType())
                .build();
    }
}