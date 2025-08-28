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
import {ApexAxisChartSeries} from 'ng-apexcharts';
import {Observable} from 'rxjs';

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
  worstCells: WorstCell[] = [];
  granularity: string = 'day';
  standardKpis: StandardKpiDto[] = [];
  selectedStandardKpi: string = '';
  selectedKpiTrendPeriod: string = 'month';
  kpiTrendData: KpiTrendDto[] = [];
  kpiData: KpiDataDto[] = [];

  chartSeries:any = null;

  @ViewChild('analysisModal') analysisModal!: ElementRef<HTMLDialogElement>;

  constructor(private cdr: ChangeDetectorRef,
              private ltefddbasickpiservice: LtefddbasickpiService,
              private ltefdddayservice: LtefdddayService,
              private ltefddstandardkpiservice: LtefddstandardkpiService) {
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

  getWorstCellsByKpi(kpiName: string, granularity: string, count: number) {
    this.worstCells = [];
    this.ltefdddayservice.getWorstCellsByKpi(kpiName, granularity, count).subscribe({
      next: data => {
        this.worstCells = data;
      }, error: error => {
        console.log("Error getWorstCellsByKpi:");
        console.error(error);
        alert("Error getWorstCellsByKpi:");
      }
    })
  }

  selectGranularity(granularity: string) {
    this.granularity = granularity;
    this.ngOnInit();
  }

  selectKpi(kpi: string) {
    this.getWorstCellsByKpi(kpi, this.granularity, 100);
    this.getTrendDataByKpi(kpi, this.selectedKpiTrendPeriod)
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
        console.log("DataByKpi:", this.kpiTrendData);
      }, error: error => {
        console.log("Error getDataByKpi:");
        console.error(error);
        alert("Error getDataByKpi:");
      }
    })
  }

  getTrendDataByKpiLabelAndCell(kpiLabel: string, cellName: string, period: string): Observable<KpiDataDto[]> {
    // let kpiDataDtos: KpiDataDto[] = [];
    // this.ltefdddayservice.getDataByKpiLabelAndCell(kpiLabel, cellName, period).subscribe({
    //   next: data => {
    //     kpiDataDtos = data;
    //     console.log("getTrendDataByKpiLabelAndCell-kpiDataDtos:", kpiDataDtos);
    //     console.log("getTrendDataByKpiLabelAndCell-data",data);
    //   }, error: error => {
    //     console.log("Error getDataByKpiLabel:");
    //     console.error(error);
    //     alert("Error getDataByKpiLabel:");
    //   }
    // })
    // return kpiDataDtos;

    return this.ltefdddayservice.getDataByKpiLabelAndCell(kpiLabel,cellName,period);
  }

  openAnalysisModal(kpiLabel: string, cellName: string) {
    console.log(kpiLabel, cellName);
    this.getTrendDataByKpiLabelAndCell(kpiLabel,cellName,'quarter')
      .subscribe({
        next: data => {
          console.log(kpiLabel, cellName);
          console.log("data:", data);
          this.chartSeries = this.buildSeriesWithCell(data);
          console.log("chartSeries:", this.chartSeries);
          this.analysisModal.nativeElement.showModal();
        }, error: err => {
          console.log("Error getDataByKpiLabelAndCell:");
          console.error(err);
        }
      })
    // let kpiDataDtos: KpiDataDto[] = this.getTrendDataByKpiLabelAndCell(kpiLabel,cellName,'quarter');
    // // this.getTrendDataByKpiLabelAndCell(kpiLabel, cellName, 'quarter');
    // console.log("openAnalysisModal-KpiDataDtos:" , kpiDataDtos)
    // this.chartSeries = this.buildSeriesWithCell(kpiDataDtos);
    // this.analysisModal.nativeElement.showModal();
  }


  private buildSeriesWithCell(kpiDataDto:KpiDataDto[]): ApexAxisChartSeries {
    const grouped = kpiDataDto.reduce((acc, curr) => {
      const key = curr.kpiLabel ?? 'Unknown KPI';
      if (!acc[key]) {
        acc[key] = [];
      }
      acc[key].push({
        x: new Date(curr.timestamp!),
        y: this.round2(curr.kpiValue ?? 0)
      });
      return acc;
    }, {} as Record<string, { x: Date; y: number }[]>);

    return Object.entries(grouped).map(([name, data]) => ({
      name,
      data
    }));
  }

  private round2(n: number): number {
    return Math.round((n + Number.EPSILON) * 100) / 100;
  }

}
