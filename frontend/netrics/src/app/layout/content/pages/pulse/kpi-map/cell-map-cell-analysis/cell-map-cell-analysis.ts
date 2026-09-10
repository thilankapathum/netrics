import {Component, effect, EventEmitter, Input, Output, signal} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {Linechart} from "../../../../../../components/charts/linechart/linechart/linechart";
import {Router, RouterLink} from '@angular/router';
import {SharedService} from '../../../../../../service/pulse/shared-service';
import {Observable} from 'rxjs';
import {KpiDataDto} from '../../../../../../models/pulse/KpiDataDto';
import {KpidayService} from '../../../../../../service/pulse/ltefdd/kpiday.service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {ChartService} from '../../../../../../service/components/chart/chart.service';

@Component({
  selector: 'app-cell-map-cell-analysis',
  imports: [
    FormsModule,
    Linechart,
    RouterLink
  ],
  templateUrl: './cell-map-cell-analysis.html',
  styleUrl: './cell-map-cell-analysis.css'
})
export class CellMapCellAnalysis {

  @Input() open = signal<boolean>(false);
  @Output() closed = new EventEmitter<void>();
  @Output() openEngParaModify = new EventEmitter<void>();

  @Input() cellName: string = '';
  @Input() standardKpiName = signal<string | undefined>(undefined);
  @Input() ratName = signal<string | undefined>(undefined);
  @Input() granularityName = signal<string | undefined>(undefined);
  @Input() isEngineeringParaEditable:boolean = false;

  trendPeriod = signal<'month' | 'week' | 'quarter'>('month')

  chartSeries: any = null;

  loadingChart: boolean = false;
  private initialized = false;

  constructor(private router: Router,
              private sharedService: SharedService,
              private kpiDayService: KpidayService,
              private alertService: AlertService,
              private chartService: ChartService,) {

    effect(() => {
      if(!this.open() || this.initialized) return;

      this.queryTrendData(this.standardKpiName()!, this.cellName, this.trendPeriod(),this.ratName()!, this.granularityName()!);
      this.initialized = true;
    });
  }

  onCancel(): void {
    this.initialized = false;
    this.closed.emit();
  }

  onEditEngParams(): void {
    this.closed.emit();          // close this modal first (optional but clean UX)
    this.openEngParaModify.emit();
  }

  loadingAll() {
    return this.loadingChart ;
  }

  onPeriodChange($event: Event) {
    this.queryTrendData(this.standardKpiName()!, this.cellName,this.trendPeriod(),this.ratName()!, this.granularityName()!);
  }

  queryTrendData(kpiName: string, cellName: string, trendPeriod:string, ratName:string, granularityName:string) {
    this.chartSeries = [];
    const resolvedGranularity = this.resolveGranularity(trendPeriod, granularityName);
    this.loadingChart = true;
    this.getTrendDataByKpiName(kpiName, cellName, trendPeriod, ratName, resolvedGranularity)
      .subscribe({
        next: data => {
          this.chartSeries = this.chartService.buildSeriesKpiDataDto(data);
          this.loadingChart = false;
        }, error: err => {
          this.loadingChart = false;
          console.log("Error getDataByKpiLabelAndCell:");
          console.error(err);
          this.alertService.error('KPI Data retrieval failed', 'Error', `${err.status} ${err.statusText}`);
        }
      });
  }

  getTrendDataByKpiName(kpiName: string, cellName: string, period: string, ratName: string, granularityName: string): Observable<KpiDataDto[]> {
    return this.kpiDayService.getDataByKpiAndCell(kpiName, cellName, period, ratName, granularityName);
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
    this.sharedService.selectedCell.set(this.cellName);
    this.sharedService.returnPage = 'pulse/map';
  }

}
