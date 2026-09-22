package com.challenge.customers;

import com.challenge.customers.api.*;
import com.challenge.customers.domain.*;
import com.challenge.customers.exception.*;
import com.challenge.customers.integration.ScoreClient;
import com.challenge.customers.repository.*;
import com.challenge.customers.service.CustomerService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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