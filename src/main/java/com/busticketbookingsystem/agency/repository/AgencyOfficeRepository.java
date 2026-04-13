package com.busticketbookingsystem.agency.repository;

import com.busticketbookingsystem.agency.entity.AgencyOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgencyOfficeRepository extends JpaRepository<AgencyOffice, Integer> {

    @Query("SELECT o FROM AgencyOffice o LEFT JOIN FETCH o.agency LEFT JOIN FETCH o.officeAddress")
    List<AgencyOffice> findAllWithDetails();

    @Query("SELECT o FROM AgencyOffice o LEFT JOIN FETCH o.agency LEFT JOIN FETCH o.officeAddress WHERE o.agency.agencyId = :agencyId")
    List<AgencyOffice> findByAgencyIdWithDetails(@Param("agencyId") Integer agencyId);

    /**
     * CRITICAL for safe deletions!
     * Used by AgencyService to check if an Agency still has physical offices open
     * before allowing the Admin to delete the Agency entirely.
     */
    boolean existsByAgency_AgencyId(Integer agencyId);

    List<AgencyOffice> findByAgency_AgencyId(Integer agencyId);
}