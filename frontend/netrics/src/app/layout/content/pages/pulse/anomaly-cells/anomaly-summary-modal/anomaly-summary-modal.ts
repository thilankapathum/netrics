import {Component, effect, EventEmitter, Input, Output, signal} from '@angular/core';
import {AnomalySummaryDto} from '../../../../../../models/pulse/AnomalySummaryRowDto';
import {AnomalyCellsService} from '../../../../../../service/pulse/anomaly-cells-service';
import {AlertService} from '../../../../../../components/alert/alert.service';

@Component({
  selector: 'app-anomaly-summary-modal',
  imports: [],
  templateUrl: './anomaly-summary-modal.html',
  styleUrl: './anomaly-summary-modal.css'
})
export class AnomalySummaryModal {
  @Input() open = signal<boolean>(false);
  @Output() closed = new EventEmitter<void>();

  @Input() areaName = signal<string | undefined>(undefined);
  @Input() ratName = signal<string | undefined>(undefined);
  @Input() granularityName = signal<string | undefined>(undefined);
  @Input() kpiName = signal<string | null>(null);

  summary = signal<AnomalySummaryDto | null>(null);
  loading = signal(false);
  private initialized = false;

  constructor(private anomalyCellsService: AnomalyCellsService,
              private alertService: AlertService) {
    effect(() => {
      if (!this.open() || this.initialized) return;

      this.fetchSummary();
      this.initialized = true;
    });
  }

  private fetchSummary(): void {
    this.loading.set(true);
    this.anomalyCellsService
      .getAnomalySummary({
        areaName: this.areaName()!,
        ratName: this.ratName()!,
        granularityName: this.granularityName()!,
        kpiName: this.kpiName(),
      })
      .subscribe({
        next: (data) => {
          this.summary.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.alertService.error('Summary retrieval failed', 'Error', `${err.status} ${err.statusText}`);
        },
      });
  }

  onCancel(): void {
    this.initialized = false;
    this.summary.set(null);
    this.closed.emit();
  }
}
