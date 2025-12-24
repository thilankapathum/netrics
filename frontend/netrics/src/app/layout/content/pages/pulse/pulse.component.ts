import {ChangeDetectorRef, Component, effect, ElementRef, OnInit, signal, ViewChild} from '@angular/core';
import {CommonModule, DecimalPipe} from '@angular/common';
import {LineChart} from '../../../../components/charts/linechart/line-chart/line-chart';
import {BasickpiService} from '../../../../service/pulse/ltefdd/basickpi.service';
import {KpidayService} from '../../../../service/pulse/ltefdd/kpiday.service';
import {BasicKpiDto} from '../../../../models/pulse/BasicKpiDto';
import {BasicKpiSnapshot} from '../../../../models/pulse/BasicKpiSnapshot';
import {FormsModule} from '@angular/forms';
import {StandardKpiDto} from '../../../../models/pulse/StandardKpiDto';
import {StandardkpiService} from '../../../../service/pulse/ltefdd/standardkpi.service';
import {KpiTrendDto} from '../../../../models/pulse/KpiTrendDto';
import {KpiDataDto} from '../../../../models/pulse/KpiDataDto';
import {Linechart} from '../../../../components/charts/linechart/linechart/linechart';
import {forkJoin, Observable} from 'rxjs';
import {ChartService} from '../../../../service/components/chart/chart.service';
import {WorstCells} from '../../../../models/pulse/WorstCells';
import {AlertService} from '../../../../components/alert/alert.service';
import {DistrictDto} from '../../../../models/pulse/DistrictDto';
import {DistrictService} from '../../../../service/pulse/district/district.service';
import {KpiSnapshot} from '../../../../models/pulse/KpiSnapshot';
import {Router, RouterLink} from '@angular/router';
import {SharedService} from '../../../../service/pulse/shared-service';
import {DateRangeDto} from '../../../../models/pulse/DateRangeDto';
import {DateService} from '../../../../service/pulse/date-service';
import {AreaTypeDto} from '../../../../models/pulse/AreaTypeDto';
import {AreaDto} from '../../../../models/pulse/AreaDto';
import {AreaService} from '../../../../service/pulse/area-service';
import {AreaTypeService} from '../../../../service/pulse/area-type-service';
import {UserAreaService} from '../../../../service/pulse/user-area-service';
import {KeycloakProfile} from 'keycloak-js';
import {AuthService} from '../../../../auth/service/auth-service';

@Component({
  selector: 'app-pulse',
  standalone: true,
  imports: [CommonModule, LineChart, FormsModule, Linechart, DecimalPipe, DecimalPipe, DecimalPipe, RouterLink],
  templateUrl: './pulse.component.html',
  styleUrl: './pulse.component.css'
})
export class PulseComponent implements OnInit {

  selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');
  selectedGranularity = signal<'day-average' | 'busy-hour'>('day-average');
  dateRanges = signal<{ [aggregation: string]: DateRangeDto | undefined }>({});

  areaTypes: AreaTypeDto[] = [];
  areaType = signal<string | undefined>('');

  areas: AreaDto[] = [];
  area = signal<string | undefined>('')

  userArea = signal<AreaDto | undefined>(undefined);
  userProfile: KeycloakProfile = {};

  basicKpiDtos: BasicKpiDto[] = [];
  basicKpiSnapshots: BasicKpiSnapshot[] = [];
  aggregation = signal<'day' | 'week' | 'month'>('day')
  aggregationList: string[] = ['day', 'week', 'month'];
  standardKpis: StandardKpiDto[] = [];
  selectedStandardKpi = signal('');
  selectedKpiTrendPeriod = signal<'month' | 'week' | 'quarter'>('month')
  kpiTrendData: KpiTrendDto[] = [];
  chartSeries: any = null;
  analysisModalCell: string = '';
  analysisModalKpiLabel: string = '';

