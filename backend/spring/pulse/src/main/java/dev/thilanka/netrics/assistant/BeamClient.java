package dev.thilanka.netrics.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Reads beam engineering parameters on behalf of the current user: the caller's JWT is relayed, so
 * beam's own ROLE_BEAM_READ check applies. The body is passed through as JSON (no copied beam DTOs).
 */
@Slf4j
@Component
public class BeamClient {
    private final RestClient restClient;

    public BeamClient(@LoadBalanced RestClient.Builder builder,
                      @Value("${netrics.assistant.beam-url:http://beam-service}") String beamUrl) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = builder.clone().baseUrl(beamUrl).requestFactory(factory).build();
    }

    /** @return site -> sectors -> antennas -> electrical tilts, or null when beam is unavailable or denies access. */
    public JsonNode siteEngineeringSummary(String siteCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) return null;
        try {
            return restClient.get()
                    .uri(uri -> uri.path("/api/v1/beam/sites/engineering-summary").queryParam("siteCode", siteCode).build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            log.warn("Beam engineering summary unavailable for site {}: {}", siteCode, e.getMessage());
            return null;
        }
    }
}
