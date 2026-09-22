package com.challenge.customers.api;

import com.challenge.customers.domain.CustomerStatus;
import com.challenge.customers.exception.ConflictException;
import com.challenge.customers.exception.ExternalServiceException;
import com.challenge.customers.exception.NotFoundException;
import com.challenge.customers.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService service;

    @Test
    void shouldCreateCustomer() throws Exception {
        var request = validRequest();

        var response = new CustomerResponse(
                1L,
                "João da Silva",
                "12345678901",
                "joao@email.com",
                CustomerStatus.ACTIVE
        );

        when(service.create(any(CustomerRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/customers/1"))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("João da Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(service).create(any(CustomerRequest.class));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        var json = """
                {
                    "name": "",
                    "cpf": "12345678901",
                    "email": "joao@email.com",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.path").value("/customers"))
                .andExpect(jsonPath("$.fields.name").exists());

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenNameIsGreaterThan150Characters() throws Exception {
        var json = """
                {
                    "name": "%s",
                    "cpf": "12345678901",
                    "email": "joao@email.com",
                    "status": "ACTIVE"
                }
                """.formatted("A".repeat(151));

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.name").exists());

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenCpfIsBlank() throws Exception {
        var json = """
                {
                    "name": "João da Silva",
                    "cpf": "",
                    "email": "joao@email.com",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.cpf").exists());

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenCpfHasLessThan11Digits() throws Exception {
        var json = """
                {
                    "name": "João da Silva",
                    "cpf": "1234567890",
                    "email": "joao@email.com",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.cpf")
                        .value("cpf must contain 11 digits"));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenCpfHasMoreThan11Digits() throws Exception {
        var json = """
                {
                    "name": "João da Silva",
                    "cpf": "123456789012",
                    "email": "joao@email.com",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.cpf")
                        .value("cpf must contain 11 digits"));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenCpfContainsLetters() throws Exception {
        var json = """
                {
                    "name": "João da Silva",
                    "cpf": "1234567890A",
                    "email": "joao@email.com",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.cpf")
                        .value("cpf must contain 11 digits"));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenEmailIsBlank() throws Exception {
        var json = """
                {
                    "name": "João da Silva",
                    "cpf": "12345678901",
                    "email": "",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists());

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        var json = """
                {
                    "name": "João da Silva",
                    "cpf": "12345678901",
                    "email": "email-invalido",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists());

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenStatusIsNull() throws Exception {
        var json = """
                {
                    "name": "João da Silva",
                    "cpf": "12345678901",
                    "email": "joao@email.com",
                    "status": null
                }
                """;

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.status").exists());

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenCreateRequestIsInvalid() throws Exception {
        var json = """
                {
                    "name": "",
                    "cpf": "123",
                    "email": "email-invalido",
                    "status": null
                }
                """;

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.path").value("/customers"))
                .andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.cpf").exists())
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.status").exists());

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn409WhenCpfAlreadyExists() throws Exception {
        var request = validRequest();

        when(service.create(any(CustomerRequest.class)))
                .thenThrow(new ConflictException("CPF already registered"));

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("CPF already registered"))
                .andExpect(jsonPath("$.path").value("/customers"));

        verify(service).create(any(CustomerRequest.class));
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        var request = new CustomerRequest(
                "João Atualizado",
                "12345678901",
                "novo@email.com",
                CustomerStatus.ACTIVE
        );

        var response = new CustomerResponse(
                1L,
                "João Atualizado",
                "12345678901",
                "novo@email.com",
                CustomerStatus.ACTIVE
        );

        when(service.update(eq(1L), any(CustomerRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/customers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("João Atualizado"))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.email").value("novo@email.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(service)
                .update(eq(1L), any(CustomerRequest.class));
    }

    @Test
    void shouldReturn400WhenUpdateRequestIsInvalid() throws Exception {
        var json = """
                {
                    "name": "",
                    "cpf": "",
                    "email": "invalido",
                    "status": null
                }
                """;

        mockMvc.perform(put("/customers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request"));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingCustomer()
            throws Exception {

        var request = validRequest();

        when(service.update(eq(999L), any(CustomerRequest.class)))
                .thenThrow(new NotFoundException("Customer not found"));

        mockMvc.perform(put("/customers/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Customer not found"))
                .andExpect(jsonPath("$.path")
                        .value("/customers/999"));

        verify(service)
                .update(eq(999L), any(CustomerRequest.class));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/customers/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).delete(1L);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingCustomer()
            throws Exception {

        doThrow(new NotFoundException("Customer not found"))
                .when(service)
                .delete(999L);

        mockMvc.perform(delete("/customers/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Customer not found"))
                .andExpect(jsonPath("$.path")
                        .value("/customers/999"));

        verify(service).delete(999L);
    }

    @Test
    void shouldGetCustomerById() throws Exception {
        var response = new CustomerResponse(
                1L,
                "João da Silva",
                "12345678901",
                "joao@email.com",
                CustomerStatus.ACTIVE
        );

        when(service.get(1L))
                .thenReturn(response);

        mockMvc.perform(get("/customers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("João da Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(service).get(1L);
    }

    @Test
    void shouldReturn404WhenCustomerDoesNotExist()
            throws Exception {

        when(service.get(999L))
                .thenThrow(new NotFoundException("Customer not found"));

        mockMvc.perform(get("/customers/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Customer not found"))
                .andExpect(jsonPath("$.path")
                        .value("/customers/999"));

        verify(service).get(999L);
    }

    @Test
    void shouldListAllCustomers() throws Exception {
        var customer1 = new CustomerResponse(
                1L,
                "João",
                "12345678901",
                "joao@email.com",
                CustomerStatus.ACTIVE
        );

        var customer2 = new CustomerResponse(
                2L,
                "Maria",
                "98765432100",
                "maria@email.com",
                CustomerStatus.INACTIVE
        );

        when(service.list(null))
                .thenReturn(List.of(customer1, customer2));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("João"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Maria"))
                .andExpect(jsonPath("$[1].status").value("INACTIVE"));

        verify(service).list(null);
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoCustomers()
            throws Exception {

        when(service.list(null))
                .thenReturn(List.of());

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(service).list(null);
    }

    @Test
    void shouldListCustomersByStatus() throws Exception {
        var response = new CustomerResponse(
                1L,
                "João",
                "12345678901",
                "joao@email.com",
                CustomerStatus.ACTIVE
        );

        when(service.list(CustomerStatus.ACTIVE))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/customers")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("João"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(service).list(CustomerStatus.ACTIVE);
    }

    @Test
    void shouldSearchCustomersByName() throws Exception {
        var response = new CustomerResponse(
                1L,
                "João da Silva",
                "12345678901",
                "joao@email.com",
                CustomerStatus.ACTIVE
        );

        when(service.search("joao"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/customers/search")
                        .param("name", "joao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("João da Silva"));

        verify(service).search("joao");
    }

    @Test
    void shouldReturnEmptyListWhenSearchDoesNotFindCustomer()
            throws Exception {

        when(service.search("inexistente"))
                .thenReturn(List.of());

        mockMvc.perform(get("/customers/search")
                        .param("name", "inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(service).search("inexistente");
    }

    @Test
    void shouldReturn400WhenSearchNameIsMissing()
            throws Exception {

        mockMvc.perform(get("/customers/search"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldGetCustomerScore() throws Exception {
        var response = new ScoreResponse(
                "12345678901",
                750,
                "LOW_RISK"
        );

        when(service.score(1L))
                .thenReturn(response);

        mockMvc.perform(get("/customers/{id}/score", 1L))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cpf")
                        .value("12345678901"))
                .andExpect(jsonPath("$.score")
                        .value(750))
                .andExpect(jsonPath("$.classification")
                        .value("LOW_RISK"));

        verify(service).score(1L);
    }

    @Test
    void shouldReturn404WhenCustomerDoesNotExistForScore()
            throws Exception {

        when(service.score(999L))
                .thenThrow(new NotFoundException("Customer not found"));

        mockMvc.perform(get("/customers/{id}/score", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Customer not found"))
                .andExpect(jsonPath("$.path")
                        .value("/customers/999/score"));

        verify(service).score(999L);
    }

    @Test
    void shouldReturn502WhenScoreServiceFails()
            throws Exception {

        when(service.score(1L))
                .thenThrow(new ExternalServiceException(
                        "Score service unavailable"
                ));

        mockMvc.perform(get("/customers/{id}/score", 1L))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error").value("Bad Gateway"))
                .andExpect(jsonPath("$.message")
                        .value("Score service unavailable"))
                .andExpect(jsonPath("$.path")
                        .value("/customers/1/score"));

        verify(service).score(1L);
    }

    private CustomerRequest validRequest() {
        return new CustomerRequest(
                "João da Silva",
                "12345678901",
                "joao@email.com",
                CustomerStatus.ACTIVE
        );
    }
}