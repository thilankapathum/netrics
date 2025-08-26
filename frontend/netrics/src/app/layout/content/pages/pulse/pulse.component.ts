import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {WorstCell} from '../../../../models/WorstCell';
import {OnInit, ChangeDetectorRef} from '@angular/core';
import {LineChart} from '../../../../components/charts/linechart/line-chart/line-chart';

@Component({
  selector: 'app-pulse',
  standalone: true,
  imports: [CommonModule, LineChart],
  templateUrl: './pulse.component.html',
  styleUrl: './pulse.component.css'
})
export class PulseComponent implements OnInit {

  constructor(private cdr: ChangeDetectorRef) {
  }

  ngOnInit() {
    this.cdr.detectChanges(); // Force change detection
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
