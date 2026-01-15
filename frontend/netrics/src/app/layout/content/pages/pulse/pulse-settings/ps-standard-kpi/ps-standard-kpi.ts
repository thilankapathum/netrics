import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'app-ps-standard-kpi',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './ps-standard-kpi.html',
  styleUrl: './ps-standard-kpi.css'
})
export class PsStandardKpi {
  @Input() open: boolean = false;
  @Output() closed = new EventEmitter<void>();

  onCancel():void{
    this.closed.emit();
  }
}
