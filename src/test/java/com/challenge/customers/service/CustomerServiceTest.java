package com.challenge.customers.service;

import com.challenge.customers.api.CustomerRequest;
import com.challenge.customers.api.ScoreResponse;
import com.challenge.customers.domain.Customer;
import com.challenge.customers.domain.CustomerStatus;
import com.challenge.customers.exception.ConflictException;
import com.challenge.customers.exception.NotFoundException;
import com.challenge.customers.integration.ScoreClient;
import com.challenge.customers.repository.CustomerJdbcRepository;
import com.challenge.customers.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repo;

    @Mock
    private CustomerJdbcRepository jdbc;

    @Mock
    private ScoreClient score;

    @InjectMocks
    private CustomerService service;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(
                "Joao da Silva",
                "12345678901",
                "joao@email.com",
                CustomerStatus.ACTIVE
        );
    }

    @Test
    void shouldCreateCustomer() {
        var request = new CustomerRequest(
                "Joao da Silva",
                "12345678901",
                "joao@email.com",
                CustomerStatus.ACTIVE
        );

        when(repo.existsByCpf("12345678901"))
                .thenReturn(false);

        when(repo.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(request);

        assertNotNull(response);
        assertEquals("Joao da Silva", response.name());
        assertEquals("12345678901", response.cpf());
        assertEquals("joao@email.com", response.email());
        assertEquals(CustomerStatus.ACTIVE, response.status());

        verify(repo).existsByCpf("12345678901");

        var captor = ArgumentCaptor.forClass(Customer.class);

        verify(repo).save(captor.capture());

        var saved = captor.getValue();

        assertEquals("Joao da Silva", saved.getName());
        assertEquals("12345678901", saved.getCpf());
        assertEquals("joao@email.com", saved.getEmail());
        assertEquals(CustomerStatus.ACTIVE, saved.getStatus());

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldThrowConflictWhenCpfAlreadyExistsOnCreate() {
        var request = new CustomerRequest(
                "Joao da Silva",
                "12345678901",
                "joao@email.com",
                CustomerStatus.ACTIVE
        );

        when(repo.existsByCpf("12345678901"))
                .thenReturn(true);

        var exception = assertThrows(
                ConflictException.class,
                () -> service.create(request)
        );

        assertEquals(
                "CPF already registered",
                exception.getMessage()
        );

        verify(repo).existsByCpf("12345678901");
        verify(repo, never()).save(any());

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldUpdateCustomer() {
        var request = new CustomerRequest(
                "Joao Atualizado",
                "98765432100",
                "novo@email.com",
                CustomerStatus.INACTIVE
        );

        when(repo.findById(1L))
                .thenReturn(Optional.of(customer));

        when(repo.existsByCpfAndIdNot("98765432100", 1L))
                .thenReturn(false);

        var response = service.update(1L, request);

        assertNotNull(response);

        assertEquals("Joao Atualizado", response.name());
        assertEquals("98765432100", response.cpf());
        assertEquals("novo@email.com", response.email());
        assertEquals(CustomerStatus.INACTIVE, response.status());

        assertEquals("Joao Atualizado", customer.getName());
        assertEquals("98765432100", customer.getCpf());
        assertEquals("novo@email.com", customer.getEmail());
        assertEquals(CustomerStatus.INACTIVE, customer.getStatus());

        verify(repo).findById(1L);

        verify(repo)
                .existsByCpfAndIdNot("98765432100", 1L);

        verify(repo, never()).save(any());

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistingCustomer() {
        var request = new CustomerRequest(
                "Joao",
                "12345678901",
                "joao@email.com",
                CustomerStatus.ACTIVE
        );

        when(repo.findById(99L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                NotFoundException.class,
                () -> service.update(99L, request)
        );

        assertEquals(
                "Customer not found: 99",
                exception.getMessage()
        );

        verify(repo).findById(99L);

        verify(repo, never())
                .existsByCpfAndIdNot(anyString(), anyLong());

        verify(repo, never()).save(any());

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldThrowConflictWhenCpfBelongsToAnotherCustomer() {
        var request = new CustomerRequest(
                "Joao Atualizado",
                "98765432100",
                "novo@email.com",
                CustomerStatus.ACTIVE
        );

        when(repo.findById(1L))
                .thenReturn(Optional.of(customer));

        when(repo.existsByCpfAndIdNot(
                "98765432100",
                1L
        )).thenReturn(true);

        var exception = assertThrows(
                ConflictException.class,
                () -> service.update(1L, request)
        );

        assertEquals(
                "CPF already registered",
                exception.getMessage()
        );

        assertEquals("Joao da Silva", customer.getName());
        assertEquals("12345678901", customer.getCpf());
        assertEquals("joao@email.com", customer.getEmail());
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());

        verify(repo).findById(1L);

        verify(repo)
                .existsByCpfAndIdNot(
                        "98765432100",
                        1L
                );

        verify(repo, never()).save(any());

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldDeleteCustomer() {
        when(repo.findById(1L))
                .thenReturn(Optional.of(customer));

        service.delete(1L);

        verify(repo).findById(1L);
        verify(repo).delete(customer);

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistingCustomer() {
        when(repo.findById(99L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                NotFoundException.class,
                () -> service.delete(99L)
        );

        assertEquals(
                "Customer not found: 99",
                exception.getMessage()
        );

        verify(repo).findById(99L);
        verify(repo, never()).delete(any());

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldGetCustomerById() {
        when(repo.findById(1L))
                .thenReturn(Optional.of(customer));

        var response = service.get(1L);

        assertNotNull(response);

        assertEquals("Joao da Silva", response.name());
        assertEquals("12345678901", response.cpf());
        assertEquals("joao@email.com", response.email());
        assertEquals(CustomerStatus.ACTIVE, response.status());

        verify(repo).findById(1L);

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldThrowNotFoundWhenCustomerDoesNotExist() {
        when(repo.findById(99L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                NotFoundException.class,
                () -> service.get(99L)
        );

        assertEquals(
                "Customer not found: 99",
                exception.getMessage()
        );

        verify(repo).findById(99L);

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldListAllCustomersWhenStatusIsNull() {
        var maria = new Customer(
                "Maria Silva",
                "98765432100",
                "maria@email.com",
                CustomerStatus.INACTIVE
        );

        when(repo.findAll())
                .thenReturn(List.of(customer, maria));

        var response = service.list(null);

        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(
                "Joao da Silva",
                response.get(0).name()
        );

        assertEquals(
                CustomerStatus.ACTIVE,
                response.get(0).status()
        );

        assertEquals(
                "Maria Silva",
                response.get(1).name()
        );

        assertEquals(
                CustomerStatus.INACTIVE,
                response.get(1).status()
        );

        verify(repo).findAll();

        verify(repo, never())
                .findByStatus(any());

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoCustomers() {
        when(repo.findAll())
                .thenReturn(List.of());

        var response = service.list(null);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(repo).findAll();

        verify(repo, never())
                .findByStatus(any());

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldListCustomersByActiveStatus() {
        when(repo.findByStatus(CustomerStatus.ACTIVE))
                .thenReturn(List.of(customer));

        var response =
                service.list(CustomerStatus.ACTIVE);

        assertNotNull(response);
        assertEquals(1, response.size());

        assertEquals(
                "Joao da Silva",
                response.get(0).name()
        );

        assertEquals(
                CustomerStatus.ACTIVE,
                response.get(0).status()
        );

        verify(repo)
                .findByStatus(CustomerStatus.ACTIVE);

        verify(repo, never()).findAll();

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldListCustomersByInactiveStatus() {
        var inactive = new Customer(
                "Maria Silva",
                "98765432100",
                "maria@email.com",
                CustomerStatus.INACTIVE
        );

        when(repo.findByStatus(CustomerStatus.INACTIVE))
                .thenReturn(List.of(inactive));

        var response =
                service.list(CustomerStatus.INACTIVE);

        assertNotNull(response);
        assertEquals(1, response.size());

        assertEquals(
                "Maria Silva",
                response.get(0).name()
        );

        assertEquals(
                CustomerStatus.INACTIVE,
                response.get(0).status()
        );

        verify(repo)
                .findByStatus(CustomerStatus.INACTIVE);

        verify(repo, never()).findAll();

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldReturnEmptyListWhenStatusHasNoCustomers() {
        when(repo.findByStatus(CustomerStatus.INACTIVE))
                .thenReturn(List.of());

        var response =
                service.list(CustomerStatus.INACTIVE);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(repo)
                .findByStatus(CustomerStatus.INACTIVE);

        verify(repo, never()).findAll();

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldSearchCustomersByName() {
        when(repo.searchByNameNative("joao"))
                .thenReturn(List.of(customer));

        var response = service.search("joao");

        assertNotNull(response);
        assertEquals(1, response.size());

        assertEquals(
                "Joao da Silva",
                response.get(0).name()
        );

        assertEquals(
                "12345678901",
                response.get(0).cpf()
        );

        assertEquals(
                "joao@email.com",
                response.get(0).email()
        );

        assertEquals(
                CustomerStatus.ACTIVE,
                response.get(0).status()
        );

        verify(repo)
                .searchByNameNative("joao");

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldReturnEmptyListWhenSearchFindsNothing() {
        when(repo.searchByNameNative("inexistente"))
                .thenReturn(List.of());

        var response =
                service.search("inexistente");

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(repo)
                .searchByNameNative("inexistente");

        verifyNoInteractions(jdbc, score);
    }

    @Test
    void shouldGetCustomerScore() {
        var scoreResponse = new ScoreResponse(
                "12345678901",
                750,
                "LOW_RISK"
        );

        when(repo.findById(1L))
                .thenReturn(Optional.of(customer));

        when(score.getScore("12345678901"))
                .thenReturn(scoreResponse);

        var response = service.score(1L);

        assertNotNull(response);

        assertEquals(
                "12345678901",
                response.cpf()
        );

        assertEquals(
                750,
                response.score()
        );

        assertEquals(
                "LOW_RISK",
                response.classification()
        );

        verify(repo).findById(1L);

        verify(score)
                .getScore("12345678901");

        verifyNoInteractions(jdbc);
    }

    @Test
    void shouldThrowNotFoundWhenGettingScoreForNonExistingCustomer() {
        when(repo.findById(99L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                NotFoundException.class,
                () -> service.score(99L)
        );

        assertEquals(
                "Customer not found: 99",
                exception.getMessage()
        );

        verify(repo).findById(99L);

        verifyNoInteractions(score, jdbc);
    }

    @Test
    void shouldCountCustomersByActiveStatus() {
        when(jdbc.countByStatus("ACTIVE"))
                .thenReturn(5L);

        long result =
                service.countByStatus(CustomerStatus.ACTIVE);

        assertEquals(5L, result);

        verify(jdbc)
                .countByStatus("ACTIVE");

        verifyNoInteractions(repo, score);
    }

    @Test
    void shouldCountCustomersByInactiveStatus() {
        when(jdbc.countByStatus("INACTIVE"))
                .thenReturn(3L);

        long result =
                service.countByStatus(CustomerStatus.INACTIVE);

        assertEquals(3L, result);

        verify(jdbc)
                .countByStatus("INACTIVE");

        verifyNoInteractions(repo, score);
    }

    @Test
    void shouldReturnZeroWhenNoCustomersExistForStatus() {
        when(jdbc.countByStatus("ACTIVE"))
                .thenReturn(0L);

        long result =
                service.countByStatus(CustomerStatus.ACTIVE);

        assertEquals(0L, result);

        verify(jdbc)
                .countByStatus("ACTIVE");

        verifyNoInteractions(repo, score);
    }
}