import { APP_INITIALIZER, Provider } from '@angular/core';
import { KeycloakService, KeycloakOptions } from 'keycloak-angular';
import { environment } from '../../environments/environment';

// Keycloak Configuration
export const keycloakConfig: KeycloakOptions = {
  config: {
    url: environment.authUrl,
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
