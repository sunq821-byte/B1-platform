package com.b1.module.ai.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Shared HTTP client for AI provider calls. Applies connect/read timeouts so a stalled
 * upstream cannot hang the analysis thread, and retries transient failures (network I/O,
 * 5xx) with linear backoff. Client errors (4xx) fail fast. When all attempts are exhausted
 * the last error is rethrown so the caller can degrade honestly instead of masking it.
 */
@Slf4j
@Component
public class AiHttpClient {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 1000L;
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 90_000;

    private final RestTemplate restTemplate;

    public AiHttpClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * POST a JSON body with bearer auth, retrying transient failures. Returns the response
     * body on success; throws the last transient error when all attempts are exhausted.
     */
    public String postJson(String url, String apiKey, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                return resp.getBody();
            } catch (ResourceAccessException | HttpServerErrorException e) {
                lastError = e;
                log.warn("AI HTTP transient failure (attempt {}/{}): {}", attempt, MAX_ATTEMPTS, e.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    backoff(BASE_BACKOFF_MS * attempt);
                }
            }
        }
        throw lastError;
    }

    private void backoff(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI retry interrupted", ie);
        }
    }
}
