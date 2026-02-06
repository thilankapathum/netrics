import {Component, EventEmitter, Input, Output, signal} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {CellService} from '../../../../../../service/pulse/cell-service';
import {AlertService} from '../../../../../../components/alert/alert.service';

@Component({
  selector: 'app-ps-cells',
    imports: [
        FormsModule
    ],
  templateUrl: './ps-cells.html',
  styleUrl: './ps-cells.css'
})
export class PsCells {

  missingCellInfoCount = signal(0);

  loadingMissingCellInfoUpload: boolean = false;
  loadingMissingCellInfoDownload: boolean = false;
  loadingAllCellsDownload: boolean = false;

  @Input() open: boolean = false;
  @Output() closed = new EventEmitter<void>();

  constructor(private cellService: CellService,
              private alertService: AlertService,) {

    this.getCellCountWithMissingInfo();
  }

  onCancel():void{
    this.closed.emit();
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
        console.log("Error exporting missing cell information:");
        console.error(error);
        this.alertService.error("Error exporting missing cell information!");
        this.loadingMissingCellInfoDownload = false;
      }
    })
  }

  exportAllCells(){
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
        console.log("Error exporting cell information:");
        console.error(error);
        this.alertService.error(`Error exporting missing cell information! - ${error.statusText}`);
        this.loadingAllCellsDownload = false;
      }
    })
  }

  importCellsWithCorrectedInfo(fileInput: HTMLInputElement) {

    this.loadingMissingCellInfoUpload = true;

    const files = fileInput.files;

    if (!files || files.length === 0) {
      this.alertService.warning('Please select a CSV file to upload.')
      this.loadingMissingCellInfoUpload = false;
      return;
    }

    const file: File = files[0];

    // Optional: validate file type
    if (!file.name.endsWith('.csv')) {
      this.alertService.warning('Please upload a CSV file.')
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
        console.log("Error exporting missing cell information:");
        console.error(error);
        this.loadingMissingCellInfoUpload = false;
        this.alertService.error("Error exporting missing cell information!");
        fileInput.value = '';
      }
    })
  }

  getCellCountWithMissingInfo() {
    this.cellService.getCellCountWithMissingInfo().subscribe({
      next: data => {
        this.missingCellInfoCount.set(data);
      },
      error: error => {
        console.log("Error getCellCountWithMissingInfo:");
        console.error(error);
        this.alertService.error("Error retrieving Cell count with missing information");
      }
    })
  }

}
