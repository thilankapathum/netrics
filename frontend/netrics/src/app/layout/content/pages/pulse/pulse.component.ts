import {ChangeDetectorRef, Component, ElementRef, OnInit, signal, ViewChild} from '@angular/core';
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
import {firstValueFrom, forkJoin, map, Observable} from 'rxjs';
import {ChartService} from '../../../../service/components/chart/chart.service';
import {WorstCells} from '../../../../models/pulse/WorstCells';
import {AlertService} from '../../../../components/alert/alert.service';
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
import {CellService} from '../../../../service/pulse/cell-service';
import {BandService} from '../../../../service/pulse/band-service';
import {BandDto} from '../../../../models/pulse/BandDto';
import {BandWorstCellsKpiTrend} from '../../../../models/pulse/BandWorstCellsKpiTrend';
import {BandKpiSeries} from '../../../../models/apexCharts/BandKpiSeries';
import {PsCells} from './pulse-settings/ps-cells/ps-cells';
import {StandardRawKpiMappingService} from '../../../../service/pulse/standard-raw-kpi-mapping-service';

@Component({
  selector: 'app-pulse',
  standalone: true,
  imports: [CommonModule, LineChart, FormsModule, Linechart, DecimalPipe, DecimalPipe, DecimalPipe, RouterLink, PsCells],
  templateUrl: './pulse.component.html',
  styleUrl: './pulse.component.css'
})
export class PulseComponent implements OnInit {

  // selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');
  // selectedGranularity = signal<'day-average' | 'busy-hour'>('day-average');
  selectedRat = signal<string>('ltefdd');
  selectedGranularity = signal<string>('day-average');
  dateRanges = signal<{ [aggregation: string]: DateRangeDto | undefined }>({});

  areaTypes: AreaTypeDto[] = [];
  areaType = signal<string | undefined>('');

  areas: AreaDto[] = [];
  area = signal<string | undefined>('')

  userArea = signal<AreaDto | undefined>(undefined);
  userProfile: KeycloakProfile = {};

  bands: BandDto[] = [];

  basicKpiDtos: BasicKpiDto[] = [];
  basicKpiSnapshots: BasicKpiSnapshot[] = [];
  aggregation = signal<'day' | 'week' | 'month'>('day')
  aggregationList: string[] = ['day', 'week', 'month'];
  standardKpis: StandardKpiDto[] = [];
  selectedStandardKpi = signal('');
  selectedKpiTrendPeriod = signal<'month' | 'week' | 'quarter'>('month')
  selectedKpiTrendPeriodModal = signal<'month' | 'week' | 'quarter'>('month')
  kpiTrendData: KpiTrendDto[] = [];
  bandKpiTrend = signal<KpiTrendDto[]>([]);
  bandWorstCellsKpiTrends: BandWorstCellsKpiTrend[] = [];
  chartSeries: any = null;
  bandChartSeries = signal<BandKpiSeries[]>([]);
  analysisModalCell: string = '';
  analysisModalKpiLabel: string = '';

  currentPage: number = 0;
  pageSize: number = 5;
  totalPages: number = 0;
  allWorstCells: WorstCells[] = [];   // Total 25 worst cells
  allBandWorstCells: WorstCells[] = [];
  worstCells: WorstCells[] = [];  // 5 Worst cells displayed in the page
  excludeZeroes: boolean = false;
  showOperands:boolean = false;
  standardRawKpiMappingAvailable = signal<boolean>(false);

  bandWise: boolean = false;
  selectedBand = signal('');

  missingCellInfoCount = signal(0);

  loadingBasicKpi: boolean = false;
  loadingWorstCells: boolean = false;
  loadingKpiTrend: boolean = false;
  loadingAnalysisModalChart: boolean = false;
  loadingAreaTypes: boolean = false;
  loadingAreas: boolean = false;
  loadingDateRanges: boolean = false;

  showMissingCellInfoModal: boolean = false;

