import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { SectorService } from '../../../../../../service/pulse/sector-service';
import { AlertService } from '../../../../../../components/alert/alert.service';

@Component({
  selector: 'app-ps-sector-info',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './ps-sector-info.html',
  styleUrl: './ps-sector-info.css'
})
export class PsSectorInfo {

  missingSectorInfoCount = signal<number>(0);

  loadingMissingSectorInfoUpload: boolean = false;
  loadingMissingSectorInfoDownload: boolean = false;
  loadingAllSectorDownload: boolean = false;

  @Input() open: boolean = false;
  @Output() closed = new EventEmitter<void>();

  onCancel(): void {
    this.closed.emit();
  }

  constructor(private sectorService: SectorService,
              private alertService: AlertService) {
    this.getSectorCountWithMissingInfo();
  }

  exportSectorsWithMissingInfo() {
    this.loadingMissingSectorInfoDownload = true;
    this.sectorService.exportSectorsWithMissingInfo().subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'missing_sector_info.csv';
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
        this.loadingMissingSectorInfoDownload = false;
      },
      error: error => {
        console.log("Error exporting missing sector information:");
        console.error(error);
        this.alertService.error("Error exporting missing sector information!");
        this.loadingMissingSectorInfoDownload = false;
      }
    });
  }

  exportAllSectors() {
    this.loadingAllSectorDownload = true;
    this.sectorService.exportAllSectors().subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'all_sector_info.csv';
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
        this.loadingAllSectorDownload = false;
      },
      error: error => {
        console.log("Error exporting sector information:");
        console.error(error);
        this.alertService.error('Error exporting missing sector information', 'Error', `${error.statusText}`);
        this.loadingAllSectorDownload = false;
      }
    });
  }

  importSectorsWithCorrectedInfo(fileInput: HTMLInputElement) {
    this.loadingMissingSectorInfoUpload = true;
    const files = fileInput.files;

    if (!files || files.length === 0) {
      this.alertService.warning('Please select a CSV file to upload.');
      this.loadingMissingSectorInfoUpload = false;
      return;
    }

    const file: File = files[0];
    if (!file.name.endsWith('.csv')) {
      this.alertService.warning('Please upload a CSV file.');
      this.loadingMissingSectorInfoUpload = false;
      return;
    }

    this.sectorService.importSectorsWithCorrectedInfo(file).subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'missing_sector_info.csv';
        a.click();
        window.URL.revokeObjectURL(downloadUrl);

        fileInput.value = '';
        this.loadingMissingSectorInfoUpload = false;
        this.getSectorCountWithMissingInfo();
      },
      error: error => {
        console.log("Error exporting missing sector information:");
        console.error(error);
        this.loadingMissingSectorInfoUpload = false;
        this.alertService.error("Error exporting missing sector information!");
        fileInput.value = '';
      }
    });
  }

  getSectorCountWithMissingInfo() {
    this.sectorService.getSectorCountWithMissingInfo().subscribe({
      next: data => {
        this.missingSectorInfoCount.set(data);
      },
      error: error => {
        console.log("Error getSectorCountWithMissingInfo:");
        console.error(error);
        this.alertService.error("Error retrieving Sector count with missing information");
      }
    });
  }
}
