import {Component, EventEmitter, Input, Output, signal} from '@angular/core';
import {MapCellThrSetAndThresholds} from '../../../../../../models/pulse/map-cell/MapCellThrSetAndThresholds';
import {MapCellThresholdDto} from '../../../../../../models/pulse/map-cell/MapCellThresholdDto';

@Component({
  selector: 'app-cell-map-legend',
  imports: [],
  templateUrl: './cell-map-legend.html',
  styleUrl: './cell-map-legend.css'
})
export class CellMapLegend {

  @Input() mapCellThrSetAndThresholds = signal<MapCellThrSetAndThresholds | undefined>(undefined);

  // @Input() standardKpiLabel = signal<string | undefined>(undefined);
  @Input() standardKpiLabel: string | undefined = undefined;
  @Input() isThresholdEditable:boolean = false;
  @Input() isThresholdsCustomizable:boolean = false;
  @Input() isAdmin: boolean = true;
  @Output() isAdminChange: EventEmitter<boolean> = new EventEmitter();
  @Output() openCellMapLegendEditModal: EventEmitter<void> = new EventEmitter();

  @Input() isThresholdAvailable = signal<boolean>(true);

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
}
