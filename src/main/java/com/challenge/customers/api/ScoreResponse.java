package com.challenge.customers.api;

public record ScoreResponse(String cpf, Integer score, String classification) {
}