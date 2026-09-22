package com.challenge.customers.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class CustomerJdbcRepository {
    private final JdbcTemplate jdbc;

    public CustomerJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long countByStatus(String status) {
        Long n = jdbc.queryForObject("select count(*) from customers where status = ?", Long.class, status);
        return n == null ? 0 : n;
    }
}