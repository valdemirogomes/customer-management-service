package com.challenge.customers.api;

import com.challenge.customers.domain.CustomerStatus;
import com.challenge.customers.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService s) {
        service = s;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest q) {
        var r = service.create(q);
        return ResponseEntity.created(URI.create("/customers/" + r.id())).body(r);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest q) {
        return service.update(id, q);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<CustomerResponse> list(@RequestParam(required = false) CustomerStatus status) {
        return service.list(status);
    }

    @GetMapping("/search")
    public List<CustomerResponse> search(@RequestParam String name) {
        return service.search(name);
    }

    @GetMapping("/{id}/score")
    public ScoreResponse score(@PathVariable Long id) {
        return service.score(id);
    }
}