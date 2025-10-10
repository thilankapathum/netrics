import {ChangeDetectorRef, Component, effect, ElementRef, OnInit, signal, ViewChild} from '@angular/core';
import {CommonModule, DecimalPipe} from '@angular/common';
import {LineChart} from '../../../../components/charts/linechart/line-chart/line-chart';
import {LtefddbasickpiService} from '../../../../service/pulse/ltefdd/ltefddbasickpi.service';
import {LtefdddayService} from '../../../../service/pulse/ltefdd/ltefddday.service';
import {BasicKpiDto} from '../../../../models/pulse/BasicKpiDto';
import {BasicKpiSnapshot} from '../../../../models/pulse/BasicKpiSnapshot';
import {FormsModule} from '@angular/forms';
import {StandardKpiDto} from '../../../../models/pulse/StandardKpiDto';
import {LtefddstandardkpiService} from '../../../../service/pulse/ltefdd/ltefddstandardkpi.service';
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
import {WorstCell} from '../../../../models/pulse/WorstCell';
import {Router, RouterLink} from '@angular/router';

@Component({
  selector: 'app-pulse',
  standalone: true,
  imports: [CommonModule, LineChart, FormsModule, Linechart, DecimalPipe, DecimalPipe, DecimalPipe, RouterLink],
  templateUrl: './pulse.component.html',
  styleUrl: './pulse.component.css'
})
export class PulseComponent implements OnInit {

  selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');

  basicKpiDtos: BasicKpiDto[] = [];
  basicKpiSnapshots: BasicKpiSnapshot[] = [];
  granularity: string = 'day';
  standardKpis: StandardKpiDto[] = [];
  selectedStandardKpi: string = '';
  selectedKpiTrendPeriod: string = 'month';
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

  districts: DistrictDto[] = [{name: 'All Districts', code: 'ALLDIST'}];
  district: string = 'All Districts';

  loadingBasicKpi: boolean = false;
  loadingWorstCells: boolean = false;
  loadingKpiTrend: boolean = false;

  @ViewChild('analysisModal') analysisModal!: ElementRef<HTMLDialogElement>;

  constructor(private cdr: ChangeDetectorRef,
              private ltefddbasickpiservice: LtefddbasickpiService,
              private ltefdddayservice: LtefdddayService,
              private ltefddstandardkpiservice: LtefddstandardkpiService,
              private chartService: ChartService,
              private alertService: AlertService,
              private districtService: DistrictService,
              private router: Router) {
  }

  ngOnInit() {
    this.cdr.detectChanges(); // Force change detection
    this.getAllDistricts();
    this.getAllBasicKpi();
    this.getAllStandardKpi();
  }

  selectGranularity(granularity: string) {
    this.granularity = granularity;
    this.cdr.detectChanges(); // Force change detection
    this.getAllBasicKpi();
    this.selectKpi(this.selectedStandardKpi);
  }

  selectDistrict(district: string) {
    this.district = district;
    this.cdr.detectChanges(); // Force change detection
    this.getAllBasicKpi();
    this.selectKpi(this.selectedStandardKpi);
  }

  getAllDistricts() {
    this.districts = [{name: 'All Districts', code: 'ALLDIST'}];
    this.districtService.getAllDistricts().subscribe({
      next: data => {
        for (let d of data) {
          this.districts.push(d);
        }
      }, error: error => {
        console.log("Error getAllDistricts");
        console.error(error);
        this.alertService.error("District retrieval failed");
      }
    });
  }

