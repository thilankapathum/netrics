import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
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

export type ChartOptions = {
  series: ApexAxisChartSeries | ApexNonAxisChartSeries;
  chart: ApexChart;
  xaxis: ApexXAxis;
  yaxis: ApexYAxis;
  dataLabels: ApexDataLabels;
  stroke: ApexStroke;
  legend: ApexLegend;
};

@Component({
  selector: 'app-linechart',
  imports: [CommonModule, ChartComponent],
  templateUrl: './linechart.html',
  styleUrl: './linechart.css'
})
export class Linechart implements OnInit, OnChanges {
  @ViewChild("chart") chart!: ChartComponent;
  @Input() chartSeries: any;

  public chartOptions: Partial<ChartOptions> = {
    series: [],
    chart: {
      fontFamily: 'Inter',
      type: 'line',
      height: 215,
      width: '100%',
      animations: {
        enabled: true,
        speed: 300,
        animateGradually: {
          enabled: true,
          delay: 150
        }
      },
      toolbar: {
        show: false
      }
    },
    xaxis: {type: 'datetime'},
    yaxis: {
      title: {text: 'KPI Value'},
      labels: {
        formatter: (val: number) => val.toFixed(2)
      }
    },
    legend: {
      show: true,
      showForSingleSeries: true,
      position: 'top',
    },
    stroke: {
      curve: 'smooth',
      width: 2,
    }
  };

  constructor() {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const series = this.chartSeries;
    console.log("series-InChart", series);
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
