import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {KeycloakService} from 'keycloak-angular';

export const authGuard: CanActivateFn = async (route, state) => {

  const keycloakService = inject(KeycloakService);
  const router = inject(Router);

  const isLoggedIn = await keycloakService.isLoggedIn();

  if (!isLoggedIn) {
    await keycloakService.login({
      redirectUri: window.location.origin + state.url
    });
    return false;
  }

  // Check for required roles
  const requiredRoles = route.data['roles'] as string[];
  if (requiredRoles && requiredRoles.length > 0) {
    const hasRequiredRole = requiredRoles.some(role =>
      keycloakService.getUserRoles().includes(role)
    );

    if (!hasRequiredRole) {
      router.navigate(['/unauthorized']);
      return false;
    }
  }
  return true;
};
