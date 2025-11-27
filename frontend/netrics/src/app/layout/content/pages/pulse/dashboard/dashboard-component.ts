import {Component, OnInit, signal} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {FormBuilder, FormsModule} from '@angular/forms';
import {AreaTypeDto} from '../../../../../models/pulse/AreaTypeDto';
import {AreaTypeService} from '../../../../../service/pulse/area-type-service';
import {AlertService} from '../../../../../components/alert/alert.service';
import {AreaDto} from '../../../../../models/pulse/AreaDto';
import {AreaService} from '../../../../../service/pulse/area-service';
import {DashboardService} from '../../../../../service/pulse/dashboard/dashboard-service';
import {StandardKpiDto} from '../../../../../models/pulse/StandardKpiDto';
import {StandardkpiService} from '../../../../../service/pulse/ltefdd/standardkpi.service';
import {WorstCellsWithLatestDto} from '../../../../../models/pulse/WorstCellsWithLatestDto';
import {DatePipe, DecimalPipe} from '@angular/common';
import {LineChart} from '../../../../../components/charts/linechart/line-chart/line-chart';
import {KpiTrendDto} from '../../../../../models/pulse/KpiTrendDto';
import {KpidayService} from '../../../../../service/pulse/ltefdd/kpiday.service';
import {Observable} from 'rxjs';
import {KpiDataDto} from '../../../../../models/pulse/KpiDataDto';
import {ChartService} from '../../../../../service/components/chart/chart.service';
import {Linechart} from '../../../../../components/charts/linechart/linechart/linechart';

@Component({
  selector: 'app-dashboard-component',
  imports: [
    RouterLink,
    FormsModule,
    DatePipe,
    DecimalPipe,
    Linechart
  ],
  providers: [DatePipe],
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.css'
})
export class DashboardComponent implements OnInit {

  areaTypes: AreaTypeDto[] = [];
  areaType = signal<string | undefined>('');

  areas: AreaDto[] = [];
  area = signal<string | undefined>('')

  timestamps: Date[] = [];
  timestamp = signal<Date>(new Date());

  standardKpis: StandardKpiDto[] = [];
  selectedStandardKpi = signal('');

  selectedPeriod = signal('day');
  selectedKpiTrendPeriod = signal<'month' | 'week' | 'quarter'>('month')

  selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');

  selectedCell=signal('');

  worstCells: Array<WorstCellsWithLatestDto> = [];
  kpiTrendData: KpiTrendDto[] = [];
  chartSeries: any = null;

  loadingKpiTrend: boolean = false;
  loadingWorstCells: boolean = false;
  excludeZeroes: boolean = false;


  constructor(private router: Router,
              private areaTypeService: AreaTypeService,
              private areaService: AreaService,
              private dashboardService: DashboardService,
              private standardKpiService: StandardkpiService,
              private alertService: AlertService,
              private datePipe: DatePipe,
              private kpiDayService:KpidayService,
              private chartService: ChartService
              ) {
  }

  ngOnInit(): void {
    // this.getAreaTypes();
    this.getAllStandardKpi(this.selectedRat())
  }

  getAllStandardKpi(ratName: string) {
    this.standardKpis = [];
    this.standardKpiService.getAllStandardKpi(ratName).subscribe({
      next: data => {
        this.standardKpis = data;
        if (this.standardKpis.length > 0) {
          this.selectedStandardKpi.set(this.standardKpis[0].kpiName!);
          this.getAreaTypes();
          // this.selectKpi(this.selectedStandardKpi(), ratName);    // Getting Worst-cells and Trend-data
        } else {
          this.alertService.error("KPI are unavailable for the RAT");
        }
      }, error: error => {
        console.log("Error getAllStandardKpi:");
        console.error(error);
        this.alertService.error("Standard KPI retrieval failed");
      }
    })
  }

  getAreaTypes() {
    this.areaTypeService.getAllAreaTypes().subscribe({
        next: data => {
          this.areaTypes = data;
          this.areaType.set(this.areaTypes.at(0)?.name);
          this.getAreasByAreaType(this.areaType()!);
        }, error: error => {
          console.log(error);
          this.alertService.error('Error getting areaTypes');
        }
      }
    )
  }

