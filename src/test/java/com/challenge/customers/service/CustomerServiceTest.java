package com.challenge.customers.service;

import com.challenge.customers.api.CustomerRequest;
import com.challenge.customers.domain.CustomerStatus;
import com.challenge.customers.exception.ConflictException;
import com.challenge.customers.exception.NotFoundException;
import com.challenge.customers.integration.ScoreClient;
import com.challenge.customers.repository.CustomerJdbcRepository;
import com.challenge.customers.repository.CustomerRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerServiceTest {
    CustomerRepository repo = mock(CustomerRepository.class);
    CustomerJdbcRepository jdbc = mock(CustomerJdbcRepository.class);
    ScoreClient score = mock(ScoreClient.class);
    CustomerService service = new CustomerService(repo, jdbc, score);

    @Test
    void duplicateCpf() {
        when(repo.existsByCpf("12345678901")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.create(new CustomerRequest("Joao", "12345678901", "j@x.com", CustomerStatus.ACTIVE)));
    }

    @Test
    void notFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(99L));
    }
}