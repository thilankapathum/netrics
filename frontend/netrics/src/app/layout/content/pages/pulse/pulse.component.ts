import {Component} from '@angular/core';
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
import {KpiDataDto} from '../../../../models/pulse/KpiDataDto';

@Component({
  selector: 'app-pulse',
  standalone: true,
  imports: [CommonModule, LineChart, FormsModule],
  templateUrl: './pulse.component.html',
  styleUrl: './pulse.component.css'
})
export class PulseComponent implements OnInit {

  basicKpiDtos: BasicKpiDto[] = [];
  basicKpiSnapshots: BasicKpiSnapshot[] = [];
  worstCells: WorstCell[] = [];
  period: string = 'day';
  standardKpis: StandardKpiDto[] = [];
  selectedStandardKpi: string = '';
  kpiData: KpiDataDto[] = [];

  constructor(private cdr: ChangeDetectorRef,
              private ltefddbasickpiservice: LtefddbasickpiService,
              private ltefdddayservice: LtefdddayService,
              private ltefddstandardkpiservice:LtefddstandardkpiService) {
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
      this.ltefdddayservice.getBasicKpiSnapshot(kpi.kpiName!, this.period)
        .subscribe({
          next: data => {
            this.basicKpiSnapshots.push(data);
          }, error: error => {
            console.log("Error getBasicKpiSnapshots:");
            console.error(error);
            alert("Error getBasicKpiSnapshots:");
          }
        })
    }
  }

  getWorstCellsByKpi(kpiName: string, period: string, count: number) {
    this.worstCells = [];
    this.ltefdddayservice.getWorstCellsByKpi(kpiName, period, count).subscribe({
      next: data => {
        this.worstCells = data;
        // console.log("WorstCellsByKpi:", this.worstCells);
      }, error: error => {
        console.log("Error getWorstCellsByKpi:");
        console.error(error);
        alert("Error getWorstCellsByKpi:");
      }
    })
  }

  selectPeriod(period: string) {
    this.period = period;
    this.ngOnInit();
  }

  selectKpi(kpi: string) {
    this.getWorstCellsByKpi(kpi,this.period,10);
    this.getDataByKpiAndCell(kpi,'KYUDW1-M-L85-B1','month')
    // this.ngOnInit();
  }

  getAllStandardKpi() {
    this.standardKpis = [];
    this.ltefddstandardkpiservice.getAllStandardKpi().subscribe({
      next: data => {
        this.standardKpis = data;
        // console.log("StandardKpis:", this.standardKpis);
      }, error: error => {
        console.log("Error getAllStandardKpi:");
        console.error(error);
        alert("Error getAllStandardKpi:");
      }
    })
  }

  getDataByKpiAndCell(kpiName: string, cellName:string, period: string) {
    this.kpiData = [];
    this.ltefdddayservice.getDataByKpiAndCell(kpiName, cellName, period).subscribe({
      next: data => {
        this.kpiData = data;
        console.log("DataByKpiAndCell:", this.kpiData);
      },error: error => {
        console.log("Error getDataByKpiAndCell:");
        console.error(error);
        alert("Error getDataByKpiAndCell:");
      }
    })
  }

}
