import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {WorstCell} from '../../../../models/pulse/WorstCells';
import {OnInit, ChangeDetectorRef} from '@angular/core';
import {LineChart} from '../../../../components/charts/linechart/line-chart/line-chart';
import {LtefddbasickpiService} from '../../../../service/pulse/ltefdd/ltefddbasickpi.service';
import {LtefdddayService} from '../../../../service/pulse/ltefdd/ltefddday.service';
import {BasicKpiDto} from '../../../../models/pulse/BasicKpiDto';
import {BasicKpiSnapshot} from '../../../../models/pulse/BasicKpiSnapshot';

@Component({
  selector: 'app-pulse',
  standalone: true,
  imports: [CommonModule, LineChart],
  templateUrl: './pulse.component.html',
  styleUrl: './pulse.component.css'
})
export class PulseComponent implements OnInit {
  basicKpiDtos: BasicKpiDto[] = [];
  basicKpiSnapshots: BasicKpiSnapshot[] = [];

  constructor(private cdr: ChangeDetectorRef,
              private ltefddbasickpiservice: LtefddbasickpiService,
              private ltefdddayservice: LtefdddayService) {
  }

  ngOnInit() {
    this.cdr.detectChanges(); // Force change detection
    this.getAllBasicKpi();
    this.getBasicKpiSnapshot(this.basicKpiDtos);
  }

  getAllBasicKpi() {
    this.ltefddbasickpiservice.getAllBasicKpi().subscribe({
      next: data => {
        this.basicKpiDtos = data;
        console.log("BasicKpiDtos:", this.basicKpiDtos);

        this.getBasicKpiSnapshot(this.basicKpiDtos);
        console.log("BasicKpiSnapshots:", this.basicKpiDtos);

      }, error: error => {
        console.log("Error getAllBasicKpi");
        console.error(error);
        alert("Error getAllBasicKpi");
      }
    })
  }

  getBasicKpiSnapshot(basicKpiDto: BasicKpiDto[]) {
    for (const kpi of basicKpiDto) {
      this.ltefdddayservice.getBasicKpiSnapshot(kpi.kpiName!, 'day')
        .subscribe({
          next: data => {
            this.basicKpiSnapshots.push(data);
            // console.log("BasicKpiSnapshots:", this.basicKpiSnapshots);
          }, error: error => {
            console.log("Error getBasicKpiSnapshots:");
            console.error(error);
            alert("Error getBasicKpiSnapshots:");
          }
        })
    }
  }


  basicKpi: string[] = ['Accessibility', 'Retainability', 'Mobility', 'Availability', 'Utilization', 'Quality'];

  worstCells: WorstCell[] = [
    {
      cellName: "KYUDW1-M-L85-B1",
      kpiLabel: "E-RAB Setup Success Rate",
      value: 99.92999999999999,
      previousValue: 100,
      difference: -0.07000000000000739,
      improved: false
    },
    {
      cellName: "KYUDW1-M-L85-A1",
      kpiLabel: "E-RAB Setup Success Rate",
      value: 99.94,
      previousValue: 99.86999999999999,
      difference: 0.07000000000000739,
      improved: true
    },
    {
      cellName: "KYUDW1-M-L18-A1",
      kpiLabel: "E-RAB Setup Success Rate",
      value: 99.97999999999999,
      previousValue: 99.95,
      difference: 0.029999999999986926,
      improved: true
    },
    {
      cellName: "KYUDW1-M-L85-C1",
      kpiLabel: "E-RAB Setup Success Rate",
      value: 99.99,
      previousValue: 99.96000000000001,
      difference: 0.029999999999986926,
      improved: true
    },
    {
      cellName: "KYUDW1-M-L18-B1",
      kpiLabel: "E-RAB Setup Success Rate",
      value: 99.99,
      previousValue: 99.98,
      difference: 0.009999999999990905,
      improved: true
    }
  ]

}
