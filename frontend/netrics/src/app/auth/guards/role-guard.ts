import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {KeycloakService} from 'keycloak-angular';

export const roleGuard = (roles: string[]): CanActivateFn => {
  return async (route, state) => {
    const keycloakService = inject(KeycloakService);
    const router = inject(Router);

    const isLoggedIn = await keycloakService.isLoggedIn();

    if (!isLoggedIn) {
      await keycloakService.login({
        redirectUri: window.location.origin + state.url
      });
      return false;
    }

    const userRoles = keycloakService.getUserRoles();
    const hasRole = roles.some(role => userRoles.includes(role));

    if (!hasRole) {
      // await router.navigate(['/unauthorized']);
      // return false;
      let currentUrl = state.url;

      if (currentUrl.endsWith('/')) {
        currentUrl = currentUrl.slice(0, -1);
      }

      const unauthorizedUrl = `${currentUrl}/unauthorized`;

      return router.parseUrl(unauthorizedUrl);
    }
    return true;
  };
};
