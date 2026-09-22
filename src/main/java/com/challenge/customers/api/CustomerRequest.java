package com.challenge.customers.api;

import com.challenge.customers.domain.CustomerStatus;
import jakarta.validation.constraints.*;

public record CustomerRequest(@NotBlank @Size(max = 150) String name,
                              @NotBlank @Pattern(regexp = "\\d{11}", message = "cpf must contain 11 digits") String cpf,
                              @NotBlank @Email String email, @NotNull CustomerStatus status) {
}