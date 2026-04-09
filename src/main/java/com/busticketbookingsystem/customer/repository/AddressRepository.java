package com.busticketbookingsystem.customer.repository;

import com.busticketbookingsystem.customer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address,Integer> {


}
