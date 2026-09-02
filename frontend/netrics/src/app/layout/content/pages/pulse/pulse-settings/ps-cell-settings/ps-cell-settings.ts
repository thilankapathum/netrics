import {Component, signal} from '@angular/core';
import {CellService} from '../../../../../../service/pulse/cell-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-ps-cell-settings',
  imports: [FormsModule],
  templateUrl: './ps-cell-settings.html',
  styleUrl: './ps-cell-settings.css'
})
export class PsCellSettings {
  missingCellInfoCount = signal(0);

  loadingMissingCellInfoUpload: boolean = false;
  loadingMissingCellInfoDownload: boolean = false;
  loadingAllCellsDownload: boolean = false;

  constructor(
    private cellService: CellService,
    private alertService: AlertService,
  ) {
    this.getCellCountWithMissingInfo();
  }

  exportCellsWithMissingInfo() {
    this.loadingMissingCellInfoDownload = true;
    this.cellService.exportCellsWithMissingInfo().subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'missing_cell_info.csv';
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
        this.loadingMissingCellInfoDownload = false;
      },
      error: error => {
        console.error("Error exporting missing cell information:", error);
        this.alertService.error('Error exporting missing cell information', 'Error', `${error.status} ${error.statusText}`);
        this.loadingMissingCellInfoDownload = false;
      }
    });
  }

  exportAllCells() {
    this.loadingAllCellsDownload = true;
    this.cellService.exportAllCells().subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'all_cell_info.csv';
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
        this.loadingAllCellsDownload = false;
      },
      error: error => {
        console.error("Error exporting cell information:", error);
        this.alertService.error('Error exporting missing cell information', 'Error', `${error.status} ${error.statusText}`);
        this.loadingAllCellsDownload = false;
      }
    });
  }

  importCellsWithCorrectedInfo(fileInput: HTMLInputElement) {
    this.loadingMissingCellInfoUpload = true;
    const files = fileInput.files;

    if (!files || files.length === 0) {
      this.alertService.warning('Please select a CSV file to upload.');
      this.loadingMissingCellInfoUpload = false;
      return;
    }

    const file: File = files[0];

    if (!file.name.endsWith('.csv')) {
      this.alertService.warning('Please upload a CSV file.');
      this.loadingMissingCellInfoUpload = false;
      return;
    }

    this.cellService.importCellsWithCorrectedInfo(file).subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'missing_cell_info.csv';
        a.click();
        window.URL.revokeObjectURL(downloadUrl);

        fileInput.value = '';
        this.loadingMissingCellInfoUpload = false;
      },
      error: error => {
        console.error("Error exporting missing cell information:", error);
        this.loadingMissingCellInfoUpload = false;
        this.alertService.error('Error exporting missing cell information', 'Error', `${error.status} ${error.statusText}`);
        fileInput.value = '';
      }
    });
  }

  getCellCountWithMissingInfo() {
    this.cellService.getCellCountWithMissingInfo().subscribe({
      next: data => {
        this.missingCellInfoCount.set(data);
      },
      error: error => {
        console.error("Error getCellCountWithMissingInfo:", error);
        this.alertService.error('Error retrieving Cell count with missing information', 'Error', `${error.status} ${error.statusText}`);
      }
    });
  }
}
