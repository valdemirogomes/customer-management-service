package com.challenge.customers.repository;

import com.challenge.customers.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    List<Customer> findByStatus(CustomerStatus status);

    @Query(value = "select * from customers c where lower(c.name) like lower(concat('%', :name, '%')) order by c.name", nativeQuery = true)
    List<Customer> searchByNameNative(@Param("name") String name);
}