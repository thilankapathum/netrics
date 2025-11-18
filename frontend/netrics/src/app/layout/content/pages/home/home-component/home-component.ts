import {Component, signal} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../../../../../auth/service/auth-service';
import {KeycloakProfile} from 'keycloak-js';

@Component({
  selector: 'app-home-component',
  imports: [
    RouterLink
  ],
  templateUrl: './home-component.html',
  styleUrl: './home-component.css'
})
export class HomeComponent {

  userProfile = signal<KeycloakProfile | undefined>(undefined);
  userRoles = signal(undefined);

  constructor(private authService: AuthService, private router: Router) {
    this.getUserProfile();
  }

  getUserProfile(){
    this.authService.getUserProfile().then(userProfile => {
      this.userProfile.set(userProfile);
    })
  }

  hasRole(role:string){
    return this.authService.hasRole(role);
  }

  hasAnyRole(roles:string[]){
    return this.authService.hasAnyRole(roles);
  }

}
