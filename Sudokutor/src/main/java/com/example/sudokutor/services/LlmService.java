package com.example.sudokutor.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private static final int MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 20_000;

    private final RestClient restClient;

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.model:meta-llama/llama-3.3-70b-instruct:free}")
    private String model;

    public LlmService() {
        this.restClient = RestClient.create();
    }

    public String generate(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Map<?, ?> response = restClient.post()
                        .uri("https://openrouter.ai/api/v1/chat/completions")
                        .header("Authorization", "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(Map.class);

                System.out.println("OpenRouter response: " + response);

                List<?> choices = (List<?>) response.get("choices");
                Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                String finishReason = (String) choice.get("finish_reason");
                Map<?, ?> message = (Map<?, ?>) choice.get("message");
                String content = (String) message.get("content");

                if (content == null) {
                    throw new IllegalStateException("LLM returned null content. finish_reason=" + finishReason + " full response=" + response);
                }
                return content;

            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt == MAX_RETRIES) throw e;
                long retryAfterMs = parseRetryAfterMs(e);
                System.out.printf("Rate limited. Retrying in %dms (attempt %d/%d)%n",
                        retryAfterMs, attempt, MAX_RETRIES);
                sleep(retryAfterMs);
            }
        }

        throw new IllegalStateException("LLM request failed after " + MAX_RETRIES + " attempts");
    }

    private long parseRetryAfterMs(HttpClientErrorException e) {
        try {
            String body = e.getResponseBodyAsString();
            int idx = body.indexOf("retry_after_seconds\":");
            if (idx == -1) return DEFAULT_RETRY_DELAY_MS;
            String after = body.substring(idx + 21).replaceAll("[^0-9.]", "").split("\\.")[0];
            return (Long.parseLong(after) + 2) * 1000L;
        } catch (Exception ignored) {
            return DEFAULT_RETRY_DELAY_MS;
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
