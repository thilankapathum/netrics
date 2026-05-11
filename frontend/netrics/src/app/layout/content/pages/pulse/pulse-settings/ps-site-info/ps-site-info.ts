import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {SiteService} from '../../../../../../service/pulse/site-service';
import {AlertService} from '../../../../../../components/alert/alert.service';

@Component({
  selector: 'app-ps-site-info',
    imports: [
        FormsModule
    ],
  templateUrl: './ps-site-info.html',
  styleUrl: './ps-site-info.css'
})
export class PsSiteInfo {

  loadingMissingSiteInfoUpload: boolean = false;
  loadingMissingSiteInfoDownload: boolean = false;
  loadingAllSitesDownload: boolean = false;

  @Input() open: boolean = false;
  @Output() closed = new EventEmitter<void>();

  constructor(private siteService: SiteService,
              private alertService: AlertService) {
  }

  onCancel():void{
    this.closed.emit();
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
        console.log("Error exporting missing site information:");
        console.error(error);
        this.alertService.error("Error exporting missing Site Information!");
        this.loadingMissingSiteInfoDownload = false;
      }
    })
  }

  exportAllSites(){
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
        console.log("Error exporting site information:");
        console.error(error);
        this.alertService.error(`Error exporting site information! - ${error.statusText}`);
        this.loadingAllSitesDownload = false;
      }
    })
  }

  importSitesWithCorrectedInfo(fileInput: HTMLInputElement) {

    this.loadingMissingSiteInfoUpload = true;

    const files = fileInput.files;

    if (!files || files.length === 0) {
      this.alertService.warning('Please select a CSV file to upload.')
      this.loadingMissingSiteInfoUpload = false;
      return;
    }

    const file: File = files[0];

    // Optional: validate file type
    if (!file.name.endsWith('.csv')) {
      this.alertService.warning('Please upload a CSV file.')
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
        console.log("Error exporting missing site information:");
        console.error(error);
        this.loadingMissingSiteInfoUpload = false;
        this.alertService.error("Error exporting missing site information!");
        fileInput.value = '';
      }
    })
  }
}
