package dev.thilanka.beam.common;

import dev.thilanka.beam.common.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.List;

/**
 * Reads pulse data on behalf of the current user: the caller's JWT is relayed, so pulse's own role check applies.
 * Mirrors pulse's BeamClient. Resolves pulse through LoadBalancerClient rather than a @LoadBalanced
 * RestClient.Builder bean: on this Spring Cloud version the Eureka client registers through the
 * RestClient.Builder bean, and a load-balanced one breaks registration.
 */
@Component
public class PulseClient {
    private final RestClient restClient;
    private final LoadBalancerClient loadBalancer;
    private final String pulseServiceId;

    public PulseClient(LoadBalancerClient loadBalancer,
                       @Value("${netrics.pulse-service-id:pulse-service}") String pulseServiceId) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.loadBalancer = loadBalancer;
        this.pulseServiceId = pulseServiceId;
    }

    /** @return names of pulse cells on the sector; throws when pulse can't answer, so callers fail closed. */
    public List<String> sectorCellNames(String sectorName) {
        return getStrings("/api/v1/pulse/sectors/{name}/cells", sectorName);
    }

    /** @return codes of the sites with a cell in the pulse area; throws when pulse can't answer. */
    public List<String> areaSiteCodes(String areaName) {
        return getStrings("/api/v1/pulse/areas/{name}/site-codes", areaName);
    }

    private List<String> getStrings(String path, Object... uriVariables) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new ExternalServiceException("pulse-service", "no user token to relay");
        }
        ServiceInstance pulse = loadBalancer.choose(pulseServiceId);
        if (pulse == null) {
            throw new ExternalServiceException("pulse-service", "no instance registered");
        }
        try {
            List<String> body = restClient.get()
                    .uri(pulse.getUri() + path, uriVariables)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (body == null) throw new ExternalServiceException("pulse-service", "empty response from " + path);
            return body;
        } catch (RestClientException e) {
            throw new ExternalServiceException("pulse-service", e);
        }
    }
}
