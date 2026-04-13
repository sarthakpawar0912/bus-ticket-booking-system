package com.busticketbookingsystem.customer.repository;

import com.busticketbookingsystem.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Integer> {

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.address")
    List<Customer> findAllWithAddress();

    Optional<Customer> findByEmail(String email);
    boolean existsByAddress_AddressId(Integer addressId);
}
