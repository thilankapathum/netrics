import {Component, OnInit, computed, input, signal} from '@angular/core';
import {Observable} from 'rxjs';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {AntennaTypeService} from '../../../../../service/beam/antenna-type-service';
import {InfraTypeService} from '../../../../../service/beam/infra-type-service';
import {ManufacturerService} from '../../../../../service/beam/manufacturer-service';
import {OperatorService} from '../../../../../service/beam/operator-service';
import {AlertService} from '../../../../../components/alert/alert.service';
import {AuthService} from '../../../../../auth/service/auth-service';
import {AntennaTypeDto} from '../../../../../models/beam/AntennaTypeDto';
import {InfraTypeDto} from '../../../../../models/beam/InfraTypeDto';
import {ManufacturerDto} from '../../../../../models/beam/ManufacturerDto';
import {OperatorDto} from '../../../../../models/beam/OperatorDto';
import {DeleteConfirmModal} from './delete-confirm-modal/delete-confirm-modal';

export type Tab = 'antenna-types' | 'infra-types' | 'manufacturers' | 'operators';
type DeletableTab = 'antenna-types' | 'manufacturers' | 'operators';

const TAB_TITLES: Record<Tab, string> = {
  'antenna-types': 'Antenna Types',
  'infra-types': 'Infra Types',
  'manufacturers': 'Manufacturers',
  'operators': 'Operators'
};

const emptyAntennaType: AntennaTypeDto = {name: '', portCount: 0, height: 0, width: 0, retAvailability: false};
const emptyInfraType: InfraTypeDto = {infraType: '', legType: ''};
const emptyManufacturer: ManufacturerDto = {name: ''};
const emptyOperator: OperatorDto = {name: ''};

@Component({
  selector: 'app-beam-settings',
  imports: [CommonModule, FormsModule, DeleteConfirmModal],
  templateUrl: './beam-settings.html',
  styleUrl: './beam-settings.css'
})
export class BeamSettings implements OnInit {
  // When set (e.g. embedded in Pulse Settings), shows only this tab without the page header and tab bar
  tab = input<Tab | null>(null);
  activeTab = signal<Tab>('antenna-types');
  currentTab = computed(() => this.tab() ?? this.activeTab());
  tabTitles = TAB_TITLES;

  antennaTypes = signal<AntennaTypeDto[]>([]);
  infraTypes = signal<InfraTypeDto[]>([]);
  manufacturers = signal<ManufacturerDto[]>([]);
  operators = signal<OperatorDto[]>([]);

  editingAntennaTypeId = signal<number | null>(null);
  editingInfraTypeId = signal<number | null>(null);
  editingManufacturerId = signal<number | null>(null);
  editingOperatorId = signal<number | null>(null);

  antennaTypeDraft: AntennaTypeDto = {...emptyAntennaType};
  infraTypeDraft: InfraTypeDto = {...emptyInfraType};
  manufacturerDraft: ManufacturerDto = {...emptyManufacturer};
  operatorDraft: OperatorDto = {...emptyOperator};

  newAntennaType: AntennaTypeDto = {...emptyAntennaType};
  newInfraType: InfraTypeDto = {...emptyInfraType};
  newManufacturer: ManufacturerDto = {...emptyManufacturer};
  newOperator: OperatorDto = {...emptyOperator};

  saving = signal(false);

  deleteModalOpen = signal(false);
  deleteItemLabel = signal('');
  deleteDependents = signal<string[]>([]);
  deleteLoadingDependents = signal(false);
  deletingInProgress = signal(false);
  private pendingDelete: { type: DeletableTab, item: AntennaTypeDto | ManufacturerDto | OperatorDto } | null = null;

  constructor(private antennaTypeService: AntennaTypeService,
              private infraTypeService: InfraTypeService,
              private manufacturerService: ManufacturerService,
              private operatorService: OperatorService,
              private alertService: AlertService,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    this.loadAntennaTypes();
    this.loadInfraTypes();
    this.loadManufacturers();
    this.loadOperators();
  }

  canCreate(): boolean {
    return this.authService.hasAnyRole(['BEAM_CREATE']);
  }

  canEdit(): boolean {
    return this.authService.hasAnyRole(['BEAM_UPDATE']);
  }

  canDelete(): boolean {
    return this.authService.hasAnyRole(['BEAM_DELETE']);
  }

  setTab(tab: Tab): void {
    this.activeTab.set(tab);
  }

  // ---------------- Antenna Types ----------------

  private loadAntennaTypes(): void {
    this.antennaTypeService.getAll().subscribe({
      next: data => this.antennaTypes.set(data),
      error: err => this.alertService.error('Failed to load antenna types', 'Error', `${err.status} ${err.statusText}`)
    });
  }

  startEditAntennaType(item: AntennaTypeDto): void {
    this.editingAntennaTypeId.set(item.id!);
    this.antennaTypeDraft = {...item};
  }

