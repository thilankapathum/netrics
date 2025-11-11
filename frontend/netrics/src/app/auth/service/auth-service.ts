import { Injectable } from '@angular/core';
import {KeycloakService} from 'keycloak-angular';
import {from, Observable} from 'rxjs';
import {KeycloakProfile} from 'keycloak-js';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private keycloakService: KeycloakService) {}

  // Check if user is authenticated
  async isAuthenticated(): Promise<boolean> {
    return await this.keycloakService.isLoggedIn();
  }

  // Check if user is authenticated (Observable version)
  // isAuthenticated$(): Observable<boolean> {
  //   return from(this.keycloakService.isLoggedIn());
  // }

  // Get user profile
  async getUserProfile(): Promise<KeycloakProfile> {
    return await this.keycloakService.loadUserProfile();
  }

  // Get user profile (Observable version)
  getUserProfile$(): Observable<KeycloakProfile> {
    return from(this.keycloakService.loadUserProfile());
  }

  // Get username
  getUsername(): string {
    return this.keycloakService.getUsername();
  }

  // Get user roles
  getUserRoles(): string[] {
    return this.keycloakService.getUserRoles();
  }

  // Check if user has specific role
  hasRole(role: string): boolean {
    return this.keycloakService.isUserInRole(role);
  }

  // Check if user has any of the specified roles
  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  // Login
  login(redirectUri?: string): void {
    this.keycloakService.login({
      redirectUri: redirectUri || window.location.origin
    });
  }

  // Logout
  logout(redirectUri?: string): void {
    this.keycloakService.logout(redirectUri || window.location.origin);
  }

  // Get access token
  getToken(): Promise<string> {
    return this.keycloakService.getToken();
  }

  // Refresh token
  updateToken(minValidity: number = 5): Promise<boolean> {
    return this.keycloakService.updateToken(minValidity);
  }

  // Clear token
  clearToken(): void {
    this.keycloakService.clearToken();
  }

  // Check if token is expired
  isTokenExpired(minValidity: number = 0): boolean {
    return this.keycloakService.isTokenExpired(minValidity);
  }

  // Get token expiration time
  getTokenExpirationTime(): number | undefined {
    const token = this.keycloakService.getKeycloakInstance().tokenParsed;
    return token?.exp;
  }
}
