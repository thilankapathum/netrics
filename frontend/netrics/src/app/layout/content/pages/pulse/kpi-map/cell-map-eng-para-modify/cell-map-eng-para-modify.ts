import {Component, effect, EventEmitter, Input, Output, signal} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CellService} from '../../../../../../service/pulse/cell-service';
import {CellDto} from '../../../../../../models/pulse/CellDto';
import {AlertService} from '../../../../../../components/alert/alert.service';

@Component({
  selector: 'app-cell-map-eng-para-modify',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './cell-map-eng-para-modify.html',
  styleUrl: './cell-map-eng-para-modify.css'
})
export class CellMapEngParaModify {
  @Input() open = signal<boolean>(false);
  @Output() closed = new EventEmitter<void>();

  @Input() cellName: string = '';
  @Input() isEngineeringParaEditable: boolean = false;

  cell: CellDto = {cellName: ''};

  private initialized = false;

  savingCell: boolean = false;

  constructor(private cellService: CellService,
              private alertService: AlertService) {

    effect(() => {
      if (!this.open() || this.initialized) return;

      this.getCellInfo(this.cellName);
      this.initialized = true;
    });
  }

  getCellInfo(cellName: string): void {
    this.cellService.getCellByName(cellName).subscribe({
      next: data => {
        console.log('cellInfo', data);
        this.cell = data;
      }, error: err => {
        console.error(err);
        this.alertService.error(`Error finding cell by ${cellName}. ${err.statusCode} ${err.statusText}`);
      }
    })
  }

  onCancel(): void {
    this.initialized = false;
    this.closed.emit();
  }

  onSave(): void {
    if (this.isEngineeringParaEditable) {
      this.savingCell = true;
      this.cellService.updateCell(this.cell).subscribe({
        next: data => {
          this.alertService.success(`Updated cell ${this.cellName}`);
          this.cell = data;
          this.savingCell = false;
        }, error: err => {
          console.error(err);
          this.alertService.error(`Error updating cell ${this.cellName}. ${err.statusCode} ${err.statusText}`);
          this.savingCell = false;
        }
      })
    } else {
      this.alertService.error(`Unauthorized to update Cells`);
    }


  }

  isValid() {
    return this.cell.azimuth! < 360
      && this.cell.azimuth! >= 0
      && this.cell.beamwidth! <= 360
      && this.cell.beamwidth! > 0;
  }


}