  getAreasByAreaType(areaTypeName: string) {
    this.areaService.getAreasByAreaTypes(areaTypeName).subscribe({
      next: data => {
        this.areas = data;
        this.area.set(this.areas.at(0)?.name);
        this.getTimestamps(this.selectedStandardKpi(), this.selectedPeriod(), this.area()!, this.selectedRat());
      }, error: error => {
        console.log(error);
        this.alertService.error('Error getting areasByAreaType');
      }
    })
  }

  getTimestamps(kpiName: string, period: string, areaName: string, ratName: string) {
    this.dashboardService.getTimestamps(kpiName, period, areaName, ratName).subscribe({
      next: data => {
        console.log("Timestamps data: ", data);
        this.timestamps = data;
        this.timestamp.set(this.timestamps.at(0)!);
        this.getWorstCells(this.timestamp(), this.excludeZeroes);
      }, error: error => {
        console.log(error);
        this.alertService.error('Error getting timestamps');
      }
    })
  }

  selectStandardKpi(standardKpiName: string) {
    this.getTimestamps(standardKpiName, this.selectedPeriod(), this.area()!, this.selectedRat());
  }

  selectAreaType(areaType: string) {
    this.areaType.set(areaType);
    this.getAreasByAreaType(this.areaType()!);
  }

  selectArea(areaName: string) {
    this.area.set(areaName);
    this.getTimestamps(this.selectedStandardKpi(), this.selectedPeriod(), this.area()!, this.selectedRat());
    console.log(areaName);
  }

  //--------- WORST-CELLS ------------------------

  onExcludeZeroesChange(event: Event) {
    this.getWorstCells(this.timestamp(), this.excludeZeroes);
  }

  getWorstCells(date: Date, excludeZeroes:boolean) {
    this.loadingWorstCells = true;
    this.dashboardService.getWorstCellsByKpiAndArea(
      this.datePipe.transform(date, 'yyyy-MM-dd')!,
      this.selectedStandardKpi(),
      this.selectedPeriod(),
      this.area()!,
      excludeZeroes,
      this.selectedRat())
      .subscribe({
        next: data => {
          this.worstCells = data;
          console.log("Worst cell data: ", data);
          this.loadingWorstCells = false;
        }, error: error => {
          console.log(error);
          this.loadingWorstCells = false;
          this.alertService.error('Error getting worstCellsByKpiAndArea');
        }
      });
  }


  setSelectedRat(rat: 'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'): void {
    this.selectedRat.set(rat);
    // this.queryDateRanges();

    switch (rat) {
      case "ltefdd":
        this.getAllStandardKpi('ltefdd');
        break;
      case "ltetdd":
        this.getAllStandardKpi('ltetdd');
        break;
      case "nr":
        this.getAllStandardKpi('nr');
        break;
      case "umts":
        this.getAllStandardKpi('umts');
        break;
      case "gsm":
        this.getAllStandardKpi('gsm');
        break;
      default:
        this.getAllStandardKpi('ltefdd');
    }
  }

  //----------- KPI TREND CHART ----------------------

  onPeriodChange(event: Event) {
    // this.getTrendDataByKpi(this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.selectedRat());
  }

  getTrendDataByKpi(kpiName: string, cellName:string, period: string, ratName: string) {
    this.selectedCell.set(cellName);
    this.loadingKpiTrend = true;
    this.kpiTrendData = [];
    this.getTrendDataByKpiNameAndCell(kpiName, cellName, 'quarter', this.selectedRat())
      .subscribe({
        next: data => {
          this.chartSeries = this.chartService.buildSeriesKpiDataDto(data);
          this.loadingKpiTrend = false;
        }, error: err => {
          this.loadingKpiTrend = false;
          console.log("Error getDataByKpiLabelAndCell:");
          console.error(err);
          this.alertService.error("KPI Data retrieval failed");
        }
      })
  }

  getTrendDataByKpiNameAndCell(kpiName: string, cellName: string, period: string, ratName: string): Observable<KpiDataDto[]> {
    return this.kpiDayService.getDataByKpiAndCell(kpiName, cellName, period, ratName);
  }

  //----------- UTILITY ------------------------------

  isKpiValueRed(kpiLabel:string , value:number): boolean {
    const standardKpi = this.standardKpis.find(kpi => kpi.label == kpiLabel);
    if (!standardKpi || standardKpi.threshold == null || !standardKpi.worstOrder) {
      return false;
    }
    if (standardKpi.worstOrder === 'ASC') {
      return value! < standardKpi.threshold;
    }
    if (standardKpi.worstOrder === 'DESC') {
      return value! > standardKpi.threshold;
    }
    return false;
  }

}
