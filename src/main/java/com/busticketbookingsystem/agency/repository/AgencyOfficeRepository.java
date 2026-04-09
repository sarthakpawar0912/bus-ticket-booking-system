package com.busticketbookingsystem.agency.repository;

import com.busticketbookingsystem.agency.entity.AgencyOffice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyOfficeRepository extends JpaRepository<AgencyOffice, Integer> {
}