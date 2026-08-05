import {Component, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {SiteService} from '../../../../../../service/pulse/site-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {AuthService} from '../../../../../../auth/service/auth-service';
import {SiteDto} from '../../../../../../models/pulse/SiteDto';

@Component({
  selector: 'app-ps-site-management',
  imports: [
    FormsModule
  ],
  templateUrl: './ps-site-management.html',
  styleUrl: './ps-site-management.css'
})
export class PsSiteManagement {
// Loading states
  loadingMissingSiteInfoUpload: boolean = false;
  loadingMissingSiteInfoDownload: boolean = false;
  loadingAllSitesDownload: boolean = false;
  creatingSite = signal<boolean>(false);
  creatingSites = signal<boolean>(false);
  createMultipleSites = signal<boolean>(false);

  // Form signals
  siteCode = signal<string>('');
  siteName = signal<string>('');
  latitude = signal<number | undefined>(undefined);
  longitude = signal<number | undefined>(undefined);
  siteListJson = signal<string>('');

  constructor(private siteService: SiteService,
              private alertService: AlertService,
              private authService: AuthService) {
  }

  resetSingleSiteForm(): void {
    this.siteCode.set('');
    this.siteName.set('');
    this.latitude.set(undefined);
    this.longitude.set(undefined);
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
        this.resetSingleSiteForm();
        this.creatingSite.set(false);
      },
      error: err => {
        console.log(err);
        this.alertService.error(`Error creating Site ${site.siteCode} - ${err.statusText}`);
        this.creatingSite.set(false);
      }
    });
  }

  createSiteList(json: string) {
    this.creatingSites.set(true);
    this.siteService.createSiteList(json).subscribe({
      next: value => {
        const createdCount = value.length;
        this.alertService.success(`${createdCount} sites created successfully.`);
        this.siteListJson.set('');
        this.creatingSites.set(false);
      },
      error: err => {
        console.log(err);
        this.alertService.error(`Error creating Sites. - ${err.statusText}`);
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

  exportSitesWithMissingInfo() {
    this.loadingMissingSiteInfoDownload = true;
    this.siteService.exportSitesWithMissingInfo().subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'missing_site_info.csv';
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
        this.loadingMissingSiteInfoDownload = false;
      },
      error: error => {
        console.error(error);
        this.alertService.error("Error exporting missing Site Information!");
        this.loadingMissingSiteInfoDownload = false;
      }
    });
  }

  exportAllSites() {
    this.loadingAllSitesDownload = true;
    this.siteService.exportAllSites().subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'all_sites_info.csv';
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
        this.loadingAllSitesDownload = false;
      },
      error: error => {
        console.error(error);
        this.alertService.error(`Error exporting site information! - ${error.statusText}`);
        this.loadingAllSitesDownload = false;
      }
    });
  }

  importSitesWithCorrectedInfo(fileInput: HTMLInputElement) {
    this.loadingMissingSiteInfoUpload = true;
    const files = fileInput.files;

    if (!files || files.length === 0) {
      this.alertService.warning('Please select a CSV file to upload.');
      this.loadingMissingSiteInfoUpload = false;
      return;
    }

    const file: File = files[0];
    if (!file.name.endsWith('.csv')) {
      this.alertService.warning('Please upload a CSV file.');
      this.loadingMissingSiteInfoUpload = false;
      return;
    }

    this.siteService.importSitesWithCorrectedInfo(file).subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'missing_site_info.csv';
        a.click();
        window.URL.revokeObjectURL(downloadUrl);

        fileInput.value = '';
        this.loadingMissingSiteInfoUpload = false;
      },
      error: error => {
        console.error(error);
        this.loadingMissingSiteInfoUpload = false;
        this.alertService.error("Error exporting missing site information!");
        fileInput.value = '';
      }
    });
  }

  hasAnyRole(roles: string[]) {
    return this.authService.hasAnyRole(roles);
  }
}
