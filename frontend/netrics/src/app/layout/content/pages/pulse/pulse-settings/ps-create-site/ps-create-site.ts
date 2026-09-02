import { Component, signal } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { AlertService } from '../../../../../../components/alert/alert.service';
import { AuthService } from '../../../../../../auth/service/auth-service';
import { KeycloakProfile } from 'keycloak-js';
import { SiteService } from '../../../../../../service/pulse/site-service';
import { SiteDto } from '../../../../../../models/pulse/SiteDto';

@Component({
  selector: 'app-ps-create-site',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './ps-create-site.html',
  styleUrl: './ps-create-site.css'
})
export class PsCreateSite {

  userProfile: KeycloakProfile = {};

  creatingSite = signal<boolean>(false);
  creatingSites = signal<boolean>(false);
  createMultipleSites = signal<boolean>(false);

  siteCode = signal<string>('');
  siteName = signal<string>('');
  latitude = signal<number | undefined>(undefined);
  longitude = signal<number | undefined>(undefined);
  siteListJson = signal<string>('');

  constructor(private alertService: AlertService,
              private authService: AuthService,
              private siteService: SiteService) {
  }

  // Clears out the form inputs
  resetForm(): void {
    this.siteCode.set('');
    this.siteName.set('');
    this.latitude.set(undefined);
    this.longitude.set(undefined);
    this.siteListJson.set('');
  }

  createSite(): void {
    this.creatingSite.set(true);
    const site: SiteDto = {
      siteCode: this.siteCode(),
      siteName: this.siteName(),
      latitude: this.latitude(),
      longitude: this.longitude()
    };

    this.siteService.createSite(site).subscribe({
      next: value => {
        this.alertService.success(`Site ${value.siteCode} created successfully.`);
        this.resetForm(); // Wipe form clear upon success
        this.creatingSite.set(false);
      }, error: err => {
        console.log(err);
        this.alertService.error(`Error creating Site ${site.siteCode}`, 'Error', `${err.statusText}`);
        this.creatingSite.set(false);
      }
    })
  }

  createSiteList(json: string) {
    this.creatingSites.set(true);
    this.siteService.createSiteList(json).subscribe({
      next: value => {
        const createdCount = value.length;
        this.alertService.success(`${createdCount} sites created successfully.`);
        this.resetForm(); // Wipe JSON input clear upon success
        this.creatingSites.set(false);
      }, error: err => {
        console.log(err);
        this.alertService.error('Error creating Sites', 'Error', `${err.statusText}`);
        this.creatingSites.set(false);
      }
    });
  }

  isInputsValid() {
    return this.siteCode() != '' && this.siteCode().length > 5
      && this.siteName() != '' && this.siteName().length > 2
      && this.latitude() != undefined && this.longitude() != undefined
      && this.latitude()! > -90 && this.longitude()! > -180
      && this.latitude()! < 90 && this.longitude()! < 180;
  }

  isJsonInputValid() {
    return this.siteListJson() != '';
  }

  //============== USER VALIDATION =============================

  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
  }

  hasAnyRole(roles: string[]) {
    return this.authService.hasAnyRole(roles);
  }
}
