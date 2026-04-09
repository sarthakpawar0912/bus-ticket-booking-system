package com.busticketbookingsystem.CUSTOMER.ENTITY;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="addresses")
@Data
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="address_id")
    private Integer addressId;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(name="zip_code")
    private String zipCode;
}
