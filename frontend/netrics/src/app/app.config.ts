import {ApplicationConfig, importProvidersFrom, provideZoneChangeDetection} from '@angular/core';
import {provideRouter} from '@angular/router';

import {routes} from './app.routes';
import {provideClientHydration, withEventReplay} from '@angular/platform-browser';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {KeycloakAngularModule, KeycloakService} from 'keycloak-angular';
import {authInterceptor} from './auth/interceptor/auth.interceptor';
import {KeycloakInitializerProvider} from './auth/auth.config';

export const appConfig: ApplicationConfig = {
  providers: [provideZoneChangeDetection({eventCoalescing: true}),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideHttpClient(
      withInterceptors([authInterceptor]),
    ),
    importProvidersFrom(KeycloakAngularModule),
    KeycloakService,
    KeycloakInitializerProvider]
};

// export const appConfig: ApplicationConfig = {
//   providers: [
//     provideRouter(routes),
//     provideHttpClient(
//       withInterceptors([authInterceptor])
//     ),
//     importProvidersFrom(KeycloakAngularModule),
//     KeycloakService,
//     KeycloakInitializerProvider
//   ]
// };
//////////////////////////////////////


