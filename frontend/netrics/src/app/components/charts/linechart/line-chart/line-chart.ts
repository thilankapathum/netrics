import {Component, input, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  ApexAxisChartSeries,
  ApexNonAxisChartSeries,
  ApexChart,
  ApexXAxis,
  ApexDataLabels,
  ApexStroke,
  ChartComponent
} from 'ng-apexcharts';
import {KpiDataDto} from '../../../../models/pulse/KpiDataDto';
import {LtefdddayService} from '../../../../service/pulse/ltefdd/ltefddday.service';

export type ChartOptions = {
  series: ApexAxisChartSeries | ApexNonAxisChartSeries;
  chart: ApexChart;
  xaxis: ApexXAxis;
  yaxis: ApexYAxis;
  dataLabels: ApexDataLabels;
  stroke: ApexStroke;
};

@Component({
  selector: 'app-line-chart',
  standalone: true,
  imports: [CommonModule, ChartComponent],
  templateUrl: './line-chart.html',
  styleUrls: ['./line-chart.css']
})
export class LineChart implements OnInit, OnChanges {

  @ViewChild("chart") chart!: ChartComponent;
  @Input() kpiData: KpiDataDto[] = [];

  public chartOptions: Partial<ChartOptions> = {
    series: [],
    chart: {
      fontFamily: 'Inter',
      type: 'line',
      height: 200,
      width: '100%',
      animations: {
        enabled: true,
        speed: 300,
        animateGradually: {
          enabled: true,
          delay: 150
        }
      }
    },
    xaxis: {type: 'datetime'},
    yaxis: {
      title: {text: 'KPI Value'},
      labels: {
        formatter: (val: number) => val.toFixed(2)
      }
    }
  };

  constructor(private ltefdddayservice: LtefdddayService) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['kpiData'] && this.kpiData) {
      const series = this.buildSeries(this.kpiData);

      if (this.chart) {
        this.chart.updateSeries(series, true);
      } else {
        this.chartOptions = {
          ...this.chartOptions,
          series: series
        };
      }
    }
  }

  private buildSeries(kpiDataDto: KpiDataDto[]): ApexAxisChartSeries {
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
