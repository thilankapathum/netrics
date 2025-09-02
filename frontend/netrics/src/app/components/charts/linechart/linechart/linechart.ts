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
import {DaisyUiThemeService} from '../../../../service/components/theme/daisy-ui-theme.service';

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
  public chartOptions: Partial<ChartOptions> = {};

  currentTheme:string ='';
  isDark:boolean = false;

  constructor(
    private themeService: DaisyUiThemeService
  ) {
  }

  ngOnInit(): void {
     this.currentTheme = this.themeService.getCurrentTheme();
    console.log("currentTheme", this.currentTheme);
    this.isDark = this.currentTheme === 'netrics_dark';
    console.log("isDark", this.isDark);
    this.initializeChart();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const series = this.chartSeries;

    console.log("currentTheme-changes", this.currentTheme);
    console.log("isDark-changes", this.isDark);
    console.log("series-InChart", series);

    // this.initializeChart();


    if (this.chart) {
      this.chart.updateSeries(series, true);
    } else {
      this.chartOptions = {
        ...this.chartOptions,
        series: series
      };
    }
  }


  private initializeChart(): void {


    this.chartOptions = {
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
        },
        background: this.isDark ? 'oklch(0.647 0.311 319.903)' : 'oklch(0.899 0.159 184.319)'
        // background: 'oklch(0.899 0.159 184.319)'
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
  }

}
