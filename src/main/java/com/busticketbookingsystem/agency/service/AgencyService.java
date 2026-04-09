package com.busticketbookingsystem.agency.service;

import com.busticketbookingsystem.agency.entity.*;
import com.busticketbookingsystem.agency.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgencyService {

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private AgencyOfficeRepository officeRepository;

    @Autowired
    private BusRepository busRepository;

    // Create Agency
    public Agency createAgency(Agency agency) {
        return agencyRepository.save(agency);
    }

    // Get All Agencies
    public List<Agency> getAllAgencies() {
        return agencyRepository.findAll();
    }

    // Add Office
    public AgencyOffice addOffice(Integer agencyId, AgencyOffice office) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        office.setAgency(agency);
        return officeRepository.save(office);
    }

    public Bus addBus(Integer officeId, Bus bus) {
        AgencyOffice office = officeRepository.findById(officeId)
                .orElseThrow(() -> new RuntimeException("Office not found"));

        bus.setOffice(office);
        return busRepository.save(bus);
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }


    public Agency getAgencyById(Integer id) {
        return agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agency not found"));
    }

    public Agency updateAgency(Integer id, Agency updatedAgency) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        agency.setName(updatedAgency.getName());
        agency.setContactPersonName(updatedAgency.getContactPersonName());
        agency.setEmail(updatedAgency.getEmail());
        agency.setPhone(updatedAgency.getPhone());

        return agencyRepository.save(agency);
    }

    public void deleteAgency(Integer id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        agencyRepository.delete(agency);
    }

    public Bus updateBus(Integer id, Bus updatedBus) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        bus.setRegistrationNumber(updatedBus.getRegistrationNumber());
        bus.setCapacity(updatedBus.getCapacity());
        bus.setType(updatedBus.getType());~

        return busRepository.save(bus);
    }

    public void deleteBus(Integer id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        busRepository.delete(bus);
    }


}