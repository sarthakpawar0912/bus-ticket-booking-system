package com.busticketbookingsystem.agency.repository;

import com.busticketbookingsystem.agency.entity.AgencyOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgencyOfficeRepository extends JpaRepository<AgencyOffice, Integer> {

    /**
     * CRITICAL for safe deletions!
     * Used by AgencyService to check if an Agency still has physical offices open
     * before allowing the Admin to delete the Agency entirely.
     */
    boolean existsByAgency_AgencyId(Integer agencyId);
}