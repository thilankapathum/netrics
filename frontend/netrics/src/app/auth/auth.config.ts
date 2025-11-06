import { APP_INITIALIZER, Provider } from '@angular/core';
import { KeycloakService, KeycloakOptions } from 'keycloak-angular';

// Keycloak Configuration
export const keycloakConfig: KeycloakOptions = {
  config: {
    // url: 'http://172.19.95.160:8000/auth',
    // url: 'http://keycloak:8080/auth',
    url: 'http://netrics.local:8000/auth',
    // url: 'http://localhost:8000/auth',
    realm: 'netrics',
    clientId: 'netrics-frontend'
  },
  initOptions: {
    onLoad: 'check-sso',
    silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
    checkLoginIframe: false,
    pkceMethod: 'S256'
  },
  bearerExcludedUrls: [
    '/assets',
    '/public'
  ]
};

// Keycloak Initializer Function
export function initializeKeycloak(keycloak: KeycloakService): () => Promise<boolean> {
  return () =>
    keycloak.init({
      config: keycloakConfig.config,
      initOptions: keycloakConfig.initOptions,
      bearerExcludedUrls: keycloakConfig.bearerExcludedUrls
    });
}

// Provider for APP_INITIALIZER
export const KeycloakInitializerProvider: Provider = {
  provide: APP_INITIALIZER,
  useFactory: initializeKeycloak,
  multi: true,
  deps: [KeycloakService]
};
