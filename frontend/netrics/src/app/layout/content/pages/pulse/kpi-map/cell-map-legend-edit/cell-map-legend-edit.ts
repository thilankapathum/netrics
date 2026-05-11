import {
  Component,
  computed,
  effect,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  signal,
  SimpleChanges
} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MapCellThresholdDto} from '../../../../../../models/pulse/map-cell/MapCellThresholdDto';
import {MapCellThrSetAndThresholds} from '../../../../../../models/pulse/map-cell/MapCellThrSetAndThresholds';
import {MapCellThrSetResponseDto} from '../../../../../../models/pulse/map-cell/MapCellThrSetResponseDto';
import {AlertService} from '../../../../../../components/alert/alert.service';

@Component({
  selector: 'app-cell-map-legend-edit',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './cell-map-legend-edit.html',
  styleUrl: './cell-map-legend-edit.css'
})
export class CellMapLegendEdit implements OnInit, OnChanges {

  @Input() open = signal<boolean>(false);
  @Output() closed = new EventEmitter<void>();
  @Input() mapCellThrSetAndThresholds = signal<MapCellThrSetAndThresholds | undefined>(undefined);
  @Input() ratName: string = '';
  @Input() granularityName: string = '';
  @Input() standardKpiName: string = '';
  @Input() isAdmin: boolean = false;
  @Input() savingThresholds: boolean = true;
  @Output() save = new EventEmitter<MapCellThrSetAndThresholds>();

  mapCellThrSet = signal<MapCellThrSetResponseDto | undefined>(undefined);
  mapCellThresholds = signal<MapCellThresholdDto[]>([]);

  sortedThresholds = computed(() =>
    this.mapCellThresholds()
      .slice()
      .sort((a, b) => a.priority - b.priority));

  areThresholdsValid = computed(() =>
    this.mapCellThresholds().every(t =>
      t.minValue != null &&
      t.maxValue != null &&
      t.minValue < t.maxValue &&
      !!t.color &&
      !!t.label &&
      this.validateThresholds() &&
      !this.hasOverlap()
    ));

  private initialized = false;

  constructor(private alertService: AlertService) {

    effect(() => {
      if (!this.open() || this.initialized) return;

      const data = this.mapCellThrSetAndThresholds();
      console.log('1', data)
      if (!data || !data.thrSet) {
        this.createThrSet();
        this.mapCellThresholds.set([]);
        this.initialized = true;
        return;
      }

      if (data.thrSet) {
        this.mapCellThrSet.set({...data.thrSet});
      } else {
        this.createThrSet();
      }

      this.mapCellThresholds.set(
        data.thresholds ? data.thresholds.map(t => ({...t})) : []
      );

      this.initialized = true;
    });
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges) {
  }


  createThrSet(): void {
    const newThrSet: MapCellThrSetResponseDto = {
      id: 0,
      standardKpiName: this.standardKpiName,
      granularityName: this.granularityName,
      ratName: this.ratName,
      isAdmin: this.isAdmin,
      userId: ''
    };
    this.mapCellThrSet.set(newThrSet);
  }

  onCancel(): void {
    this.closed.emit();
    this.initialized = false;
  }

  updateThreshold(index: number, field: keyof MapCellThresholdDto, value: any): void {
    const updated = this.mapCellThresholds().map((t, i) =>
      i === index ? {...t, [field]: value} : t);

    this.mapCellThresholds.set(updated);
  }

  addThreshold() {
    const nextPriority = this.mapCellThresholds().length + 1;

    this.mapCellThresholds.set([
      ...this.mapCellThresholds(),
      {
        minValue: 0,
        maxValue: 1,
        color: 'grey',
        label: 'New Threshold',
        priority: nextPriority,
        mapCellThrSetId: this.mapCellThrSet()?.id ?? 0
      }
    ]);
  }

  removeThreshold(index: number) {
    const updated = this.mapCellThresholds()
      .filter((_, i) => i !== index)
      .map((t, i) => ({...t, priority: i + 1}));

    this.mapCellThresholds.set(updated);
  }

  onSave() {
    // console.log('onSave', this.mapCellThrSet(), this.mapCellThresholds());
    if (this.validateThresholds()) {
      this.save.emit({
        thrSet: this.mapCellThrSet(),
        thresholds: this.mapCellThresholds()
      });
    } else {
      console.error('Threshold ranges invalid');
      this.alertService.error('Threshold ranges invalid')
    }

  }

  //-- VALIDATORS ---

  validateThresholds(): boolean {
    const arr = this.sortedThresholds();

    for (let i = 0; i < arr.length; i++) {
      if (arr[i].minValue >= arr[i].maxValue) return false;
      if (i > 0 && arr[i].minValue < arr[i - 1].maxValue) return false;
    }
    return true;
  }

  isRowValid(threshold: MapCellThresholdDto): boolean {
    if (threshold.minValue == null ||
      threshold.maxValue == null ||
      !threshold.color ||
      !threshold.label) {
      return false;
    }
    return threshold.minValue < threshold.maxValue;
  }

  hasOverlap(): boolean {
    const sorted = [...this.mapCellThresholds()]
      .sort((a, b) => a.minValue - b.minValue);

    for (let i = 0; i < sorted.length - 1; i++) {
      if (sorted[i].maxValue > sorted[i + 1].minValue) {
        return true;
      }
    }

    return false;
  }

}
