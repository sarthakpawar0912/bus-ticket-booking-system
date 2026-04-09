package com.busticketbookingsystem.CUSTOMER.ENTITY;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "customers")
@Data
@Builder
public class Customer {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="customer_id")
    private Integer customerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;


    @ManyToOne
    @JoinColumn(name="address_id")
    private Address address;
}
