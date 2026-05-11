package dev.thilanka.netrics.config;

import dev.thilanka.netrics.common.security.KeycloakJwtGrantedAuthoritiesConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${keycloak.realm-name}")
    private String realmName;

    @Value("${keycloak.domain-ip}")
    private String domainIp;

    @Value("${keycloak.nginx-port}")
    private String nginxPort;

    @Value("${keycloak.keycloak-host}")
    private String keycloakHost;

    @Value("${keycloak.keycloak-port}")
    private String keycloakPort;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                // Public endpoints
                                .requestMatchers("/actuator/health", "/actuator/info", "/api/v1/pulse/cache/**").permitAll()

                                // Role-based access (example)
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")

                                // All other requests need authentication
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

//    @Bean
//    public JwtAuthenticationConverter jwtAuthenticationConverter() {
//        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
//        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
//        return converter;
//    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtGrantedAuthoritiesConverter());
        return converter;
    }

//    @Bean
//    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
//        return jwt -> {
//            // Extract realm roles
//            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
//            Collection<GrantedAuthority> realmRoles = List.of();
//
//            if (realmAccess != null && realmAccess.containsKey("roles")) {
//                @SuppressWarnings("unchecked")
//                List<String> roles = (List<String>) realmAccess.get("roles");
//                realmRoles = roles.stream()
//                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
//                        .collect(Collectors.toList());
//            }
//
//            // Extract resource roles (client-specific)
//            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
//            Collection<GrantedAuthority> resourceRoles = List.of();
//
//            if (resourceAccess != null && resourceAccess.containsKey("pulse-service")) {
//                @SuppressWarnings("unchecked")
//                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("pulse-service");
//                if (clientAccess.containsKey("roles")) {
//                    @SuppressWarnings("unchecked")
//                    List<String> roles = (List<String>) clientAccess.get("roles");
//                    resourceRoles = roles.stream()
//                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
//                            .collect(Collectors.toList());
//                }
//            }
//
//            // Extract scope authorities
//            JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
//            Collection<GrantedAuthority> scopeAuthorities = scopesConverter.convert(jwt);
//
//            // Combine all authorities
//            return Stream.concat(
//                    Stream.concat(realmRoles.stream(), resourceRoles.stream()),
//                    scopeAuthorities != null ? scopeAuthorities.stream() : Stream.empty()
//            ).collect(Collectors.toSet());
//        };
//    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri("http://" + keycloakHost + ":" + keycloakPort + "/auth/realms/" + realmName + "/protocol/openid-connect/certs")
                .build();

        // Create validator that accepts both public and internal issuer
        List<String> acceptedIssuers = Arrays.asList(
                "http://" + domainIp + ":" + nginxPort + "/auth/realms/" + realmName,  // Public
                "http://" + keycloakHost + ":" + keycloakPort + "/auth/realms/" + realmName        // Internal
        );

        OAuth2TokenValidator<Jwt> issuerValidator =
                new JwtClaimValidator<String>("iss", iss -> acceptedIssuers.contains(iss));

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                issuerValidator,
                new JwtTimestampValidator()
        );

        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }
}
