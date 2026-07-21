import {Component, effect, EventEmitter, Input, Output, signal} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {Linechart} from "../../../../../../components/charts/linechart/linechart/linechart";
import {Router, RouterLink} from '@angular/router';
import {SharedService} from '../../../../../../service/pulse/shared-service';
import {KpidayService} from '../../../../../../service/pulse/ltefdd/kpiday.service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {ChartService} from '../../../../../../service/components/chart/chart.service';
import {Observable} from 'rxjs';
import {KpiDataDto} from '../../../../../../models/pulse/KpiDataDto';
import {StandardRawKpiMappingService} from '../../../../../../service/pulse/standard-raw-kpi-mapping-service';

@Component({
  selector: 'app-cell-analysis-modal',
  imports: [
    FormsModule,
    Linechart,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './cell-analysis-modal.html',
  styleUrl: './cell-analysis-modal.css'
})
export class CellAnalysisModal {

  @Input() open = signal<boolean>(false);
  @Output() closed = new EventEmitter<void>();
  @Input() returnPage: string = '';

  @Input() cellName = signal<string>('');
  @Input() standardKpiName = signal<string | undefined>(undefined);
  @Input() ratName = signal<string | undefined>(undefined);
  @Input() granularityName = signal<string | undefined>(undefined);

  trendPeriod = signal<'month' | 'week' | 'quarter'>('month')

  chartSeries: any = null;

  loadingChart: boolean = false;
  loadingStandardRawKpiMapping: boolean = false;
  private initialized = false;

  standardRawKpiMappingAvailable = signal<boolean>(false);
  showOperands = signal<boolean>(false);

  constructor(private router: Router,
              private sharedService: SharedService,
              private kpiDayService: KpidayService,
              private alertService: AlertService,
              private chartService: ChartService,
              private standardRawKpiMappingService: StandardRawKpiMappingService) {

    effect(() => {
      if (!this.open()) {
        this.initialized = false;
        return;
      }
      const cell = this.cellName();      // now tracked
      const kpi = this.standardKpiName();
      if (!kpi) return;

      this.getStandardRawKpiMappingAvailable(this.ratName()!, kpi);
      this.queryModalTrendData(kpi, cell, this.trendPeriod(), this.ratName()!, this.granularityName()!);
    });
  }

  onCancel(): void {
    this.initialized = false;
    this.showOperands.set(false);
    this.trendPeriod.set('month');
    this.closed.emit();
  }

  loadingAll() {
    return this.loadingChart || this.loadingStandardRawKpiMapping;
  }

  onPeriodChange($event: Event) {
    this.queryModalTrendData(this.standardKpiName()!, this.cellName(), this.trendPeriod(), this.ratName()!, this.granularityName()!);
  }

  onShowOperandsChangeModal(checked: boolean) {
    this.showOperands.set(checked);
    this.queryModalTrendData(this.standardKpiName()!, this.cellName(), this.trendPeriod(), this.ratName()!, this.granularityName()!);
  }

  private queryModalTrendData(kpiName: string, cellName: string, trendPeriod: string, ratName: string, granularityName: string) {
    this.chartSeries = [];
    const resolvedGranularity = this.resolveGranularity(trendPeriod, granularityName);
    this.loadingChart = true;

    const request$ = this.showOperands()
      ? this.kpiDayService.getDataByKpiAndCellWithOperands(kpiName, cellName, trendPeriod, ratName, resolvedGranularity)
      : this.kpiDayService.getDataByKpiAndCell(kpiName, cellName, trendPeriod, ratName, resolvedGranularity);

    request$.subscribe({
      next: data => {
        this.chartSeries = this.showOperands()
          ? this.chartService.buildSeriesKpiDataWithOperandsDto(data)
          : this.chartService.buildSeriesKpiDataDto(data);
        this.loadingChart = false;
      },
      error: error => {
        this.loadingChart = false;
        console.error('Error retrieving KPI trend data:', error);
        this.alertService.error(`KPI Data retrieval failed. ${error.status} ${error.statusText}`);
      }
    });
  }

  getStandardRawKpiMappingAvailable(ratName: string, standardKpiName: string) {
    this.loadingStandardRawKpiMapping = true;
    this.standardRawKpiMappingService.isMappingAvailable(ratName, standardKpiName).subscribe({
      next: data => {
        this.standardRawKpiMappingAvailable.set(data);
        this.loadingStandardRawKpiMapping = false;
      }, error: error => {
        console.error("Error getting standardRawKpiMappingAvailable:", error);
        this.alertService.error(`Standard-Raw-KPI-Mapping retrieval failed :"${error.status} ${error.statusText}`);
        this.loadingStandardRawKpiMapping = false;
      }
    })
  }

  private resolveGranularity(period: string, selectedGranularity: string): string {
    return period === 'week' ? 'hour' : selectedGranularity;
  }

  getRouterLinkForCell(): string[] {
    return ['/pulse/cell']
  }

  onCellNavigate() {
    this.sharedService.selectedGranularity.set(this.granularityName()!);
    this.sharedService.selectedRat.set(this.ratName()!);
    this.sharedService.selectedStandardKpi.set(this.standardKpiName()!);
    this.sharedService.selectedCell.set(this.cellName());
    this.sharedService.returnPage = this.returnPage;
  }

  get modalYAxis(): ApexYAxis[] | undefined {
    if (!this.showOperands()) return undefined;
    return [
      {seriesName: 'KPI Value', title: {text: 'KPI Value'}},
      {seriesName: 'Numerator', opposite: true, title: {text: 'Count'}},
      {seriesName: 'Denominator', opposite: true, show: false}
    ];
  }

}
