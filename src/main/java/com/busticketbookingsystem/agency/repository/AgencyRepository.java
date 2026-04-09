package com.busticketbookingsystem.agency.repository;

import com.busticketbookingsystem.agency.entity.Agency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyRepository extends JpaRepository<Agency, Integer> {
}
