import {Component, Input, ViewChild} from '@angular/core';
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
export class LineChart {
  @ViewChild("chart") chart!: ChartComponent;
  public chartOptions: Partial<ChartOptions>;

  @Input() width: number = 250;

  dataSales: number[] = [10, 41, 35, 51, 49, 62, 69, 91, 148]
  dataProfits: number[] = [ 49, 62, 69, 91, 148, 10,41, 35, 51]
  timeline: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9];

  constructor() {
    this.chartOptions = {
      series: [
        {
          name: "Sales",
          data: this.dataSales
        },
        {
          name: "Profits",
          data: this.dataProfits
        }
      ],
      chart: {
        fontFamily: 'Inter',
        type: "line",
        height: this.width,
        width: '100%',
        animations: {
          enabled : true,
          speed : 300,
          animateGradually :{
            enabled : true,
            delay : 150
          }
        },
      },
      dataLabels: {
        enabled: false
      },
      stroke: {
        curve: "smooth"
      },
      xaxis: {
        categories: this.timeline
      },
    };
  }
}
