package com.challenge.customers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerIntegrationTest {
    @Autowired
    MockMvc mvc;
    String body = "{\"name\":\"Joao Silva\",\"cpf\":\"12345678901\",\"email\":\"joao@email.com\",\"status\":\"ACTIVE\"}";

    @Test
    void adminCanCreate() throws Exception {
        mvc.perform(post("/customers").with(httpBasic("admin", "admin123")).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
    }

    @Test
    void userCannotCreate() throws Exception {
        mvc.perform(post("/customers").with(httpBasic("user", "user123")).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedIs401() throws Exception {
        mvc.perform(get("/customers")).andExpect(status().isUnauthorized());
    }
}