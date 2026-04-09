package com.busticketbookingsystem.agency.repository;

import com.busticketbookingsystem.agency.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer> {

    /**
     * Prevents adding two drivers with the exact same license number.
     */
    boolean existsByLicenseNumber(String licenseNumber);

    /**
     * Prevents adding two drivers with the exact same phone number.
     */
    boolean existsByPhone(String phone);

    /**
     * CRITICAL for safe deletions!
     * Used by AgencyService to check if an Office still has active drivers
     * before allowing the Admin to delete the Office.
     */
    boolean existsByOffice_OfficeId(Integer officeId);
}