  cancelEditAntennaType(): void {
    this.editingAntennaTypeId.set(null);
  }

  saveAntennaType(): void {
    if (!this.canEdit()) {
      this.alertService.error('Unauthorized to update antenna types');
      return;
    }
    if (this.antennaTypeDraft.portCount < 1) {
      this.alertService.error('Port count must be at least 1');
      return;
    }
    this.saving.set(true);
    this.antennaTypeService.update(this.antennaTypeDraft).subscribe({
      next: data => {
        this.saving.set(false);
        this.antennaTypes.update(list => list.map(i => i.id === data.id ? data : i));
        this.editingAntennaTypeId.set(null);
        this.alertService.success(`Updated antenna type ${data.name}`);
      },
      error: err => {
        this.saving.set(false);
        this.alertService.error('Failed to update antenna type', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }

  addAntennaType(): void {
    if (!this.canCreate()) {
      this.alertService.error('Unauthorized to create antenna types');
      return;
    }
    if (!this.newAntennaType.name.trim()) return;
    if (this.newAntennaType.portCount < 1) {
      this.alertService.error('Port count must be at least 1');
      return;
    }

    this.saving.set(true);
    this.antennaTypeService.create(this.newAntennaType).subscribe({
      next: data => {
        this.saving.set(false);
        this.antennaTypes.update(list => [data, ...list]);
        this.newAntennaType = {...emptyAntennaType};
        this.alertService.success(`Created antenna type ${data.name}`);
      },
      error: err => {
        this.saving.set(false);
        this.alertService.error('Failed to create antenna type', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }

  deleteAntennaType(item: AntennaTypeDto): void {
    if (!this.canDelete()) {
      this.alertService.error('Unauthorized to delete antenna types');
      return;
    }
    this.openDeleteModal('antenna-types', item, item.name);
  }

  // ---------------- Infra Types ----------------

  private loadInfraTypes(): void {
    this.infraTypeService.getAll().subscribe({
      next: data => this.infraTypes.set(data),
      error: err => this.alertService.error('Failed to load infra types', 'Error', `${err.status} ${err.statusText}`)
    });
  }

  startEditInfraType(item: InfraTypeDto): void {
    this.editingInfraTypeId.set(item.id!);
    this.infraTypeDraft = {...item};
  }

  cancelEditInfraType(): void {
    this.editingInfraTypeId.set(null);
  }

  saveInfraType(): void {
    if (!this.canEdit()) {
      this.alertService.error('Unauthorized to update infra types');
      return;
    }
    this.saving.set(true);
    this.infraTypeService.update(this.infraTypeDraft).subscribe({
      next: data => {
        this.saving.set(false);
        this.infraTypes.update(list => list.map(i => i.id === data.id ? data : i));
        this.editingInfraTypeId.set(null);
        this.alertService.success(`Updated infra type ${data.infraType}-${data.legType}`);
      },
      error: err => {
        this.saving.set(false);
        this.alertService.error('Failed to update infra type', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }

  addInfraType(): void {
    if (!this.canCreate()) {
      this.alertService.error('Unauthorized to create infra types');
      return;
    }
    if (!this.newInfraType.infraType.trim() || !this.newInfraType.legType.trim()) return;

    this.saving.set(true);
    this.infraTypeService.create(this.newInfraType).subscribe({
      next: data => {
        this.saving.set(false);
        this.infraTypes.update(list => [data, ...list]);
        this.newInfraType = {...emptyInfraType};
        this.alertService.success(`Created infra type ${data.infraType}-${data.legType}`);
      },
      error: err => {
        this.saving.set(false);
        this.alertService.error('Failed to create infra type', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }

  // ---------------- Manufacturers ----------------

  private loadManufacturers(): void {
    this.manufacturerService.getAll().subscribe({
      next: data => this.manufacturers.set(data),
      error: err => this.alertService.error('Failed to load manufacturers', 'Error', `${err.status} ${err.statusText}`)
    });
  }

  startEditManufacturer(item: ManufacturerDto): void {
    this.editingManufacturerId.set(item.id!);
    this.manufacturerDraft = {...item};
  }

  cancelEditManufacturer(): void {
    this.editingManufacturerId.set(null);
  }

  saveManufacturer(): void {
    if (!this.canEdit()) {
      this.alertService.error('Unauthorized to update manufacturers');
      return;
    }
    this.saving.set(true);
    this.manufacturerService.update(this.manufacturerDraft).subscribe({
      next: data => {
        this.saving.set(false);
        this.manufacturers.update(list => list.map(i => i.id === data.id ? data : i));
        this.editingManufacturerId.set(null);
        this.alertService.success(`Updated manufacturer ${data.name}`);
      },
      error: err => {
        this.saving.set(false);
        this.alertService.error('Failed to update manufacturer', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }

  addManufacturer(): void {
    if (!this.canCreate()) {
      this.alertService.error('Unauthorized to create manufacturers');
      return;
    }
    if (!this.newManufacturer.name.trim()) return;

    this.saving.set(true);
    this.manufacturerService.create(this.newManufacturer).subscribe({
      next: data => {
        this.saving.set(false);
        this.manufacturers.update(list => [data, ...list]);
        this.newManufacturer = {...emptyManufacturer};
        this.alertService.success(`Created manufacturer ${data.name}`);
      },
      error: err => {
        this.saving.set(false);
        this.alertService.error('Failed to create manufacturer', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }

  deleteManufacturer(item: ManufacturerDto): void {
    if (!this.canDelete()) {
      this.alertService.error('Unauthorized to delete manufacturers');
      return;
    }
    this.openDeleteModal('manufacturers', item, item.name);
  }

  // ---------------- Operators ----------------

  private loadOperators(): void {
    this.operatorService.getAll().subscribe({
      next: data => this.operators.set(data),
      error: err => this.alertService.error('Failed to load operators', 'Error', `${err.status} ${err.statusText}`)
    });
  }

  startEditOperator(item: OperatorDto): void {
    this.editingOperatorId.set(item.id!);
    this.operatorDraft = {...item};
  }

  cancelEditOperator(): void {
    this.editingOperatorId.set(null);
  }

  saveOperator(): void {
    if (!this.canEdit()) {
      this.alertService.error('Unauthorized to update operators');
      return;
    }
    this.saving.set(true);
    this.operatorService.update(this.operatorDraft).subscribe({
      next: data => {
        this.saving.set(false);
        this.operators.update(list => list.map(i => i.id === data.id ? data : i));
        this.editingOperatorId.set(null);
        this.alertService.success(`Updated operator ${data.name}`);
      },
      error: err => {
        this.saving.set(false);
        this.alertService.error('Failed to update operator', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }

  addOperator(): void {
    if (!this.canCreate()) {
      this.alertService.error('Unauthorized to create operators');
      return;
    }
    if (!this.newOperator.name.trim()) return;

    this.saving.set(true);
    this.operatorService.create(this.newOperator).subscribe({
      next: data => {
        this.saving.set(false);
        this.operators.update(list => [data, ...list]);
        this.newOperator = {...emptyOperator};
        this.alertService.success(`Created operator ${data.name}`);
      },
      error: err => {
        this.saving.set(false);
        this.alertService.error('Failed to create operator', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }

  deleteOperator(item: OperatorDto): void {
    if (!this.canDelete()) {
      this.alertService.error('Unauthorized to delete operators');
      return;
    }
    this.openDeleteModal('operators', item, item.name);
  }

  // ---------------- Delete confirmation modal ----------------

  private dependentsServiceFor(type: DeletableTab): { getDependents: (name: string) => Observable<string[]>, delete: (name: string, force?: boolean) => Observable<unknown> } {
    if (type === 'antenna-types') return this.antennaTypeService;
    if (type === 'manufacturers') return this.manufacturerService;
    return this.operatorService;
  }

  private openDeleteModal(type: DeletableTab, item: AntennaTypeDto | ManufacturerDto | OperatorDto, name: string): void {
    this.pendingDelete = {type, item};
    this.deleteItemLabel.set(name);
    this.deleteDependents.set([]);
    this.deleteLoadingDependents.set(true);
    this.deleteModalOpen.set(true);

    this.dependentsServiceFor(type).getDependents(name).subscribe({
      next: deps => {
        this.deleteDependents.set(deps);
        this.deleteLoadingDependents.set(false);
      },
      error: err => {
        this.deleteLoadingDependents.set(false);
        this.alertService.error('Failed to check dependencies', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }

  onDeleteModalClosed(): void {
    this.deleteModalOpen.set(false);
    this.pendingDelete = null;
  }

  onDeleteConfirmed(event: { force: boolean }): void {
    if (!this.pendingDelete) return;
    const {type, item} = this.pendingDelete;
    const name = item.name;

    this.deletingInProgress.set(true);
    this.dependentsServiceFor(type).delete(name, event.force).subscribe({
      next: () => {
        this.deletingInProgress.set(false);
        this.deleteModalOpen.set(false);
        this.pendingDelete = null;

        if (type === 'antenna-types') this.antennaTypes.update(list => list.filter(i => i.id !== (item as AntennaTypeDto).id));
        if (type === 'manufacturers') this.manufacturers.update(list => list.filter(i => i.id !== (item as ManufacturerDto).id));
        if (type === 'operators') this.operators.update(list => list.filter(i => i.id !== (item as OperatorDto).id));

        this.alertService.success(`Deleted ${name}`);
      },
      error: err => {
        this.deletingInProgress.set(false);
        this.alertService.error('Failed to delete', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }
}
