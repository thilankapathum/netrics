package dev.thilanka.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class OAuth2ClientConfig {
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

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    @Bean
    public ReactiveClientRegistrationRepository reactiveClientRegistrationRepository() {
        ClientRegistration keycloakClient = ClientRegistration
                .withRegistrationId("keycloak")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                // Browser-facing authorization endpoint (external URL)
                .authorizationUri("http://" + domainIp + ":" + frontendPort + "/auth/realms/" + realmName + "/protocol/openid-connect/auth")
                // Internal token endpoint
                .tokenUri("http://" + keycloakHost + ":" + keycloakPort + "/auth/realms/" + realmName + "/protocol/openid-connect/token")
                // Internal userinfo endpoint
                .userInfoUri("http://" + keycloakHost + ":" + keycloakPort + "/auth/realms/" + realmName + "/protocol/openid-connect/userinfo")
                // Internal JWK Set endpoint
                .jwkSetUri("http://" + keycloakHost + ":" + keycloakPort + "/auth/realms/" + realmName + "/protocol/openid-connect/certs")
                // Use external issuer (what tokens will contain)
                .issuerUri("http://" + domainIp + ":" + frontendPort + "/auth/realms/" + realmName)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .userNameAttributeName("preferred_username")
                .clientName("Keycloak")
                .build();

        return new InMemoryReactiveClientRegistrationRepository(keycloakClient);
    }
}
