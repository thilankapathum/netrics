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
  granularity = signal<'day' | 'week' | 'month'>('day')
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

  districts: DistrictDto[] = [{name: 'All Districts', code: 'ALLDIST'}];
  district: string = 'All Districts';

  loadingBasicKpi: boolean = false;
  loadingWorstCells: boolean = false;
  loadingKpiTrend: boolean = false;
  loadingAnalysisModalChart:boolean = false;

  @ViewChild('analysisModal') analysisModal!: ElementRef<HTMLDialogElement>;

  constructor(private cdr: ChangeDetectorRef,
              private ltefddbasickpiservice: BasickpiService,
              private ltefdddayservice: KpidayService,
              private ltefddstandardkpiservice: StandardkpiService,
              private chartService: ChartService,
              private alertService: AlertService,
              private districtService: DistrictService,
              private router: Router) {
  }

  ngOnInit() {
    this.cdr.detectChanges(); // Force change detection
    this.getAllDistricts();
    this.getBasicKpi(this.selectedRat())
    this.getAllStandardKpi(this.selectedRat());
  }

  selectGranularity(granularity: 'day' | 'week' | 'month') {
    this.granularity.set(granularity);
    this.cdr.detectChanges(); // Force change detection
    this.getBasicKpi(this.selectedRat());
    this.selectKpi(this.selectedStandardKpi(), this.selectedRat());
  }

  selectDistrict(district: string) {
    this.district = district;
    this.cdr.detectChanges(); // Force change detection
    this.getBasicKpi(this.selectedRat());
    this.selectKpi(this.selectedStandardKpi(), this.selectedRat());
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
    this.ltefddbasickpiservice.getAllBasicKpi(ratName).subscribe({
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
      this.ltefdddayservice.getBasicKpiSnapshot(kpi.kpiName!, this.granularity(), this.district, this.selectedRat())
    );
    return forkJoin([...requests]);
  }

  get sortedBasicKpiSnapshots(): BasicKpiSnapshot[] {
    return this.basicKpiSnapshots.sort((a, b) => a.kpiLabel!.localeCompare(b.kpiLabel!))
  }

  //----------- WORST CELLS ----------------

  onExcludeZeroesChange(event: Event) {
    this.getWorstCellsByKpi(this.selectedStandardKpi(), this.granularity(), this.selectedRat());
  }

  getWorstCellsByKpi(kpiName: string, granularity: string, ratName: string) {
    this.loadingWorstCells = true;
    this.allWorstCells = [];
    this.ltefdddayservice.getWorstCellsByKpi(kpiName, granularity, this.district, this.excludeZeroes, ratName).subscribe({
      next: data => {
        this.allWorstCells = data;
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

  selectKpi(kpi: string, ratName: string) {
    this.currentPage = 0;
    this.getTrendDataByKpi(kpi, this.selectedKpiTrendPeriod(), ratName);
    this.getWorstCellsByKpi(kpi, this.granularity(), ratName);
  }

  getAllStandardKpi(ratName: string) {
    this.standardKpis = [];
    this.ltefddstandardkpiservice.getAllStandardKpi(ratName).subscribe({
      next: data => {
        this.standardKpis = data;
        if (this.standardKpis.length > 0) {
          this.selectedStandardKpi.set(this.standardKpis[0].kpiName!);
          this.selectKpi(this.selectedStandardKpi(), ratName);    // Getting Worst-cells and Trend-data
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

  openAnalysisModal(kpiLabel: string, cellName: string) {
    this.analysisModalCell = cellName;
    this.analysisModalKpiLabel = kpiLabel;
    this.loadingAnalysisModalChart = true;
    this.analysisModal.nativeElement.showModal();
    this.getTrendDataByKpiLabelAndCell(kpiLabel, cellName, 'quarter', this.selectedRat())
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
    this.getTrendDataByKpi(this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.selectedRat());
  }

  getTrendDataByKpi(kpiName: string, period: string, ratName: string) {
    this.loadingKpiTrend = true;
    this.kpiTrendData = [];
    this.ltefdddayservice.getDataByKpi(kpiName, period, this.district, ratName).subscribe({
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

  //============ MODAL KPI TREND CHART =================

  getTrendDataByKpiLabelAndCell(kpiLabel: string, cellName: string, period: string, ratName: string): Observable<KpiDataDto[]> {
    return this.ltefdddayservice.getDataByKpiLabelAndCell(kpiLabel, cellName, period, ratName);
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
