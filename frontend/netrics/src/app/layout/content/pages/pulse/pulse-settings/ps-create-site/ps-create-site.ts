import {Component, EventEmitter, Input, Output, signal} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AlertService} from '../../../../../../components/alert/alert.service';
import {RatService} from '../../../../../../service/pulse/rat-service';
import {AreaTypeService} from '../../../../../../service/pulse/area-type-service';
import {GranularityService} from '../../../../../../service/pulse/granularity-service';
import {DashboardService} from '../../../../../../service/pulse/dashboard/dashboard-service';
import {AuthService} from '../../../../../../auth/service/auth-service';
import {KeycloakProfile} from 'keycloak-js';
import {Router} from '@angular/router';
import {SiteService} from '../../../../../../service/pulse/site-service';
import {SiteDto} from '../../../../../../models/pulse/SiteDto';

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

  @Input() open: boolean = false;
  @Output() closed = new EventEmitter<void>();

  userProfile: KeycloakProfile = {};

  creatingSite = signal<boolean>(false);
  creatingSites = signal<boolean>(false);
  createMultipleSites = signal<boolean>(false);

  siteCode = signal<string>('');
  siteName = signal<string>('');
  siteListJson = signal<string>('');

  constructor(private alertService: AlertService,
              private authService: AuthService,
              private siteService: SiteService) {
  }

  onCancel(): void {
    this.createMultipleSites.set(false);
    this.closed.emit();
  }

  createSite(): void {
    this.creatingSite.set(true);
    const site: SiteDto = {siteCode: this.siteCode(), siteName: this.siteName()};

    this.siteService.createSite(site).subscribe({
      next: value => {
        this.alertService.success(`Site ${value.siteCode} created successfully.`);
        this.closed.emit();
        this.siteCode.set('');
        this.siteName.set('');
        this.creatingSite.set(false);
      }, error: err => {
        console.log(err);
        this.alertService.error(`Error creating Site ${site.siteCode} - ${err.statusText}`);
        this.creatingSite.set(false);
      }
    })
  }

  createSiteList(json:string){
    this.creatingSites.set(true);
    this.siteService.createSiteList(json).subscribe({
      next: value => {
        console.log(value);
        const createdCount = value.length;
        this.alertService.success(`${createdCount} sites created successfully.`);
        this.creatingSites.set(false);
        this.siteListJson.set('');
        this.closed.emit();
      }, error: err => {
        console.log(err);
        this.alertService.error(`Error creating Sites. - ${err.statusText}`);
      }
    });
  }

  isInputsValid() {
    // return !(this.rat() != '' && this.areaType() != '' && this.granularity() != '' && this.selectedDate() != '');
    return this.siteCode() != '' && this.siteCode().length > 5 && this.siteName() != '' && this.siteName().length > 2;
  }

  isJsonInputValid(){
    return this.siteListJson() != '';
  }

  //============== USER VALIDATION =============================

  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
    if (this.authService.hasRole('PULSE_CREATE')) {
      // this.startWorstCellCreationStatusPolling();
    }
  }

  hasAnyRole(roles: string[]) {
    return this.authService.hasAnyRole(roles);
  }

}
