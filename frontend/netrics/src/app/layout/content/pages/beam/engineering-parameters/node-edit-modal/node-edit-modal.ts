import {Component, effect, EventEmitter, Input, OnInit, Output, signal} from '@angular/core';
import {FormsModule, NgForm} from '@angular/forms';
import {Observable} from 'rxjs';
import {CommonModule} from '@angular/common';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {SiteService} from '../../../../../../service/beam/site-service';
import {SectorService} from '../../../../../../service/beam/sector-service';
import {AntennaService} from '../../../../../../service/beam/antenna-service';
import {ElectricalTiltService} from '../../../../../../service/beam/electrical-tilt-service';
import {OperatorService} from '../../../../../../service/beam/operator-service';
import {InfraTypeService} from '../../../../../../service/beam/infra-type-service';
import {AntennaTypeService} from '../../../../../../service/beam/antenna-type-service';
import {ManufacturerService} from '../../../../../../service/beam/manufacturer-service';
import {BandService} from '../../../../../../service/beam/band-service';
import {SiteDto} from '../../../../../../models/beam/SiteDto';
import {SectorDto} from '../../../../../../models/beam/SectorDto';
import {AntennaDto} from '../../../../../../models/beam/AntennaDto';
import {ElectricalTiltDto} from '../../../../../../models/beam/ElectricalTiltDto';
import {OperatorDto} from '../../../../../../models/beam/OperatorDto';
import {InfraTypeDto} from '../../../../../../models/beam/InfraTypeDto';
import {AntennaTypeDto} from '../../../../../../models/beam/AntennaTypeDto';
import {ManufacturerDto} from '../../../../../../models/beam/ManufacturerDto';
import {BeamBandDto} from '../../../../../../models/beam/BeamBandDto';
import {DeleteConfirmModal} from '../../beam-settings/delete-confirm-modal/delete-confirm-modal';
import {antennaToNode, NodeType, sectorToNode, tiltToNode, TreeNodeVm} from '../tree-node.model';

@Component({
  selector: 'app-node-edit-modal',
  imports: [FormsModule, CommonModule, DeleteConfirmModal],
  templateUrl: './node-edit-modal.html',
  styleUrl: './node-edit-modal.css'
})
export class NodeEditModal implements OnInit {
  @Input() open = signal<boolean>(false);
  @Input() mode: 'create' | 'edit' = 'edit';
  @Input() nodeType!: NodeType;
  @Input() editNode?: TreeNodeVm;
  @Input() parentNode?: TreeNodeVm;
  @Input() canEdit = false;
  /** Site the node belongs to; used to pre-fill new antennas */
  @Input() contextSite?: SiteDto;
  @Input() canDelete = false;

  @Output() closed = new EventEmitter<void>();
  @Output() saved = new EventEmitter<TreeNodeVm>();
  @Output() deleted = new EventEmitter<void>();

  site: SiteDto = {siteCode: ''};
  sector: SectorDto = {name: ''};
  antenna: AntennaDto = {sectorName: ''};
  tilt: ElectricalTiltDto = {antennaId: 0, bandName: ''};

  /** Indexes already used by siblings of the sector/antenna being created */
  takenIndexes: number[] = [];

  operators: OperatorDto[] = [];
  infraTypes: InfraTypeDto[] = [];
  antennaTypes: AntennaTypeDto[] = [];
  manufacturers: ManufacturerDto[] = [];
  bands: BeamBandDto[] = [];

  saving = signal(false);

  deleteOpen = signal(false);
  deleting = signal(false);
  loadingDependents = signal(false);
  /** Tilts removed along with the antenna being deleted */
  deleteDependents: string[] = [];
  private initialized = false;

  constructor(private alertService: AlertService,
              private siteService: SiteService,
              private sectorService: SectorService,
              private antennaService: AntennaService,
              private electricalTiltService: ElectricalTiltService,
              private operatorService: OperatorService,
              private infraTypeService: InfraTypeService,
              private antennaTypeService: AntennaTypeService,
              private manufacturerService: ManufacturerService,
              private bandService: BandService) {

    effect(() => {
      if (!this.open() || this.initialized) return;

      this.initForm();
      this.initialized = true;
    });
  }

