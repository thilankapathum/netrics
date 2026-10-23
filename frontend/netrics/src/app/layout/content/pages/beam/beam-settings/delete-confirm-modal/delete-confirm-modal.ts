import {Component, effect, EventEmitter, Input, Output, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-delete-confirm-modal',
  imports: [CommonModule, FormsModule],
  templateUrl: './delete-confirm-modal.html',
  styleUrl: './delete-confirm-modal.css'
})
export class DeleteConfirmModal {
  @Input() open = signal<boolean>(false);
  @Input() itemLabel = '';
  @Input() dependents: string[] = [];
  @Input() loadingDependents = false;
  @Input() deleting = false;
  // false: items in use can't be deleted at all (no "Delete anyway")
  @Input() allowForce = true;
  /** Overrides the warning shown above the dependents list when allowForce is on */
  @Input() dependentsNote = '';
  @Input() forceLabel = 'Delete anyway';

  @Output() closed = new EventEmitter<void>();
  @Output() confirmed = new EventEmitter<{ force: boolean }>();

  confirmCode = '';
  typedCode = '';

  private initialized = false;

  constructor() {
    effect(() => {
      if (!this.open() || this.initialized) return;

      this.confirmCode = String(Math.floor(100000 + Math.random() * 900000));
      this.typedCode = '';
      this.initialized = true;
    });
  }

  get hasDependents(): boolean {
    return !this.loadingDependents && this.dependents.length > 0;
  }

  get codeMatches(): boolean {
    return this.typedCode.trim() === this.confirmCode;
  }

  onCancel(): void {
    this.initialized = false;
    this.closed.emit();
  }

  onConfirmDelete(): void {
    this.initialized = false;
    this.confirmed.emit({force: false});
  }

  onForceDelete(): void {
    if (!this.codeMatches) return;
    this.initialized = false;
    this.confirmed.emit({force: true});
  }
}
