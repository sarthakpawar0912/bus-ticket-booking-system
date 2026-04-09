package com.busticketbookingsystem.agency.repository;

import com.busticketbookingsystem.agency.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRepository extends JpaRepository<Bus, Integer> {
}