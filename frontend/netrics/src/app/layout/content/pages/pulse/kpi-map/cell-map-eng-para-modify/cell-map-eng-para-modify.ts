import {Component, computed, effect, EventEmitter, Input, Output, signal} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CellService} from '../../../../../../service/pulse/cell-service';
import {CellDto} from '../../../../../../models/pulse/CellDto';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {SectorService} from '../../../../../../service/pulse/sector-service';
import {SectorDto} from '../../../../../../models/pulse/SectorDto';
import {AuthService} from '../../../../../../auth/service/auth-service';

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

  sectors = signal<SectorDto[]>([]);
  sectorQuery = signal('');
  filteredSectors = computed(() => {
    const q = this.sectorQuery().trim().toLowerCase();
    return this.sectors().filter(s => s.name.toLowerCase().includes(q));
  });

  readonly canOpenBeam: boolean;

  /** Unit vectors for the N/E/S/W ticks on the beam preview */
  readonly compassTicks = [0, 90, 180, 270].map(a => ({label: a, ...this.polar(a, 1)}));

  private initialized = false;

  loading = signal(false);
  savingCell: boolean = false;

  constructor(private cellService: CellService,
              private alertService: AlertService,
              private sectorService: SectorService,
              authService: AuthService) {
    this.canOpenBeam = authService.hasRole('BEAM_READ');

    effect(() => {
      if (!this.open() || this.initialized) return;

      this.getCellInfo(this.cellName);
      this.initialized = true;
    });
  }

  getCellInfo(cellName: string): void {
    this.loading.set(true);
    this.cellService.getCellByName(cellName).subscribe({
      next: data => {
        console.log('cellInfo', data);
        this.cell = data;
        this.loading.set(false);
        this.getSectors(this.cell.siteCode!);
      }, error: err => {
        console.error(err);
        this.loading.set(false);
        this.alertService.error(`Error finding cell by ${cellName}`, 'Error', `${err.statusCode} ${err.statusText}`);
      }
    })
  }

  getSectors(siteCode: string): void {
    this.sectorService.getSectorsBySiteCode(siteCode).subscribe({
      next: data => this.sectors.set(data),
      error: err => {
        console.error(err);
        this.alertService.error(`Error finding sectors of ${siteCode}`, 'Error', `${err.statusCode} ${err.statusText}`);
      }
    })
  }

  selectSector(sector: SectorDto): void {
    this.cell.sectorName = sector.name;
    this.sectorQuery.set('');
    (document.activeElement as HTMLElement | null)?.blur(); // closes the daisyUI focus dropdown
  }

  onCancel(): void {
    this.initialized = false;
    this.sectors.set([]);
    this.sectorQuery.set('');
    this.closed.emit();
  }

  onSaveCell(): void {
    if (this.isEngineeringParaEditable) {
      this.savingCell = true;
      this.cellService.updateCell(this.cell).subscribe({
        next: data => {
          this.alertService.success(`Saved cell ${this.cellName}`);
          this.cell = data;
          this.savingCell = false;
        }, error: err => {
          console.error(err);
          this.alertService.error(`Error updating cell ${this.cellName}`, 'Error', `${err.statusCode} ${err.statusText}`);
          this.savingCell = false;
        }
      })
    } else {
      this.alertService.error(`Unauthorized to update Cells`);
    }
  }

  azimuthValid() {
    return this.cell.azimuth! < 360 && this.cell.azimuth! >= 0;
  }

  beamwidthValid() {
    return this.cell.beamwidth! <= 360 && this.cell.beamwidth! > 0;
  }

  isValid() {
    return this.azimuthValid() && this.beamwidthValid();
  }

  sectorAzimuth(): number | undefined {
    return this.sectors().find(s => s.name === this.cell.sectorName)?.azimuth;
  }

  /** Smallest angle between the cell's and its sector's azimuth, or null when either is unknown */
  azimuthOffset(): number | null {
    const sector = this.sectorAzimuth();
    if (sector == null || !this.azimuthValid()) return null;
    const diff = Math.abs(this.cell.azimuth! - sector) % 360;
    return Math.min(diff, 360 - diff);
  }

  /** Compass bearing (0 = north, clockwise) to SVG coordinates */
  polar(bearing: number, r: number) {
    const rad = bearing * Math.PI / 180;
    return {x: r * Math.sin(rad), y: -r * Math.cos(rad)};
  }

  /** Wedge path for the cell's footprint on the beam preview */
  beamPath(): string | null {
    if (!this.isValid()) return null;
    const r = 38;
    const bw = Number(this.cell.beamwidth);
    if (bw >= 360) return `M 0 ${-r} A ${r} ${r} 0 1 1 0 ${r} A ${r} ${r} 0 1 1 0 ${-r} Z`;
    const az = Number(this.cell.azimuth);
    const a = this.polar(az - bw / 2, r);
    const b = this.polar(az + bw / 2, r);
    return `M 0 0 L ${a.x} ${a.y} A ${r} ${r} 0 ${bw > 180 ? 1 : 0} 1 ${b.x} ${b.y} Z`;
  }

}