  ngOnInit(): void {
    this.operatorService.getAll().subscribe(data => this.operators = data);
    this.infraTypeService.getAll().subscribe(data => this.infraTypes = data);
    this.antennaTypeService.getAll().subscribe(data => this.antennaTypes = data);
    this.manufacturerService.getAll().subscribe(data => this.manufacturers = data);
    this.bandService.getAll().subscribe(data => this.bands = data);
  }

  private initForm(): void {
    if (this.mode === 'edit' && this.editNode) {
      if (this.nodeType === 'site') this.site = {...this.editNode.data as SiteDto};
      if (this.nodeType === 'sector') this.sector = {...this.editNode.data as SectorDto};
      if (this.nodeType === 'antenna') this.antenna = {...this.editNode.data as AntennaDto};
      if (this.nodeType === 'tilt') this.tilt = {...this.editNode.data as ElectricalTiltDto};
    } else {
      this.takenIndexes = [];
      if (this.nodeType === 'site') this.site = {siteCode: ''};
      if (this.nodeType === 'sector') {
        const siteCode = (this.parentNode?.data as SiteDto).siteCode;
        this.sector = {name: '', sectorIndex: undefined, azimuth: undefined, siteCode};
        this.sectorService.getBySiteCode(siteCode).subscribe({
          next: sectors => {
            this.takenIndexes = sectors.map(s => s.sectorIndex!);
            this.sector.sectorIndex = this.nextFreeIndex();
          },
          error: err => this.alertService.httpError('Failed to load existing sectors', err)
        });
      }
      if (this.nodeType === 'antenna') {
        const sector = this.parentNode?.data as SectorDto;
        const sectorName = sector.name;
        // antenna usually points with the sector and sits on top of the structure; user can adjust
        const heights = [this.contextSite?.towerHeight, this.contextSite?.buildingHeight].filter((h): h is number => h != null);
        this.antenna = {
          sectorName, antennaTypeName: '', manufacturerName: '',
          azimuth: sector.azimuth,
          mechanicalTilt: 0,
          antennaHeight: heights.length ? heights.reduce((a, b) => a + b, 0) : undefined
        };
        this.antennaService.getBySectorName(sectorName).subscribe({
          next: antennas => {
            this.takenIndexes = antennas.map(a => a.antennaIndex!);
            this.antenna.antennaIndex = this.nextFreeIndex();
          },
          error: err => this.alertService.httpError('Failed to load existing antennas', err)
        });
      }
      if (this.nodeType === 'tilt') {
        const antennaId = (this.parentNode?.data as AntennaDto).id!;
        this.tilt = {electricalTilt: undefined, antennaId, bandName: ''};
      }
    }
  }

  onCancel(): void {
    this.initialized = false;
    this.closed.emit();
  }

  private nextFreeIndex(): number {
    return Math.max(0, ...this.takenIndexes) + 1;
  }

  /** Duplicate sector/antenna index check (create only; index is read-only on edit) */
  indexTaken(): boolean {
    if (this.mode !== 'create') return false;
    const index = this.nodeType === 'sector' ? this.sector.sectorIndex
      : this.nodeType === 'antenna' ? this.antenna.antennaIndex : undefined;
    return index != null && this.takenIndexes.includes(index);
  }

