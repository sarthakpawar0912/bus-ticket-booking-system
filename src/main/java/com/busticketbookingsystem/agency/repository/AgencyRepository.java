package com.busticketbookingsystem.agency.repository;

import com.busticketbookingsystem.agency.entity.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Integer> {

    /**
     * Prevents creating two agencies with the same email.
     */
    boolean existsByEmail(String email);

    /**
     * Prevents creating two agencies with the same phone number.
     */
    boolean existsByPhone(String phone);
}