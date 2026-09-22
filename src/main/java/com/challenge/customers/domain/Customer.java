package com.challenge.customers.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "customers", uniqueConstraints = {@UniqueConstraint(name = "uk_customer_cpf", columnNames = "cpf")})
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, length = 11)
    private String cpf;
    @Column(nullable = false)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status;

    protected Customer() {
    }

    public Customer(String name, String cpf, String email, CustomerStatus status) {
        this.name = name;
        this.cpf = cpf;
        this.email = email;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String v) {
        cpf = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        email = v;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus v) {
        status = v;
    }
}