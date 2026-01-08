package dev.thilanka.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CustomJwtDecoderConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

//    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
//    private String issuerUri;

    @Value("${app.domain-ip}")
    private String domainIp;

    @Value("${app.frontend-port}")
    private String frontendPort;

    @Value("${app.realm-name}")
    private String realmName;

    @Value("${app.keycloak-host}")
    private String keycloakHost;

    @Value("${app.keycloak-port}")
    private String keycloakPort;

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(){

        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // Create a list of valid issuers
        List<String> validIssuers = Arrays.asList(
//                issuerUri,  // http://netrics.local:8000/auth/realms/netrics
                "http://" + keycloakHost + ":" + keycloakPort + "/auth/realms/" + realmName,  // Internal Docker issuer
                "http://" + domainIp + ":" + frontendPort + "/auth/realms/" + realmName  // External issuer
//                "http://localhost:8000/auth/realms/" + realmName  // Localhost issuer
        );

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();

        // Custom multi-issuer validator
        validators.add(new MultiIssuerValidator(validIssuers));
        validators.add(new JwtTimestampValidator());

//        validators.add(new JwtIssuerValidator(issuerUri));
//
//        validators.add(new JwtTimestampValidator());

        OAuth2TokenValidator<Jwt> delegatingValidator = new DelegatingOAuth2TokenValidator<>(validators);

        jwtDecoder.setJwtValidator(delegatingValidator);

        return jwtDecoder;
    }

    // Custom validator that accepts multiple issuers
    private static class MultiIssuerValidator implements OAuth2TokenValidator<Jwt> {
        private final List<String> validIssuers;

        public MultiIssuerValidator(List<String> validIssuers) {
            this.validIssuers = validIssuers;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            String issuer = jwt.getIssuer().toString();

            if (validIssuers.stream().anyMatch(validIssuer -> validIssuer.equals(issuer))) {
                return OAuth2TokenValidatorResult.success();
            }

            OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "The token's issuer claim '" + issuer + "' does not match any of the expected issuers: " + validIssuers,
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}
