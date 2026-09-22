package com.challenge.customers.api;

import com.challenge.customers.domain.*;

public record CustomerResponse(Long id, String name, String cpf, String email, CustomerStatus status) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getCpf(), c.getEmail(), c.getStatus());
    }
}