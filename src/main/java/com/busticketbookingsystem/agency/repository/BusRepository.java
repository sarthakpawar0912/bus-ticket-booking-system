package com.busticketbookingsystem.agency.repository;

import com.busticketbookingsystem.agency.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusRepository extends JpaRepository<Bus, Integer> {

    /**
     * Used by BusService to prevent adding two buses with the same license plate.
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Used by AgencyService to prevent deleting an Office if it still has physical buses parked there.
     * (This matches the safe-delete logic from the Customer module!)
     */
    boolean existsByOffice_OfficeId(Integer officeId);
}