  @ViewChild('analysisModal') analysisModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('cellMissingInfoModal') cellMissingInfoModal!: ElementRef<HTMLDialogElement>;

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
              private authService: AuthService,
              private cellService: CellService,
              private bandService: BandService,
              private standardRawKpiMappingService: StandardRawKpiMappingService) {
    this.queryDateRanges();
  }

  loadingAll() {
    return this.loadingBasicKpi || this.loadingWorstCells || this.loadingKpiTrend  || this.loadingAreaTypes || this.loadingAreas || this.loadingDateRanges;
  }

  queryDateRanges() {
    for (let range of this.aggregationList) {
      this.getDateRanges(range, this.selectedRat(), this.selectedGranularity());
    }
  }

  getDateRanges(aggregation: string, selectedRat: string, granularityName: string) {
    this.loadingDateRanges = true;
    this.dateService.getLatestDateRange(aggregation, selectedRat, granularityName).subscribe(
      {
        next: data => {
          this.dateRanges.update(range => ({
            ...range,
            [aggregation]: data
          }));
          this.loadingDateRanges = false;
        }, error: err => {
          console.log('Error getting date range');
          console.error(err);
          this.alertService.error(`Error getting date range. ${err.status} ${err.statusText}`);
          this.loadingDateRanges = false;
        }
      }
    )
  }

  ngOnInit() {
    this.getUserProfile();
  }

  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
    if (this.authService.hasRole('PULSE_CREATE')) {
      this.getCellCountWithMissingInfo();
    }
    this.getAreaByUserId(this.userProfile.id!);
  }

  getAreaByUserId(userId: string) {
    this.userAreaService.findByUserId(userId).subscribe({
      next: data => {
        this.userArea.set(data);
        this.ngOnInitRemaining();

      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Area. ${error.status} ${error.statusText}`);
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

    if (this.sharedService.bandWise != this.bandWise) {
      this.bandWise = this.sharedService.bandWise
    }

    if (this.sharedService.selectedBand() != this.selectedBand()) {
      this.selectedBand.set(this.sharedService.selectedBand());
    }

    this.getAreaTypes();
    this.excludeZeroes = this.sharedService.excludeZeroes;
  }


  //------------ FILTERS ---------------------------

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

  selectBand(bandName: string) {
    this.selectedBand.set(bandName);
    this.allWorstCells = [];
    if (bandName === '') {
      this.getWorstCellsByKpi(this.selectedStandardKpi(), this.aggregation(), this.selectedRat(), this.selectedGranularity());
    } else {
      const index = this.bandWorstCellsKpiTrends.findIndex(b => b.band?.name === bandName);
      if (index >= 0) {
        this.loadingWorstCells = true;
        this.kpiDayService.getWorstCellsByKpiAreaAndBand(this.selectedStandardKpi(), this.aggregation(), this.excludeZeroes, 25, this.area()!, this.selectedRat(), this.selectedGranularity(), bandName).subscribe({
          next: data => {
            this.bandWorstCellsKpiTrends[index] = {
              ...this.bandWorstCellsKpiTrends[index],
              worstCells: data
            }
            console.log(this.bandWorstCellsKpiTrends[index]);
            this.allWorstCells = this.bandWorstCellsKpiTrends[index].worstCells!;
            this.totalPages = Math.ceil(this.allWorstCells.length / this.pageSize);
            this.setPage(this.sharedService.currentPage);  //-- To visit prev. worst-cell page by back-navigation from /cell page
            this.sharedService.currentPage = 0;
            this.loadingWorstCells = false;
          }, error: error => {
            console.error(error);
            this.alertService.error(`Error getting Worst Cells. ${error.status} ${error.statusText}`);
            this.loadingWorstCells = false;
          }
        });
      }
    }
  }

  setSelectedGranularity(granularity: 'day-average' | 'busy-hour') {
    this.selectedGranularity.set(granularity);
    this.getBasicKpi(this.selectedRat());
    this.getWorstCellsAndKpiTrends(this.bandWise, this.selectedBand(), this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.selectedRat(), this.excludeZeroes, this.selectedGranularity(), this.area()!, this.aggregation());
  }

  async setSelectedRat(rat: 'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'): Promise<void> {
    this.selectedRat.set(rat);
    this.queryDateRanges();

    this.bands = [];
    this.selectedBand.set('');
    try {
      this.bands = await firstValueFrom(this.bandService.getByRatName(rat));
    } catch (error) {
      console.error('error retrieving bands', error);
      this.alertService.error(`Error retrieving Bands.`)
    }
    this.getBasicKpi(rat);
    this.getAllStandardKpi(rat);
  }

  //----------- GETTERS ----------------------------------

  getAreaTypes() {
    this.loadingAreaTypes = true;
    this.areaTypeService.getAllAreaTypes().subscribe({
        next: data => {
          this.areaTypes = data;
          const districtsAreaType = this.areaTypes.find(at => at.name === 'District');
          if (this.sharedService.areaType() != '') {
            this.areaType.set(this.sharedService.areaType());
          } else if (this.userArea() != null) {
            this.areaType.set(this.userArea()?.areaTypeName);
          } else {
            this.areaType.set(districtsAreaType?.name);
          }
          this.getAreasByAreaType(this.areaType()!);
          this.loadingAreaTypes = false;
        }, error: error => {
          console.log(error);
          this.alertService.error(`Error getting Area-types. (${error.status}:${error.statusText})`);
          this.loadingAreaTypes = false;
        }
      }
    )
  }


  getAreasByAreaType(areaTypeName: string) {
    this.loadingAreas = true;
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
        this.loadingAreas = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Areas. (${error.status}:${error.statusText})`);
        this.loadingAreas = false;
      }
    })
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
            this.alertService.error(`Basic KPI Snapshot retrieval failed. ${error.status} ${error.statusText}`);
          }
        })
      }, error: error => {
        this.loadingBasicKpi = false;
        console.log("Error getAllBasicKpi");
        console.error(error);
        this.alertService.error(`Basic KPI retrieval failed. ${error.status} ${error.statusText}`);
      }
    });
  }

  //------- BASIC KPI SNAPSHOTS ---------

  private getBasicKpiSnapshot(basicKpiDto: BasicKpiDto[]) {
    const requests = basicKpiDto.map(kpi =>
      this.kpiDayService.getBasicKpiSnapshotByArea(kpi.kpiName!, this.aggregation(), this.area()!, this.selectedRat(), this.selectedGranularity())
    );
    return forkJoin([...requests]);
  }

  get sortedBasicKpiSnapshots(): BasicKpiSnapshot[] {
    return this.basicKpiSnapshots.sort((a, b) => a.kpiLabel!.localeCompare(b.kpiLabel!))
  }

  //----------- STANDARD KPI ---------------

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
          this.alertService.error(`KPI are unavailable for the RAT.`);
          this.kpiTrendData = [];
          this.allBandWorstCells = [];
          this.allWorstCells = [];
          this.worstCells = [];
        }
      }, error: error => {
        console.log("Error getAllStandardKpi:");
        console.error(error);
        this.alertService.error(`Standard KPI retrieval failed. ${error.status} ${error.statusText}`);
      }
    })
  }

  async selectKpi(kpi: string, ratName: string, granularityName: string) {
    this.currentPage = 0;
    await this.getWorstCellsAndKpiTrends(this.bandWise, this.selectedBand(), kpi, this.selectedKpiTrendPeriod(), ratName, this.excludeZeroes, granularityName, this.area()!, this.aggregation());
  }

  //----------- WORST CELLS + KPI TRENDS -----------

  async getWorstCellsAndKpiTrends(bandWise: boolean, band: string, kpi: string, kpiTrendPeriod: string, ratName: string, excludeZeroes: boolean, granularityName: string, areaName: string, aggregation: string) {
    if (bandWise) {
      try {
        this.bands = await firstValueFrom(this.bandService.getByRatName(ratName));
      } catch (error) {
        console.error('error retrieving bands', error);
        this.alertService.error(`Error retrieving Bands.`)
      }

      if (band != null && band != '' && band != undefined) {   // Checking whether any band is selected/filtered
        this.getTrendDataByKpiForAllBands(this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.area()!, this.selectedRat(), this.selectedGranularity());
        this.selectBand(band);  // Querying band's worst-cells
      } else {
        this.selectedBand.set(band);
        this.getWorstCellsByKpi(kpi, aggregation, ratName, granularityName);
        this.getTrendDataByKpiForAllBands(this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.area()!, this.selectedRat(), this.selectedGranularity());
      }
    } else {
      this.selectedBand.set('');
      this.getTrendDataByKpi(kpi, kpiTrendPeriod, ratName, granularityName);
      this.getWorstCellsByKpi(kpi, aggregation, ratName, granularityName);
    }
  }

  //----------- KPI TRENDS -----------------


  getTrendDataByKpiForAllBands(kpiName: string, kpiTrendPeriod: string, areaName: string, ratName: string, granularityName: string
  ) {
    this.loadingKpiTrend = true;
    this.bandChartSeries.set([]);

    const observables = this.bands.map(band => {
      let index = this.bandWorstCellsKpiTrends.findIndex(b => b.band?.name === band.name);
      if (index === -1) {
        this.bandWorstCellsKpiTrends.push({band});
        index = this.bandWorstCellsKpiTrends.findIndex(b => b.band?.name === band.name);
      }

      return this.kpiDayService.getDataByKpiAreaAndBand(
        kpiName,
        kpiTrendPeriod,
        areaName,
        ratName,
        granularityName,
        band.name!
      ).pipe(
        map(data => ({data, band, index}))
      );
    });

    forkJoin(observables).subscribe({
      next: results => {
        results.forEach(({data, band, index}) => {
          this.bandWorstCellsKpiTrends[index] = {
            ...this.bandWorstCellsKpiTrends[index],
            kpiTrend: data
          };

          const seriesForBand = this.chartService.buildSeriesForBand(
            this.bandWorstCellsKpiTrends[index].kpiTrend!,
            this.bandWorstCellsKpiTrends[index].band!
          );

          this.bandChartSeries.update(existing => [...existing, ...seriesForBand]);
        });

        this.loadingKpiTrend = false;
      },
      error: error => {
        console.error('Error getting KPI Trend Data:', error);
        this.alertService.error(`KPI Trend Data retrieval failed. ${error.status} ${error.statusText}`);
        this.loadingKpiTrend = false;
      }
    });
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
        this.alertService.error(`Trend data retrieval failed. ${error.status} ${error.statusText}`);
        this.loadingKpiTrend = false;
      }
    })
  }

  //----------- WORST CELLS ----------------

  getWorstCellsByKpi(kpiName: string, aggregation: string, ratName: string, granularityName: string) {
    this.loadingWorstCells = true;
    this.allBandWorstCells = [];
    this.allWorstCells = [];

    this.kpiDayService.getWorstCellsByKpiAndArea(kpiName, aggregation, this.excludeZeroes, 25, this.area()!, ratName, granularityName).subscribe({
      next: data => {
        this.allBandWorstCells = data;
        this.allWorstCells = this.allBandWorstCells;
        this.totalPages = Math.ceil(this.allWorstCells.length / this.pageSize);
        this.setPage(this.sharedService.currentPage);  //-- To visit prev. worst-cell page by back-navigation from /cell page
        this.sharedService.currentPage = 0;   //-- Do not convert sharedService.currentPage into a signal
        this.loadingWorstCells = false;
      },
      error: error => {
        console.error("Error getWorstCellsByKpi:", error);
        this.alertService.error(`Worst cells retrieval failed. ${error.status} ${error.statusText}`);
        this.loadingWorstCells = false;
      }
    });
  }

  getStandardRawKpiMappingAvailable(ratName:string, standardKpiName:string) {
    this.standardRawKpiMappingService.isMappingAvailable(ratName, standardKpiName).subscribe({
      next: data => {
        this.standardRawKpiMappingAvailable.set(data);
      }, error: error => {
        console.error("Error getting standardRawKpiMappingAvailable:", error);
        this.alertService.error(`Standard-Raw-KPI-Mapping retrieval failed :"${error.status} ${error.statusText}`);
      }
    })
  }

  //------------------ CHANGE SWITCHES ----------------------------------

  onExcludeZeroesChange(event: Event) {
    this.selectBand(this.selectedBand());
  }

  onBandWiseChange(event: Event) {
    this.getWorstCellsAndKpiTrends(this.bandWise, this.selectedBand(), this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.selectedRat(), this.excludeZeroes, this.selectedGranularity(), this.area()!, this.aggregation())
  }

  onPeriodChange(event: Event) {
    if (this.bandWise) {
      this.getTrendDataByKpiForAllBands(this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.area()!, this.selectedRat(), this.selectedGranularity());
    } else {
      this.getTrendDataByKpi(this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.selectedRat(), this.selectedGranularity());
    }
  }

  onPeriodChangeModal($event: Event) {
    this.queryModalTrendData(this.selectedStandardKpi(), this.analysisModalCell, this.selectedKpiTrendPeriodModal(), this.selectedRat(), this.selectedGranularity());
  }

  onShowOperandsChangeModal($event: Event) {
    this.queryModalTrendData(this.selectedStandardKpi(), this.analysisModalCell, this.selectedKpiTrendPeriodModal(), this.selectedRat(), this.selectedGranularity());
  }

  //------------------- WORST-CELL PAGINATION -----------------------------

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


  //---------- OPEN ANALYSIS MODAL (DIALOG) ----------------

  openAnalysisModal(kpiName: string, cellName: string) {
    this.analysisModalCell = cellName;
    this.analysisModalKpiLabel = kpiName;
    this.loadingAnalysisModalChart = true;
    this.analysisModal.nativeElement.showModal();
    this.selectedKpiTrendPeriodModal.set('month');
    this.getStandardRawKpiMappingAvailable(this.selectedRat(), this.selectedStandardKpi());
    this.showOperands = false;
    this.queryModalTrendData(kpiName, cellName, 'month', this.selectedRat(), this.selectedGranularity());
  }

  onNextCell(){
    if (this.isNextCellAvailable(this.analysisModalCell, this.allWorstCells)) {
      let currentIndex = this.extractCurrentCellIndex(this.analysisModalCell, this.allWorstCells);
      this.analysisModalCell = this.allWorstCells[currentIndex + 1].cellName!;
      this.queryModalTrendData(this.selectedStandardKpi(), this.analysisModalCell, this.selectedKpiTrendPeriodModal(), this.selectedRat(), this.selectedGranularity());
    }
  }

  onPrevCell(){
    if (this.isPrevCellAvailable(this.analysisModalCell, this.allWorstCells)) {
      let currentIndex = this.extractCurrentCellIndex(this.analysisModalCell, this.allWorstCells);
      this.analysisModalCell = this.allWorstCells[currentIndex - 1].cellName!;
      this.queryModalTrendData(this.selectedStandardKpi(), this.analysisModalCell, this.selectedKpiTrendPeriodModal(), this.selectedRat(), this.selectedGranularity());
    }
  }


  //============ MODAL KPI TREND CHART =================

  private queryModalTrendData(kpiName: string, cellName: string, trendPeriod: string, ratName: string, granularityName: string) {
    if (this.showOperands) {
      this.queryModalTrendDataByKpiNameAndCellWithOperands(kpiName, cellName, trendPeriod, ratName, granularityName);
    } else {
      this.queryModalTrendDataByKpiNameAndCell(kpiName, cellName, trendPeriod, ratName, granularityName);
    }
  }

  queryModalTrendDataByKpiNameAndCell(kpiName: string, cellName: string, trendPeriod:string, ratName:string, granularityName:string) {
    this.chartSeries = [];
    const resolvedGranularity = this.resolveGranularity(trendPeriod, granularityName);
    this.loadingAnalysisModalChart = true;
    this.getTrendDataByKpiNameAndCell(kpiName, cellName, trendPeriod, ratName, resolvedGranularity)
      .subscribe({
        next: data => {
          this.chartSeries = this.chartService.buildSeriesKpiDataDto(data);
          this.loadingAnalysisModalChart = false;
        }, error: error => {
          this.loadingAnalysisModalChart = false;
          console.log("Error getDataByKpiLabelAndCell:");
          console.error(error);
          this.alertService.error(`KPI Data retrieval failed. ${error.status} ${error.statusText}`);
        }
      });
  }

  queryModalTrendDataByKpiNameAndCellWithOperands(kpiName: string, cellName: string, trendPeriod:string, ratName:string, granularityName:string) {
    this.chartSeries = [];
    const resolvedGranularity = this.resolveGranularity(trendPeriod, granularityName);
    this.loadingAnalysisModalChart = true;
    this.getTrendDataByKpiNameAndCellWithOperands(kpiName, cellName, trendPeriod, ratName, resolvedGranularity)
      .subscribe({
        next: data => {
          this.chartSeries = this.chartService.buildSeriesKpiDataWithOperandsDto(data);
          this.loadingAnalysisModalChart = false;
        },
        error: error => {
          this.loadingAnalysisModalChart = false;
          console.log("Error getTrendDataByKpiNameAndCellWithOperands:");
          console.error(error);
          this.alertService.error(`KPI Data retrieval failed. ${error.status} ${error.statusText}`);
        }
      });
  }

  getTrendDataByKpiNameAndCell(kpiName: string, cellName: string, period: string, ratName: string, granularityName: string): Observable<KpiDataDto[]> {
    return this.kpiDayService.getDataByKpiAndCell(kpiName, cellName, period, ratName, granularityName);
  }

  getTrendDataByKpiNameAndCellWithOperands(kpiName: string, cellName: string, period: string, ratName: string, granularityName: string): Observable<KpiDataDto[]> {
    return this.kpiDayService.getDataByKpiAndCellWithOperands(kpiName, cellName, period, ratName, granularityName);
  }



  getCellCountWithMissingInfo() {
    this.cellService.getCellCountWithMissingInfo().subscribe({
      next: data => {
        this.missingCellInfoCount.set(data);
        if (this.missingCellInfoCount() > 0) {
          this.openMissingCellInfoModal();
        }
      },
      error: error => {
        console.log("Error getCellCountWithMissingInfo:");
        console.error(error);
        this.alertService.error(`Error retrieving Cell count with missing information. ${error.status} ${error.statusText}`);
      }
    })
  }

  //============ AUTH SERVICE ==========================

  hasAnyRole(roles: string[]) {
    return this.authService.hasAnyRole(roles);
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
    this.sharedService.area.set(this.area());
    this.sharedService.areaType.set(this.areaType());
    this.sharedService.excludeZeroes = this.excludeZeroes;
    this.sharedService.currentPage = this.currentPage;
    this.sharedService.bandWise = this.bandWise;
    this.sharedService.selectedBand.set(this.selectedBand());
    this.sharedService.returnPage = 'pulse';
  }

  clearSharedServiceData(): void {
    this.sharedService.clearAll();
  }

  //-------------------- UTILITY ----------------------

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

  private resolveGranularity(period: string, selectedGranularity: string): string {
    return period === 'week' ? 'hour' : selectedGranularity;
  }

   isNextCellAvailable(cellName:string, worstCellList: WorstCells[]){
    const currentIndex = this.extractCurrentCellIndex(cellName, worstCellList);
    return currentIndex + 1 < worstCellList.length;
  }

   isPrevCellAvailable(cellName:string, worstCellList: WorstCells[]){
    const currentIndex = this.extractCurrentCellIndex(cellName, worstCellList);
    return currentIndex > 0;
  }

   extractCurrentCellIndex(cellName:string, worstCellList: WorstCells[]){
    return worstCellList.findIndex(cN => cN.cellName === cellName);
  }

  //----------------- MODALS -------------------------------
  openMissingCellInfoModal() {
    this.showMissingCellInfoModal = true;
  }

  closeMissingCellInfoModal() {
    this.showMissingCellInfoModal = false;
  }

  //----------------- SEVERITY INDICATIONS -----------------
  severityDotClass(severity: string | null | undefined): string {
    switch (severity) {
      case 'critical': return 'bg-error';
      case 'high':      return 'bg-warning';
      case 'moderate':  return 'bg-warning opacity-50';
      default:          return '';
    }
  }

  severityBorderClass(severity: string | null | undefined): string {
    switch (severity) {
      case 'critical': return 'border-l-error bg-error-content/50';
      case 'high':      return 'border-l-warning bg-warning-content/50';
      case 'moderate':  return 'border-l-warning/40 bg-warning-content/20';
      default:          return 'border-l-transparent';
    }
  }

  severityLabel(severity: string | null | undefined): string {
    switch (severity) {
      case 'critical': return 'Critical anomaly — statistically extreme deviation';
      case 'high':      return 'High anomaly — significant deviation from baseline';
      case 'moderate':  return 'Moderate anomaly — notable deviation from baseline';
      default:          return '';
    }
  }

  get modalYAxis(): ApexYAxis[] | undefined {
    if (!this.showOperands) return undefined;
    return [
      { seriesName: 'KPI Value', title: { text: 'KPI Value' } },
      { seriesName: 'Numerator', opposite: true, title: { text: 'Count' } },
      { seriesName: 'Denominator', opposite: true, show: false }
    ];
  }

}
