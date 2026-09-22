package com.challenge.customers.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<ApiError> out(HttpStatus s, String m, HttpServletRequest r, Map<String, String> f) {
        return ResponseEntity.status(s).body(new ApiError(Instant.now(), s.value(), s.getReasonPhrase(), m, r.getRequestURI(), f));
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> nf(NotFoundException e, HttpServletRequest r) {
        return out(HttpStatus.NOT_FOUND, e.getMessage(), r, Map.of());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> cf(ConflictException e, HttpServletRequest r) {
        return out(HttpStatus.CONFLICT, e.getMessage(), r, Map.of());
    }

    @ExceptionHandler(ExternalServiceException.class)
    ResponseEntity<ApiError> ex(ExternalServiceException e, HttpServletRequest r) {
        return out(HttpStatus.BAD_GATEWAY, e.getMessage(), r, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> val(MethodArgumentNotValidException e, HttpServletRequest r) {
        Map<String, String> f = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(x -> f.putIfAbsent(x.getField(), x.getDefaultMessage()));
        return out(HttpStatus.BAD_REQUEST, "Invalid request", r, f);
    }
}