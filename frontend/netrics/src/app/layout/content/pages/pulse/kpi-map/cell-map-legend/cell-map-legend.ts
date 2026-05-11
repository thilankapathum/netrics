import {Component, EventEmitter, Input, Output, signal} from '@angular/core';
import {MapCellThrSetAndThresholds} from '../../../../../../models/pulse/map-cell/MapCellThrSetAndThresholds';
import {MapCellThresholdDto} from '../../../../../../models/pulse/map-cell/MapCellThresholdDto';
import {FormsModule} from '@angular/forms';
import {BandDto} from '../../../../../../models/pulse/BandDto';

@Component({
  selector: 'app-cell-map-legend',
  imports: [
    FormsModule
  ],
  templateUrl: './cell-map-legend.html',
  styleUrl: './cell-map-legend.css'
})
export class CellMapLegend {

  @Input() mapCellThrSetAndThresholds = signal<MapCellThrSetAndThresholds | undefined>(undefined);
  @Input() standardKpiLabel: string | undefined = undefined;
  @Input() isThresholdEditable:boolean = false;
  @Input() isThresholdsCustomizable:boolean = false;
  @Input() isAdmin: boolean = true;
  @Input() isThresholdAvailable = signal<boolean>(true);
  @Input() bands: BandDto[] = [];

  @Output() isAdminChange: EventEmitter<boolean> = new EventEmitter();
  @Output() openCellMapLegendEditModal: EventEmitter<void> = new EventEmitter();
  @Output() isShowSiteLabelsChange: EventEmitter<boolean> = new EventEmitter();
  @Output() activeBandChange = new EventEmitter<string>();

  // @Output() selectedBand: EventEmitter<string> = new EventEmitter();
  // @Output() filteredByBands: EventEmitter<boolean> = new EventEmitter();

  showSiteLabels: boolean = true;
  filterByBands:boolean = false;
  selectedBand :string = '';

  get sortedThresholds(): MapCellThresholdDto[] {
    return this.mapCellThrSetAndThresholds()?.thresholds
      ?.slice().sort((a, b) => a.priority - b.priority) ?? [];
  }

  changeLegendType(isAdmin: boolean) {
    this.isAdminChange.emit(isAdmin);
  }

  createCellMapLegend() {
    this.openCellMapLegendEditModal.emit();
  }

  changeShowSiteLabels() {
    this.isShowSiteLabelsChange.emit(this.showSiteLabels);
  }

  onFilterByBandsToggle() {
    // filterByBands is already updated by [(ngModel)] at this point
    if (!this.filterByBands) {
      // User unchecked — reset band and notify parent
      this.selectedBand = '';
      this.activeBandChange.emit('');
    }
    // If checked, don't emit yet — wait for user to pick a band
  }

  onBandSelect(bandName: string) {
    this.selectedBand = bandName;
    this.activeBandChange.emit(bandName);
  }

  onBandClear() {
    this.selectedBand = '';
    this.activeBandChange.emit('');
  }

  // selectBand(bandName:string) {
  //   this._selectedBand = bandName;
  //   this.selectedBand.emit(this._selectedBand);
  //   console.log('selected band', this._selectedBand);
  // }
  //
  // changeFilterByBands() {
  //   if (!this.filterByBands) {
  //     this._selectedBand = '';              // reset local radio state
  //     this.selectedBand.emit('');           // tell parent band is cleared
  //   }
  //   this.filteredByBands.emit(this.filterByBands);
  // }
}
