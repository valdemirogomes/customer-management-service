package com.challenge.customers.service;

import com.challenge.customers.api.*;
import com.challenge.customers.domain.*;
import com.challenge.customers.exception.*;
import com.challenge.customers.integration.ScoreClient;
import com.challenge.customers.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CustomerService {
    private final CustomerRepository repo;
    private final CustomerJdbcRepository jdbc;
    private final ScoreClient score;

    public CustomerService(CustomerRepository r, CustomerJdbcRepository j, ScoreClient s) {
        repo = r;
        jdbc = j;
        score = s;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest q) {
        if (repo.existsByCpf(q.cpf())) throw new ConflictException("CPF already registered");
        return CustomerResponse.from(repo.save(new Customer(q.name(), q.cpf(), q.email(), q.status())));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest q) {
        Customer c = getEntity(id);
        if (repo.existsByCpfAndIdNot(q.cpf(), id)) throw new ConflictException("CPF already registered");
        c.setName(q.name());
        c.setCpf(q.cpf());
        c.setEmail(q.email());
        c.setStatus(q.status());
        return CustomerResponse.from(c);
    }

    @Transactional
    public void delete(Long id) {
        Customer c = getEntity(id);
        repo.delete(c);
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        return CustomerResponse.from(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> list(CustomerStatus status) {
        var xs = status == null ? repo.findAll() : repo.findByStatus(status);
        return xs.stream().map(CustomerResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> search(String name) {
        return repo.searchByNameNative(name).stream().map(CustomerResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ScoreResponse score(Long id) {
        return score.getScore(getEntity(id).getCpf());
    }

    public long countByStatus(CustomerStatus s) {
        return jdbc.countByStatus(s.name());
    }

    private Customer getEntity(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Customer not found: " + id));
    }
}