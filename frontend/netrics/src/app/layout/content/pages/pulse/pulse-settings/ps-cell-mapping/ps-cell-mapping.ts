import {Component, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {CellNameService} from '../../../../../../service/pulse/cell-name.service';
import {CellService} from '../../../../../../service/pulse/cell-service';
import {CellMappingService} from '../../../../../../service/pulse/cell-mapping-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {CellNameDto} from '../../../../../../models/pulse/CellNameDto';
import {CellDto} from '../../../../../../models/pulse/CellDto';
import {CellMappingDto} from '../../../../../../models/pulse/CellMappingDto';

@Component({
  selector: 'app-ps-cell-mapping',
  imports: [FormsModule],
  templateUrl: './ps-cell-mapping.html',
  styleUrl: './ps-cell-mapping.css'
})
export class PsCellMapping {
  previousCellQuery = signal('');
  newCellQuery = signal('');
  previousCellSuggestions = signal<CellNameDto[]>([]);
  newCellSuggestions = signal<CellNameDto[]>([]);

  previousCellName = signal<string | null>(null);
  newCellName = signal<string | null>(null);
  previousCellDetails = signal<CellDto | null>(null);

  mappings = signal<CellMappingDto[]>([]);
  submitting = signal(false);

  mappingPendingDelete = signal<CellMappingDto | null>(null);

  uploadingMappings = signal(false);

  private previousCellSearchTerms$ = new Subject<string>();
  private newCellSearchTerms$ = new Subject<string>();

  constructor(
    private cellNameService: CellNameService,
    private cellService: CellService,
    private cellMappingService: CellMappingService,
    private alertService: AlertService,
  ) {
    this.previousCellSearchTerms$.pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(term => this.cellNameService.searchCell(term).subscribe(data => this.previousCellSuggestions.set(data)));

    this.newCellSearchTerms$.pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(term => this.cellNameService.searchCell(term).subscribe(data => this.newCellSuggestions.set(data)));

    this.loadMappings();
  }

  onSearchPreviousCell(value: string) {
    this.previousCellQuery.set(value);
    this.previousCellName.set(null);
    this.previousCellDetails.set(null);
    if (value.length > 2) {
      this.previousCellSearchTerms$.next(value);
    } else {
      this.previousCellSuggestions.set([]);
    }
  }

  onSearchNewCell(value: string) {
    this.newCellQuery.set(value);
    this.newCellName.set(null);
    if (value.length > 2) {
      this.newCellSearchTerms$.next(value);
    } else {
      this.newCellSuggestions.set([]);
    }
  }

  selectPreviousCell(cell: CellNameDto) {
    this.previousCellQuery.set(cell.cellName ?? '');
    this.previousCellName.set(cell.cellName ?? null);
    this.previousCellSuggestions.set([]);

    this.cellService.getCellByName(cell.cellName!).subscribe({
      next: dto => this.previousCellDetails.set(dto),
      error: error => {
        console.error('Error fetching previous cell details:', error);
        this.alertService.error('Error retrieving previous cell details', 'Error', `${error.status} ${error.statusText}`);
      }
    });
  }

  selectNewCell(cell: CellNameDto) {
    this.newCellQuery.set(cell.cellName ?? '');
    this.newCellName.set(cell.cellName ?? null);
    this.newCellSuggestions.set([]);
  }

  confirmMapping() {
    const previousCellName = this.previousCellName();
    const newCellName = this.newCellName();

    if (!previousCellName || !newCellName) {
      this.alertService.warning('Select both a previous cell and a new cell before confirming.');
      return;
    }

    this.submitting.set(true);
    this.cellMappingService.create({previousCellName, newCellName}).subscribe({
      next: () => {
        this.alertService.success(`'${newCellName}' now replaces '${previousCellName}'.`);
        this.resetForm();
        this.loadMappings();
        this.submitting.set(false);
      },
      error: error => {
        console.error('Error creating cell mapping:', error);
        this.alertService.error('Error creating cell mapping', 'Error', `${error.status} ${error.statusText}`);
        this.submitting.set(false);
      }
    });
  }

  cancelPreview() {
    this.previousCellDetails.set(null);
    this.previousCellName.set(null);
    this.previousCellQuery.set('');
  }

  loadMappings() {
    this.cellMappingService.getAll().subscribe({
      next: data => this.mappings.set(data),
      error: error => {
        console.error('Error loading cell mappings:', error);
        this.alertService.error('Error loading cell mappings', 'Error', `${error.status} ${error.statusText}`);
      }
    });
  }

  requestDeleteMapping(mapping: CellMappingDto) {
    this.mappingPendingDelete.set(mapping);
  }

  cancelDeleteMapping() {
    this.mappingPendingDelete.set(null);
  }

  confirmDeleteMapping() {
    const mapping = this.mappingPendingDelete();
    if (!mapping?.id) {
      return;
    }
    this.cellMappingService.delete(mapping.id).subscribe({
      next: () => {
        this.alertService.success(`Mapping '${mapping.previousCellName}' → '${mapping.newCellName}' removed.`);
        this.mappingPendingDelete.set(null);
        this.loadMappings();
      },
      error: error => {
        console.error('Error deleting cell mapping:', error);
        this.alertService.error('Error deleting cell mapping', 'Error', `${error.status} ${error.statusText}`);
        this.mappingPendingDelete.set(null);
      }
    });
  }

  importCellMappings(fileInput: HTMLInputElement) {
    const files = fileInput.files;
    if (!files || files.length === 0) {
      this.alertService.warning('Please select a CSV file to upload.');
      return;
    }
    const file = files[0];
    if (!file.name.endsWith('.csv')) {
      this.alertService.warning('Please upload a CSV file.');
      return;
    }

    this.uploadingMappings.set(true);
    this.cellMappingService.importCellMappings(file).subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'cell_mapping_import_result.csv';
        a.click();
        window.URL.revokeObjectURL(downloadUrl);

        fileInput.value = '';
        this.uploadingMappings.set(false);
        this.loadMappings();
      },
      error: error => {
        console.error('Error uploading cell mappings:', error);
        this.alertService.error('Error uploading cell mappings', 'Error', `${error.status} ${error.statusText}`);
        this.uploadingMappings.set(false);
        fileInput.value = '';
      }
    });
  }

  private resetForm() {
    this.previousCellQuery.set('');
    this.newCellQuery.set('');
    this.previousCellName.set(null);
    this.newCellName.set(null);
    this.previousCellDetails.set(null);
  }
}
