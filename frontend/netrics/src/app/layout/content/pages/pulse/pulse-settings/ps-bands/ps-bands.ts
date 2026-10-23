import {Component, OnInit, signal} from '@angular/core';
import {NgTemplateOutlet} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {BandService} from '../../../../../../service/pulse/band-service';
import {BandDto} from '../../../../../../models/pulse/BandDto';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {AuthService} from '../../../../../../auth/service/auth-service';
import {DeleteConfirmModal} from '../../../beam/beam-settings/delete-confirm-modal/delete-confirm-modal';

const emptyBand = (): BandDto => ({name: '', number: undefined, unit: 'MHz'});

@Component({
  selector: 'app-ps-bands',
  imports: [FormsModule, NgTemplateOutlet, DeleteConfirmModal],
  templateUrl: './ps-bands.html'
})
export class PsBands implements OnInit {
  bands = signal<BandDto[]>([]);
  loading = signal(true);
  saving = signal(false);

  // Bands have no id on the DTO; the original name identifies the row being edited
  editingName = signal<string | null>(null);
  draft: BandDto = emptyBand();
  newBand: BandDto = emptyBand();

  deleteModalOpen = signal(false);
  deleteItemLabel = signal('');
  deleteDependents = signal<string[]>([]);
  deleteLoadingDependents = signal(false);
  deletingInProgress = signal(false);

  constructor(private bandService: BandService,
              private alertService: AlertService,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    this.bandService.getAllBands().subscribe({
      next: data => {
        this.bands.set([...data].sort((a, b) => (a.number ?? 0) - (b.number ?? 0)));
        this.loading.set(false);
      },
      error: err => {
        this.loading.set(false);
        this.alertService.httpError('Failed to load bands', err);
      }
    });
  }

  canCreate = () => this.authService.hasAnyRole(['PULSE_CREATE']);
  canEdit = () => this.authService.hasAnyRole(['PULSE_UPDATE']);
  canDelete = () => this.authService.hasAnyRole(['PULSE_DELETE']);

  isValid(b: BandDto): boolean {
    return !!b.name?.trim() && !!b.unit?.trim() && (b.number ?? 0) >= 1;
  }

  add(): void {
    if (!this.isValid(this.newBand)) return;
    this.saving.set(true);
    this.bandService.create(this.trimmed(this.newBand)).subscribe({
      next: data => {
        this.saving.set(false);
        this.bands.update(list => [data, ...list]);
        this.newBand = emptyBand();
        this.alertService.success(`Added band ${data.name}`);
      },
      error: err => {
        this.saving.set(false);
        this.alertService.httpError('Failed to add band', err);
      }
    });
  }

  startEdit(item: BandDto): void {
    this.editingName.set(item.name!);
    this.draft = {...item};
  }

  cancelEdit(): void {
    this.editingName.set(null);
  }

  save(): void {
    const original = this.editingName();
    if (!original || !this.isValid(this.draft)) return;
    this.saving.set(true);
    this.bandService.update(original, this.trimmed(this.draft)).subscribe({
      next: data => {
        this.saving.set(false);
        this.bands.update(list => list.map(b => b.name === original ? data : b));
        this.editingName.set(null);
        this.alertService.success(`Saved band ${data.name}`);
      },
      error: err => {
        this.saving.set(false);
        this.alertService.httpError('Failed to save band', err);
      }
    });
  }

  openDelete(item: BandDto): void {
    this.deleteItemLabel.set(item.name!);
    this.deleteDependents.set([]);
    this.deleteLoadingDependents.set(true);
    this.deleteModalOpen.set(true);

    this.bandService.getDependents(item.name!).subscribe({
      next: deps => {
        this.deleteDependents.set(deps);
        this.deleteLoadingDependents.set(false);
      },
      error: err => {
        // Usage unknown (e.g. Beam unreachable) -> don't offer delete
        this.deleteModalOpen.set(false);
        this.deleteLoadingDependents.set(false);
        this.alertService.httpError(`Couldn't check where ${item.name} is used`, err);
      }
    });
  }

  onDeleteConfirmed(): void {
    const name = this.deleteItemLabel();
    this.deletingInProgress.set(true);
    this.bandService.delete(name).subscribe({
      next: () => {
        this.deletingInProgress.set(false);
        this.deleteModalOpen.set(false);
        this.bands.update(list => list.filter(b => b.name !== name));
        this.alertService.success(`Deleted band ${name}`);
      },
      error: err => {
        this.deletingInProgress.set(false);
        this.alertService.httpError('Failed to delete band', err);
      }
    });
  }

  private trimmed(b: BandDto): BandDto {
    return {name: b.name!.trim(), number: b.number, unit: b.unit!.trim()};
  }
}
