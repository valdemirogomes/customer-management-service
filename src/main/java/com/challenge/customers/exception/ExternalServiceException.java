package com.challenge.customers.exception;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String m) {
        super(m);
    }

    public ExternalServiceException(String m, Throwable t) {
        super(m, t);
    }
}