  currentPage: number = 0;
  pageSize: number = 5;
  totalPages: number = 0;
  allWorstCells: WorstCells[] = [];
  worstCells: WorstCells[] = [];
  excludeZeroes: boolean = false;

  loadingBasicKpi: boolean = false;
  loadingWorstCells: boolean = false;
  loadingKpiTrend: boolean = false;
  loadingAnalysisModalChart: boolean = false;

  @ViewChild('analysisModal') analysisModal!: ElementRef<HTMLDialogElement>;

  constructor(private cdr: ChangeDetectorRef,
              private basicKpiService: BasickpiService,
              private kpiDayService: KpidayService,
              private standardKpiService: StandardkpiService,
              private chartService: ChartService,
              private alertService: AlertService,
              private router: Router,
              private sharedService: SharedService,
              private dateService: DateService,
              private areaService: AreaService,
              private areaTypeService: AreaTypeService,
              private userAreaService: UserAreaService,
              private authService: AuthService) {
    this.queryDateRanges();

  }

  ngOnInit() {
    this.getUserProfile();
    // this.cdr.detectChanges(); // Force change detection
    //
    //
    // if (this.sharedService.selectedGranularity() != this.selectedGranularity()) {
    //   this.selectedGranularity.set(this.sharedService.selectedGranularity());
    // }
    //
    // if (this.sharedService.aggregation() != this.aggregation()) {
    //   this.aggregation.set(this.sharedService.aggregation())
    // }
    //
    // if (this.sharedService.selectedRat() != this.selectedRat()) {
    //   this.selectedRat.set(this.sharedService.selectedRat())
    // }
    //
    // this.getAreaTypes();
    // this.excludeZeroes = this.sharedService.excludeZeroes;

  }

  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
    this.getAreaByUserId(this.userProfile.id!);
  }

  getAreaByUserId(userId: string) {
    this.userAreaService.findByUserId(userId).subscribe({
      next: data => {
        this.userArea.set(data);
        this.ngOnInitRemaining();

      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting AreaByUserId)`);
      }
    })
  }

  ngOnInitRemaining(): void {
    this.cdr.detectChanges(); // Force change detection

    if (this.sharedService.selectedGranularity() != this.selectedGranularity()) {
      this.selectedGranularity.set(this.sharedService.selectedGranularity());
    }

    if (this.sharedService.aggregation() != this.aggregation()) {
      this.aggregation.set(this.sharedService.aggregation())
    }

    if (this.sharedService.selectedRat() != this.selectedRat()) {
      this.selectedRat.set(this.sharedService.selectedRat())
    }

    this.getAreaTypes();
    this.excludeZeroes = this.sharedService.excludeZeroes;
  }

  queryDateRanges() {
    for (let range of this.aggregationList) {
      this.getDateRanges(range, this.selectedRat(), this.selectedGranularity());
    }
  }

  getDateRanges(aggregation: string, selectedRat: string, granularityName: string) {
    this.dateService.getLatestDateRange(aggregation, selectedRat, granularityName).subscribe(
      {
        next: data => {
          this.dateRanges.update(range => ({
            ...range,
            [aggregation]: data
          }));
        }, error: err => {
          console.log('Error getting date range');
          console.error(err);
          this.alertService.error('Error getting date range');
        }
      }
    )
  }

  selectAreaType(areaType: string) {
    this.areaType.set(areaType);
    this.getAreasByAreaType(this.areaType()!);
  }

  selectArea(areaName: string) {
    this.area.set(areaName);
    this.cdr.detectChanges(); // Force change detection
    this.getBasicKpi(this.selectedRat());
    this.selectKpi(this.selectedStandardKpi(), this.selectedRat(), this.selectedGranularity());
  }

  selectAggregation(aggregation: 'day' | 'week' | 'month') {
    this.aggregation.set(aggregation);
    this.cdr.detectChanges(); // Force change detection
    this.getBasicKpi(this.selectedRat());
    this.selectKpi(this.selectedStandardKpi(), this.selectedRat(), this.selectedGranularity());
  }

  getAreaTypes() {
    this.areaTypeService.getAllAreaTypes().subscribe({
        next: data => {
          this.areaTypes = data;
          const districtsAreaType = this.areaTypes.find(at => at.name === 'District');
          // this.areaType.set(this.areaTypes.at(0)?.name);
          if (this.sharedService.areaType() != '') {
            this.areaType.set(this.sharedService.areaType());
          } else if (this.userArea() != null) {
            this.areaType.set(this.userArea()?.areaTypeName);
          } else {
            this.areaType.set(districtsAreaType?.name);
          }
          this.getAreasByAreaType(this.areaType()!);
        }, error: error => {
          console.log(error);
          this.alertService.error(`Error getting Area-types! (${error.status}:${error.statusText})`);
        }
      }
    )
  }


  getAreasByAreaType(areaTypeName: string) {
    this.areaService.getAreasByAreaTypes(areaTypeName).subscribe({
      next: data => {
        this.areas = data;
        if (this.sharedService.area() != '') {
          this.area.set(this.sharedService.area());
          this.sharedService.area.set('');
          this.sharedService.areaType.set('');
        } else if (this.userArea() != null) {
          this.area.set(this.userArea()?.name);
          this.userArea.set(undefined);   // Clear userArea details after initial loading
        } else {
          this.area.set(this.areas.at(0)?.name);
        }
        this.getBasicKpi(this.selectedRat());
        this.getAllStandardKpi(this.selectedRat());
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Areas! (${error.status}:${error.statusText})`);
      }
    })
  }

  setSelectedGranularity(granularity: 'day-average' | 'busy-hour') {
    this.selectedGranularity.set(granularity);

    this.getBasicKpi(this.selectedRat());
    this.getTrendDataByKpi(this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.selectedRat(), this.selectedGranularity());
    this.getWorstCellsByKpi(this.selectedStandardKpi(), this.aggregation(), this.selectedRat(), this.selectedGranularity());

  }

  setSelectedRat(rat: 'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'): void {
    this.selectedRat.set(rat);
    this.queryDateRanges();

    switch (rat) {
      case "ltefdd":
        this.getBasicKpi("ltefdd");
        this.getAllStandardKpi("ltefdd");
        break;
      case "ltetdd":
        this.getBasicKpi("ltetdd");
        this.getAllStandardKpi("ltetdd");
        break;
      case "nr":
        this.getBasicKpi("nr");
        this.getAllStandardKpi("nr");
        break;
      case "umts":
        this.getBasicKpi("umts");
        this.getAllStandardKpi("umts");
        break;
      case "gsm":
        this.getBasicKpi("gsm");
        this.getAllStandardKpi("gsm");
        break;
      default:
        this.getBasicKpi("ltefdd");
        this.getAllStandardKpi("ltefdd");
    }
  }


  isBasicKpiValueRed(snapshot: BasicKpiSnapshot): boolean {
    const basicKpi = this.basicKpiDtos.find(kpi => kpi.label == snapshot.kpiLabel);
    if (!basicKpi || basicKpi.threshold == null || !basicKpi.worstOrder) {
      return false;
    }
    if (basicKpi.worstOrder === 'ASC') {
      return snapshot.value! < basicKpi.threshold;
    }
    if (basicKpi.worstOrder === 'DESC') {
      return snapshot.value! > basicKpi.threshold;
    }
    return false;
  }

  isKpiValueRed(item: KpiSnapshot | WorstCells): boolean {
    const standardKpi = this.standardKpis.find(kpi => kpi.label == item.kpiLabel);
    if (!standardKpi || standardKpi.threshold == null || !standardKpi.worstOrder) {
      return false;
    }
    if (standardKpi.worstOrder === 'ASC') {
      return item.value! < standardKpi.threshold;
    }
    if (standardKpi.worstOrder === 'DESC') {
      return item.value! > standardKpi.threshold;
    }
    return false;
  }

  //------- BASIC KPI  ---------

  getBasicKpi(ratName: string) {
    this.loadingBasicKpi = true;
    this.basicKpiDtos = [];
    this.basicKpiSnapshots = [];
    this.basicKpiService.getAllBasicKpi(ratName).subscribe({
      next: data => {
        this.basicKpiDtos = data;
        this.getBasicKpiSnapshot(this.basicKpiDtos).subscribe({
          next: snapshots => {
            this.basicKpiSnapshots = snapshots
            this.loadingBasicKpi = false;
          }, error: error => {
            console.log("Error getAllBasicKpiSnapshots");
            console.error(error);
            this.alertService.error("Basic KPI Snapshot retrieval failed");
          }
        })
      }, error: error => {
        this.loadingBasicKpi = false;
        console.log("Error getAllBasicKpi");
        console.error(error);
        this.alertService.error("Basic KPI retrieval failed");
      }
    });
  }

  //------- BASIC KPI SNAPSHOTS ---------

  private getBasicKpiSnapshot(basicKpiDto: BasicKpiDto[]) {
    const requests = basicKpiDto.map(kpi =>
      // this.kpiDayService.getBasicKpiSnapshot(kpi.kpiName!, this.aggregation(), this.district(), this.selectedRat(), this.selectedGranularity())
      this.kpiDayService.getBasicKpiSnapshotByArea(kpi.kpiName!, this.aggregation(), this.area()!, this.selectedRat(), this.selectedGranularity())
    );
    return forkJoin([...requests]);
  }

  get sortedBasicKpiSnapshots(): BasicKpiSnapshot[] {
    return this.basicKpiSnapshots.sort((a, b) => a.kpiLabel!.localeCompare(b.kpiLabel!))
  }

  //----------- WORST CELLS ----------------

  onExcludeZeroesChange(event: Event) {
    this.getWorstCellsByKpi(this.selectedStandardKpi(), this.aggregation(), this.selectedRat(), this.selectedGranularity());
  }

  getWorstCellsByKpi(kpiName: string, aggregation: string, ratName: string, granularityName: string) {
    this.loadingWorstCells = true;
    this.allWorstCells = [];

    this.kpiDayService.getWorstCellsByKpiAndArea(kpiName, aggregation, this.excludeZeroes, 25, this.area()!, ratName, granularityName).subscribe({
      next: data => {
        this.allWorstCells = data;
        this.totalPages = Math.ceil(this.allWorstCells.length / this.pageSize);
        this.setPage(this.sharedService.currentPage);  //-- To visit prev. worst-cell page by back-navigation from /cell page
        this.sharedService.currentPage = 0;   //-- Do not convert sharedService.currentPage into a signal
        this.loadingWorstCells = false;
      },
      error: error => {
        console.error("Error getWorstCellsByKpi:", error);
        this.alertService.error("Worst cells retrieval failed");
        this.loadingWorstCells = false;
      }
    });
  }

  setPage(page: number) {
    if (page < 0 || page > this.totalPages) return;

    this.currentPage = page;
    const start = page * this.pageSize;
    const end = start + this.pageSize;
    this.worstCells = this.allWorstCells.slice(start, end)
  }

  nextPage() {
    this.setPage(this.currentPage + 1);
  }

  prevPage() {
    this.setPage(this.currentPage - 1);
  }

  selectKpi(kpi: string, ratName: string, granularityName: string) {
    this.currentPage = 0;
    // this.currentPage = this.sharedService.currentPage;
    this.getTrendDataByKpi(kpi, this.selectedKpiTrendPeriod(), ratName, granularityName);
    this.getWorstCellsByKpi(kpi, this.aggregation(), ratName, granularityName);
  }

  getAllStandardKpi(ratName: string) {
    this.standardKpis = [];
    this.standardKpiService.getAllStandardKpi(ratName).subscribe({
      next: data => {
        this.standardKpis = data;
        if (this.standardKpis.length > 0) {
          if (this.sharedService.selectedStandardKpi() != '' && this.selectedRat() === this.sharedService.selectedRat()) {
            this.selectedStandardKpi.set(this.sharedService.selectedStandardKpi());
          } else {
            this.selectedStandardKpi.set(this.standardKpis[0].kpiName!);
          }
          this.selectKpi(this.selectedStandardKpi(), ratName, this.selectedGranularity());    // Getting Worst-cells and Trend-data
        } else {
          this.alertService.error("KPI are unavailable for the RAT");
          this.kpiTrendData = [];
          this.allWorstCells = [];
          this.worstCells = [];
        }
      }, error: error => {
        console.log("Error getAllStandardKpi:");
        console.error(error);
        this.alertService.error("Standard KPI retrieval failed");
      }
    })
  }

  //---------- OPEN ANALYSIS MODAL (DIALOG) ----------------

  openAnalysisModal(kpiName: string, cellName: string) {
    this.analysisModalCell = cellName;
    this.analysisModalKpiLabel = kpiName;
    this.loadingAnalysisModalChart = true;
    this.analysisModal.nativeElement.showModal();
    this.getTrendDataByKpiNameAndCell(kpiName, cellName, 'quarter', this.selectedRat(), this.selectedGranularity())
      .subscribe({
        next: data => {
          this.chartSeries = this.chartService.buildSeriesKpiDataDto(data);
          this.loadingAnalysisModalChart = false;
        }, error: err => {
          this.loadingAnalysisModalChart = false;
          console.log("Error getDataByKpiLabelAndCell:");
          console.error(err);
          this.alertService.error("KPI Data retrieval failed");
        }
      })
  }

  //------------- KPI TREND CHART ----------------

  onPeriodChange(event: Event) {
    this.getTrendDataByKpi(this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.selectedRat(), this.selectedGranularity());
  }

  getTrendDataByKpi(kpiName: string, period: string, ratName: string, granularityName: string) {
    this.loadingKpiTrend = true;
    this.kpiTrendData = [];

    this.kpiDayService.getDataByKpiAndArea(kpiName, period, this.area()!, ratName, granularityName).subscribe({
      next: data => {
        this.kpiTrendData = data;
        this.loadingKpiTrend = false;
      },
      error: error => {
        console.log("Error getDataByKpi:");
        console.error(error);
        this.alertService.error("Trend data retrieval failed");
        this.loadingKpiTrend = false;
      }
    })
  }

  //============ MODAL KPI TREND CHART =================

  getTrendDataByKpiNameAndCell(kpiName: string, cellName: string, period: string, ratName: string, granularityName: string): Observable<KpiDataDto[]> {
    return this.kpiDayService.getDataByKpiAndCell(kpiName, cellName, period, ratName, granularityName);
  }

  //============ ROUTER-LINK ===========================

  getRouterLinkForCell(): string[] {
    return ['/pulse/cell']
  }

  onCellNavigate() {
    this.sharedService.selectedGranularity.set(this.selectedGranularity());
    this.sharedService.selectedRat.set(this.selectedRat());
    this.sharedService.selectedStandardKpi.set(this.selectedStandardKpi());
    this.sharedService.selectedCell.set(this.analysisModalCell);
    this.sharedService.aggregation.set(this.aggregation());
    // this.sharedService.district = this.district;
    this.sharedService.area.set(this.area());
    this.sharedService.areaType.set(this.areaType());
    this.sharedService.excludeZeroes = this.excludeZeroes;
    this.sharedService.currentPage = this.currentPage;
  }

  clearSharedServiceData(): void {
    this.sharedService.clearAll();
  }
}
