package com.busticketbookingsystem.CUSTOMER.repository;

import com.busticketbookingsystem.CUSTOMER.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Integer> {

    Optional<Customer> findByEmail(String email);
    boolean existsByAddress_AddressId(Integer addressId);
}