  setSelectedRat(rat: 'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'): void {
    this.selectedRat.set(rat);
    // console.log(this.selectedRat());

    switch (rat) {
      case "ltefdd":
        this.getLteFddBasicKpi();
        this.getLteFddTrendDataByKpi(this.selectedStandardKpi, this.selectedKpiTrendPeriod);
        this.getLteFddWorstCellsByKpi(this.selectedStandardKpi, this.granularity);
        break;
      case "ltetdd":
        this.getLteTddBasicKpi();
        this.getLteTddTrendDataByKpi(this.selectedStandardKpi, this.selectedKpiTrendPeriod);
        this.getLteTddWorstCellsByKpi(this.selectedStandardKpi, this.granularity);
        break;
      case "nr":
        this.getNrBasicKpi();
        this.getNrTrendDataByKpi(this.selectedStandardKpi, this.selectedKpiTrendPeriod);
        this.getNrWorstCellsByKpi(this.selectedStandardKpi, this.granularity);
        break;
      case "umts":
        this.getUmtsBasicKpi();
        this.getUmtsTrendDataByKpi(this.selectedStandardKpi, this.selectedKpiTrendPeriod);
        this.getUmtsWorstCellsByKpi(this.selectedStandardKpi, this.granularity);
        break;
      case "gsm":
        this.getGsmBasicKpi();
        this.getGsmTrendDataByKpi(this.selectedStandardKpi, this.selectedKpiTrendPeriod);
        this.getGsmWorstCellsByKpi(this.selectedStandardKpi, this.granularity);
        break;
      default:
        this.getLteFddBasicKpi();
        this.getLteFddTrendDataByKpi(this.selectedStandardKpi, this.selectedKpiTrendPeriod);
        this.getLteFddWorstCellsByKpi(this.selectedStandardKpi, this.granularity);
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

  getAllBasicKpi() {
    //TODO: This method may not be required except for OnInit
    this.loadingBasicKpi = true;
    this.basicKpiSnapshots = [];

    let selectedRat1 = this.selectedRat();
    if (selectedRat1 === "ltefdd") {
      this.getLteFddBasicKpi();
    } else if (selectedRat1 === "ltetdd") {
      this.getLteTddBasicKpi();
    } else if (selectedRat1 === "umts") {
      this.getUmtsBasicKpi();
    } else if (selectedRat1 === "gsm") {
      this.getGsmBasicKpi();
    } else {
      this.getLteFddBasicKpi();
    }
  }

  getLteFddBasicKpi() {
    console.log('Get LteFDDBasicKpi');

    this.ltefddbasickpiservice.getAllBasicKpi().subscribe({
      next: data => {
        this.basicKpiDtos = data;
        this.getLteFddBasicKpiSnapshot(this.basicKpiDtos).subscribe({
          next: snapshots => {
            this.basicKpiSnapshots = snapshots
            this.loadingBasicKpi = false;
          }, error: error => {
            console.log("Error getAllBasicKpiSnapshots");
            console.error(error);
            this.alertService.error("Basic KPI Snapshot retrieval failed");
          }
        })
        // this.loadingBasicKpi = false;
      }, error: error => {
        this.loadingBasicKpi = false;
        console.log("Error getAllBasicKpi");
        console.error(error);
        this.alertService.error("Basic KPI retrieval failed");
      }
    });
  }

  getLteTddBasicKpi() {
    console.log('Get LteTddBasicKpi');
    this.basicKpiDtos = [];
    this.basicKpiSnapshots = [];
    this.getLteTddBasicKpiSnapshot(this.basicKpiDtos);
  }

  private getNrBasicKpi() {
    console.log('Get NrBasicKpi');
    this.basicKpiDtos = [];
    this.basicKpiSnapshots = [];
    this.getNrBasicKpiSnapshot(this.basicKpiDtos);
  }

  getUmtsBasicKpi() {
    console.log('Get UMTSBasicKpi');
    this.basicKpiDtos = [];
    this.basicKpiSnapshots = [];
    this.getUmtsBasicKpiSnapshot(this.basicKpiDtos);
  }

  getGsmBasicKpi() {
    console.log('Get GSM Basic KPI');
    this.basicKpiDtos = [];
    this.basicKpiSnapshots = [];
    this.getGsmBasicKpiSnapshot(this.basicKpiDtos);
  }

  //------- BASIC KPI SNAPSHOTS ---------

  private getLteFddBasicKpiSnapshot(basicKpiDto: BasicKpiDto[]) {
    const requests = basicKpiDto.map(kpi =>
      this.ltefdddayservice.getBasicKpiSnapshot(kpi.kpiName!, this.granularity, this.district)
    );
    return forkJoin([...requests]);
  }

  private getLteTddBasicKpiSnapshot(basicKpiDto: BasicKpiDto[]) {
    console.log('Get LteTddBasicKpiSnapshot for KPI');
  }

  private getNrBasicKpiSnapshot(basicKpiDtos: BasicKpiDto[]) {
    console.log('Get NrBasicKpiSnapshot for KPI');
  }

  private getUmtsBasicKpiSnapshot(basicKpiDto: BasicKpiDto[]) {
    console.log('Get UmtsBasicKpiSnapshot for KPI');
  }

  private getGsmBasicKpiSnapshot(basicKpiDto: BasicKpiDto[]) {
    console.log('Get GsmBasicKpiSnapshot for KPI');
  }


  get sortedBasicKpiSnapshots(): BasicKpiSnapshot[] {
    return this.basicKpiSnapshots.sort((a, b) => a.kpiLabel!.localeCompare(b.kpiLabel!))
  }

  //----------- WORST CELLS ----------------

  onExcludeZeroesChange(event: Event) {
    this.getLteFddWorstCellsByKpi(this.selectedStandardKpi, this.granularity);
  }

  getLteFddWorstCellsByKpi(kpiName: string, granularity: string) {
    this.loadingWorstCells = true;
    this.ltefdddayservice.getWorstCellsByKpi(kpiName, granularity, this.district, this.excludeZeroes).subscribe({
      next: data => {
        console.log("all-worstcell-data", data);
        this.allWorstCells = data;
        console.log("allWorstCells", this.allWorstCells);
        this.totalPages = Math.ceil(this.allWorstCells.length / this.pageSize);
        this.setPage(0);
        this.loadingWorstCells = false;
      },
      error: error => {
        console.error("Error getWorstCellsByKpi:", error);
        this.alertService.error("Worst cells retrieval failed");
        this.loadingWorstCells = false;
      }
    });

  }

  getLteTddWorstCellsByKpi(kpiName: string, granularity: string) {
    this.loadingWorstCells = true;
    console.log('Get LteTddWorstCellsByKpi');
    //TODO: Configure worst cell retrieval
  }

  getNrWorstCellsByKpi(kpiName: string, granularity: string) {
    this.loadingWorstCells = true;
    console.log('Get NrWorstCellsByKpi');
    //TODO: Configure worst cell retrieval
  }

  getUmtsWorstCellsByKpi(kpiName: string, granularity: string) {
    this.loadingWorstCells = true;
    console.log('Get UmtsWorstCellsByKpi');
    //TODO: Configure worst cell retrieval
  }

  getGsmWorstCellsByKpi(kpiName: string, granularity: string) {
    this.loadingWorstCells = true;
    console.log('Get GsmWorstCellsByKpi');
    //TODO: Configure worst cell retrieval
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

  selectKpi(kpi: string) {
    this.currentPage = 0;
    this.getLteFddTrendDataByKpi(kpi, this.selectedKpiTrendPeriod);
    this.getLteFddWorstCellsByKpi(kpi, this.granularity);
  }

  getAllStandardKpi() {
    this.standardKpis = [];
    this.ltefddstandardkpiservice.getAllStandardKpi().subscribe({
      next: data => {
        this.standardKpis = data;
        if (this.standardKpis.length > 0) {
          this.selectedStandardKpi = this.standardKpis[0].kpiName!;
          this.selectKpi(this.selectedStandardKpi);
        }
      }, error: error => {
        console.log("Error getAllStandardKpi:");
        console.error(error);
        this.alertService.error("Standard KPI retrieval failed");
      }
    })
  }

  //---------- OPEN ANALYSIS MODAL (DIALOG) ----------------

  openAnalysisModal(kpiLabel: string, cellName: string) {
    this.analysisModalCell = cellName;
    this.analysisModalKpiLabel = kpiLabel;
    this.getTrendDataByKpiLabelAndCell(kpiLabel, cellName, 'quarter')
      .subscribe({
        next: data => {
          console.log("data:", data);
          this.chartSeries = this.chartService.buildSeriesKpiDataDto(data);
          this.analysisModal.nativeElement.showModal();
        }, error: err => {
          console.log("Error getDataByKpiLabelAndCell:");
          console.error(err);
          this.alertService.error("KPI Data retrieval failed");
        }
      })
  }

  //------------- KPI TREND CHART ----------------

  onPeriodChange(event: Event) {
    this.getLteFddTrendDataByKpi(this.selectedStandardKpi, this.selectedKpiTrendPeriod);
    //TODO: configure for other RATs
  }

  getLteFddTrendDataByKpi(kpiName: string, period: string) {
    this.loadingKpiTrend = true;
    this.kpiTrendData = [];
    this.ltefdddayservice.getDataByKpi(kpiName, period, this.district).subscribe({
      next: data => {
        this.kpiTrendData = data;
        this.loadingKpiTrend = false;
      }, error: error => {
        console.log("Error getDataByKpi:");
        console.error(error);
        this.alertService.error("Trend data retrieval failed");
        this.loadingKpiTrend = false;
      }
    });
  }

  getLteTddTrendDataByKpi(kpiName: string, period: string) {
    this.loadingKpiTrend = true;
    this.kpiTrendData = [];
    //TODO: configure data retrieval
    console.log('Retrieving LteTddTrendDataByKpi');
  }

  getNrTrendDataByKpi(kpiName: string, period: string) {
    this.loadingKpiTrend = true;
    this.kpiTrendData = [];
    //TODO: configure data retrieval
    console.log('Retrieving NrTrendDataByKpi');
  }

  getUmtsTrendDataByKpi(kpiName: string, period: string) {
    this.loadingKpiTrend = true;
    this.kpiTrendData = [];
    //TODO: configure data retrieval
    console.log('Retrieving UmtsTrendDataByKpi');
  }

  getGsmTrendDataByKpi(kpiName: string, period: string) {
    this.loadingKpiTrend = true;
    this.kpiTrendData = [];
    //TODO: configure data retrieval
    console.log('Retrieving GsmTrendDataByKpi');
  }

  //============ MODAL KPI TREND CHART =================

  getTrendDataByKpiLabelAndCell(kpiLabel: string, cellName: string, period: string): Observable<KpiDataDto[]> {
    //TODO: configure for other RATs
    return this.ltefdddayservice.getDataByKpiLabelAndCell(kpiLabel, cellName, period);
  }

  //============ ROUTER-LINK ===========================

  getRouterLinkForCell(): string[] {
    switch (this.selectedRat()) {
      case "ltefdd":
        return ['/pulse/cell/ltefdd', this.analysisModalCell];
      case "ltetdd":
        return ['/pulse/cell/ltetdd', this.analysisModalCell];
      case "umts":
        return ['/pulse/cell/umts', this.analysisModalCell];
      case "gsm":
        return ['/pulse/cell/gsm', this.analysisModalCell];
      default:
        return ['/pulse/cell/ltefdd', this.analysisModalCell];
    }
  }
}
