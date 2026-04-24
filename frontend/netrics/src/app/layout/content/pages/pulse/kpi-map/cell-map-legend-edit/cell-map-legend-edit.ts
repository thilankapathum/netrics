import {Component, EventEmitter, Input, Output, signal} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MapCellThresholdDto} from '../../../../../../models/pulse/map-cell/MapCellThresholdDto';
import {MapCellThrSetAndThresholds} from '../../../../../../models/pulse/map-cell/MapCellThrSetAndThresholds';

@Component({
  selector: 'app-cell-map-legend-edit',
    imports: [
        FormsModule,
        ReactiveFormsModule
    ],
  templateUrl: './cell-map-legend-edit.html',
  styleUrl: './cell-map-legend-edit.css'
})
export class CellMapLegendEdit {

  @Input() open: boolean = false;
  @Output() closed = new EventEmitter<void>();
  @Input() mapCellThrSetAndThresholds = signal<MapCellThrSetAndThresholds | undefined>(undefined);

  get sortedThresholds():MapCellThresholdDto[]{
    return this.mapCellThrSetAndThresholds()?.thresholds
      ?.slice().sort((a, b) => a.priority - b.priority) ?? [];
  }

  onCancel():void{
    this.closed.emit();
  }

}
