import {Component, ElementRef, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {WorstCell} from '../../../../models/pulse/WorstCell';
import {OnInit, ChangeDetectorRef} from '@angular/core';
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
import {Observable} from 'rxjs';
import {ChartService} from '../../../../service/components/chart/chart.service';
import {WorstCells} from '../../../../models/pulse/WorstCells';

@Component({
  selector: 'app-pulse',
  standalone: true,
  imports: [CommonModule, LineChart, FormsModule, Linechart],
  templateUrl: './pulse.component.html',
  styleUrl: './pulse.component.css'
})
export class PulseComponent implements OnInit {

  basicKpiDtos: BasicKpiDto[] = [];
  basicKpiSnapshots: BasicKpiSnapshot[] = [];
  // worstCellsX: WorstCell[] = [];
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
  worstCells: WorstCells[] = [];

  @ViewChild('analysisModal') analysisModal!: ElementRef<HTMLDialogElement>;

  constructor(private cdr: ChangeDetectorRef,
              private ltefddbasickpiservice: LtefddbasickpiService,
              private ltefdddayservice: LtefdddayService,
              private ltefddstandardkpiservice: LtefddstandardkpiService,
              private chartService: ChartService) {
  }

  ngOnInit() {
    this.cdr.detectChanges(); // Force change detection
    this.getAllBasicKpi();
    this.getAllStandardKpi();
  }

  getAllBasicKpi() {
    this.ltefddbasickpiservice.getAllBasicKpi().subscribe({
      next: data => {
        this.basicKpiDtos = data;
        this.getBasicKpiSnapshot(this.basicKpiDtos);

      }, error: error => {
        console.log("Error getAllBasicKpi");
        console.error(error);
        alert("Error getAllBasicKpi");
      }
    })
  }

  getBasicKpiSnapshot(basicKpiDto: BasicKpiDto[]) {
    this.basicKpiSnapshots = [];
    for (const kpi of basicKpiDto) {
      this.ltefdddayservice
        .getBasicKpiSnapshot(kpi.kpiName!, this.granularity)
        .subscribe({
            next: data => {
              this.basicKpiSnapshots.push(data);
            }, error: error => {
              console.log("Error getBasicKpiSnapshots:");
              console.error(error);
              alert("Error getBasicKpiSnapshots:");
            }
          }
        )
    }
  }

  getWorstCellsByKpi(kpiName: string, granularity: string, page: number, size: number) {
    this.ltefdddayservice.getWorstCellsByKpi(kpiName, granularity, page, size).subscribe({
      next: data => {
        console.log("data", data);
        this.worstCells = data.content;
        console.log("WorstCells2:", this.worstCells);
        this.totalPages = data.totalPages;
        this.currentPage = data.number;
      },
      error: error => {
        console.error("Error getWorstCellsByKpi:", error);
        alert("Error getWorstCellsByKpi");
      }
    });
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.getWorstCellsByKpi(this.selectedStandardKpi, "day", this.currentPage + 1, this.pageSize);
    }
  }

  prevPage() {
    if (this.currentPage > 0) {
      this.getWorstCellsByKpi(this.selectedStandardKpi, "day", this.currentPage - 1, this.pageSize);
    }
  }


  selectGranularity(granularity: string) {
    this.granularity = granularity;
    this.ngOnInit();
  }

  selectKpi(kpi: string) {
    // this.getWorstCellsByKpiX(kpi, this.granularity, 8);
    this.currentPage = 0;
    this.getTrendDataByKpi(kpi, this.selectedKpiTrendPeriod);
    this.getWorstCellsByKpi(kpi, this.granularity, this.currentPage, this.pageSize);
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
        alert("Error getAllStandardKpi:");
      }
    })
  }

  onPeriodChange(event: Event) {
    this.getTrendDataByKpi(this.selectedStandardKpi, this.selectedKpiTrendPeriod);
  }

  getTrendDataByKpi(kpiName: string, period: string) {
    this.kpiTrendData = [];
    this.ltefdddayservice.getDataByKpi(kpiName, period).subscribe({
      next: data => {
        this.kpiTrendData = data;
        // console.log("DataByKpi:", this.kpiTrendData);
      }, error: error => {
        console.log("Error getDataByKpi:");
        console.error(error);
        alert("Error getDataByKpi:");
      }
    })
  }

  getTrendDataByKpiLabelAndCell(kpiLabel: string, cellName: string, period: string): Observable<KpiDataDto[]> {
    return this.ltefdddayservice.getDataByKpiLabelAndCell(kpiLabel, cellName, period);
  }

  openAnalysisModal(kpiLabel: string, cellName: string) {
    this.analysisModalCell = cellName;
    this.analysisModalKpiLabel = kpiLabel;
    this.getTrendDataByKpiLabelAndCell(kpiLabel, cellName, 'quarter')
      .subscribe({
        next: data => {
          // console.log(kpiLabel, cellName);
          console.log("data:", data);
          this.chartSeries = this.chartService.buildSeriesKpiDataDto(data);
          // console.log("chartSeries:", this.chartSeries);
          this.analysisModal.nativeElement.showModal();
        }, error: err => {
          console.log("Error getDataByKpiLabelAndCell:");
          console.error(err);
        }
      })
  }

}
