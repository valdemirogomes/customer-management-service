package com.challenge.customers.integration;

import com.challenge.customers.api.ScoreResponse;
import com.challenge.customers.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.time.Duration;

@Component
public class ScoreClient {
    private final RestClient client;

    public ScoreClient(RestClient.Builder b, @Value("${score-service.base-url}") String url, @Value("${score-service.connect-timeout:2s}") Duration connect, @Value("${score-service.read-timeout:3s}") Duration read) {
        var rf = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(connect);
        rf.setReadTimeout(read);
        this.client = b.requestFactory(rf).baseUrl(url).build();
    }

    public ScoreResponse getScore(String cpf) {
        try {
            ScoreResponse r = client.get().uri("/scores/{cpf}", cpf).retrieve().body(ScoreResponse.class);
            if (r == null || r.cpf() == null || r.score() == null || r.classification() == null)
                throw new ExternalServiceException("Unexpected response from score service");
            return r;
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException("Score service unavailable or timed out", e);
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException("Score service returned HTTP " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new ExternalServiceException("Failure communicating with score service", e);
        }
    }
}