  onSave(form: NgForm): void {
    if (!this.canEdit) {
      this.alertService.error('Unauthorized to modify Engineering Parameters');
      return;
    }
    if (form.invalid || this.indexTaken()) {
      form.control.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    if (this.nodeType === 'site') this.saveSite();
    else if (this.nodeType === 'sector') this.saveSector();
    else if (this.nodeType === 'antenna') this.saveAntenna();
    else this.saveTilt();
  }

  private saveSite(): void {
    this.siteService.updateSite(this.site).subscribe({
      next: data => {
        this.saving.set(false);
        this.alertService.success(`Updated site ${data.siteCode}`);
        this.emitSaved({type: 'site', label: data.siteCode, data});
      },
      error: err => this.onError(err, `update site ${this.site.siteCode}`)
    });
  }

  private saveSector(): void {
    if (this.mode === 'create') {
      this.sector.name = `${this.sector.siteCode}__${this.sector.sectorIndex}`;
      this.sectorService.createSector(this.sector).subscribe({
        next: data => {
          this.saving.set(false);
          this.alertService.success(`Created sector ${data.name}`);
          this.emitSaved(sectorToNode(data));
        },
        error: err => this.onError(err, `create sector`)
      });
    } else {
      this.sectorService.updateSector(this.sector).subscribe({
        next: data => {
          this.saving.set(false);
          this.alertService.success(`Updated sector ${data.name}`);
          this.emitSaved(sectorToNode(data));
        },
        error: err => this.onError(err, `update sector ${this.sector.name}`)
      });
    }
  }

  private saveAntenna(): void {
    const request = this.mode === 'create'
      ? this.antennaService.createAntenna(this.antenna)
      : this.antennaService.updateAntenna(this.antenna);

    request.subscribe({
      next: data => {
        this.saving.set(false);
        this.alertService.success(`${this.mode === 'create' ? 'Created' : 'Updated'} antenna ${data.antennaIndex}`);
        this.emitSaved(antennaToNode(data));
      },
      error: err => this.onError(err, `save antenna`)
    });
  }

  private saveTilt(): void {
    const request = this.mode === 'create'
      ? this.electricalTiltService.createElectricalTilt(this.tilt)
      : this.electricalTiltService.updateElectricalTilt(this.tilt);

    request.subscribe({
      next: data => {
        this.saving.set(false);
        this.alertService.success(`${this.mode === 'create' ? 'Created' : 'Updated'} electrical tilt`);
        this.emitSaved(tiltToNode(data));
      },
      error: err => this.onError(err, `save electrical tilt`)
    });
  }

  private emitSaved(node: TreeNodeVm): void {
    this.initialized = false;
    this.saved.emit(node);
  }

  private onError(err: any, action: string): void {
    this.saving.set(false);
    console.error(err);
    this.alertService.httpError(`Error trying to ${action}`, err);
  }

  /** Antennas and tilts can be deleted from their edit dialog; sites and sectors can't */
  canDeleteNode(): boolean {
    return this.canDelete && this.mode === 'edit' && (this.nodeType === 'antenna' || this.nodeType === 'tilt');
  }

  deleteLabel(): string {
    return this.nodeType === 'antenna'
      ? `antenna ${this.antenna.antennaIndex} on ${this.antenna.sectorName}`
      : `${this.tilt.bandName} electrical tilt (${this.tilt.electricalTilt}°)`;
  }

  onDeleteClick(): void {
    this.deleteDependents = [];
    this.deleteOpen.set(true);
    if (this.nodeType !== 'antenna') return;

    this.loadingDependents.set(true);
    this.electricalTiltService.getByAntennaId(this.antenna.id!).subscribe({
      next: tilts => {
        this.deleteDependents = tilts.map(t => `${t.bandName} electrical tilt: ${t.electricalTilt}°`);
        this.loadingDependents.set(false);
      },
      error: err => {
        this.loadingDependents.set(false);
        this.deleteOpen.set(false);
        this.alertService.httpError('Failed to load electrical tilts for this antenna', err);
      }
    });
  }

  onDeleteConfirmed(): void {
    const label = this.deleteLabel();
    const request: Observable<unknown> = this.nodeType === 'antenna'
      ? this.antennaService.deleteAntenna(this.antenna)
      : this.electricalTiltService.deleteElectricalTilt(this.tilt.id!);

    this.deleting.set(true);
    request.subscribe({
      next: () => {
        this.deleting.set(false);
        this.deleteOpen.set(false);
        this.alertService.success(`Deleted ${label}`);
        this.initialized = false;
        this.deleted.emit();
      },
      error: err => {
        this.deleting.set(false);
        this.alertService.httpError(`Failed to delete ${label}`, err);
      }
    });
  }

  infraTypeOption(infraType: InfraTypeDto): string {
    return `${infraType.infraType}-${infraType.legType}`;
  }
}
