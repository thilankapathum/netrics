import {Component, OnInit, Renderer2, Inject, Injector, signal} from '@angular/core';
import { DOCUMENT } from '@angular/common';
import {DaisyUiThemeService} from '../../service/components/theme/daisy-ui-theme.service';
import {RouterLink} from "@angular/router";
import {AuthService} from '../../auth/service/auth-service';
import {KeycloakProfile} from 'keycloak-js';
import {AlertService} from '../../components/alert/alert.service';
import {UrlService} from '../../service/url/url-service';

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.html',
    imports: [
        RouterLink
    ],
    styleUrl: './navbar.css'
})
export class Navbar implements OnInit {
  isDarkMode = false;
  userProfile = signal<KeycloakProfile | undefined>(undefined);
  authUrl:string = '';

  constructor(
    private renderer: Renderer2,
    @Inject(DOCUMENT) private document: Document,
    private themeService:DaisyUiThemeService,
    private authService:AuthService,
    private alertService:AlertService,
    private urlService:UrlService
  ) {}

  ngOnInit() {
    // Load saved theme preference or default to light
    const savedTheme = localStorage.getItem('theme') || 'netrics_light';
    this.isDarkMode = savedTheme === 'netrics_dark';
    // this.applyTheme(savedTheme);
    this.themeService.setTheme(savedTheme);
    this.getUserProfile()
    this.authUrl = this.urlService.getAuthUrl();
  }

  onThemeToggle(event: Event) {
    const checkbox = event.target as HTMLInputElement;
    this.isDarkMode = checkbox.checked;
    const theme = this.isDarkMode ? 'netrics_dark' : 'netrics_light';
    this.themeService.setTheme(theme);

    // this.applyTheme(theme);
    // this.saveThemePreference(theme);
  }

  private applyTheme(theme: string) {
    // Set the data-theme attribute on the html element
    this.renderer.setAttribute(this.document.documentElement, 'data-theme', theme);

    // Also update any theme-controller checkboxes to stay in sync
    const themeControllers = this.document.querySelectorAll('.theme-controller') as NodeListOf<HTMLInputElement>;
    themeControllers.forEach(controller => {
      controller.checked = theme === 'netrics_dark';
    });
  }

  private saveThemePreference(theme: string) {
    localStorage.setItem('theme', theme);
  }

  logout(){
    this.authService.logout();
  }

  getUserProfile(){
    this.authService.getUserProfile().then(userProfile => {
      this.userProfile.set(userProfile);
    })
  }
}
