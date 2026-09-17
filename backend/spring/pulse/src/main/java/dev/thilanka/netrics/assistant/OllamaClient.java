package dev.thilanka.netrics.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import dev.thilanka.netrics.common.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal client for Ollama's OpenAI-compatible /v1/chat/completions (non-streaming). Returns the
 * "message" node, which holds either "tool_calls" (arguments as a JSON string) or plain "content".
 * Uses /v1 rather than the native /api/chat because the network path to the model host resets
 * POSTs to the native /api/* inference endpoints; /v1 also works with other OpenAI-compatible servers.
 */
@Component
public class OllamaClient {
    private final RestClient restClient;
    private final String model;

    public OllamaClient(@Value("${netrics.assistant.ollama-url:http://localhost:11434}") String ollamaUrl,
                        @Value("${netrics.assistant.model:qwen3:8b}") String model,
                        @Value("${netrics.assistant.timeout-seconds:60}") int timeoutSeconds) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder().baseUrl(ollamaUrl).requestFactory(factory).build();
        this.model = model;
    }

    public JsonNode chat(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", false);
        body.put("reasoning_effort", "none");   // qwen3: disable thinking
        body.put("temperature", 0);
        body.put("max_tokens", 512);
        if (tools != null) body.put("tools", tools);

        try {
            JsonNode response = restClient.post().uri("/v1/chat/completions").body(body).retrieve().body(JsonNode.class);
            return response == null ? MissingNode.getInstance() : response.path("choices").path(0).path("message");
        } catch (RestClientException e) {
            throw new ExternalServiceException("ollama", e);
        }
    }
}
