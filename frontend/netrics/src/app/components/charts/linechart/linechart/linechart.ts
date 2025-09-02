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
  theme: ApexTheme;
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

  currentTheme: string = '';
  isDark: boolean = false;

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
        background: this.isDark ? 'oklch(27% 0.006 286.033)' : 'oklch(98% 0.003 247.858)'
        // background: 'oklch(0.899 0.159 184.319)'
      },
      xaxis: {
        type: 'datetime',
        labels: {
          style: {
            colors: this.isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
          }
        }
      },
      yaxis: {
        title: {
          // text: 'KPI Value',
          style: {
            color: this.isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
          }
        },
        labels: {
          formatter: (val: number) => val.toFixed(2),
          style: {
            colors: this.isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
          }
        }
      },
      legend: {
        show: true,
        showForSingleSeries: true,
        position: 'top',
        labels: {
          colors: this.isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
        }
      },
      stroke: {
        curve: 'smooth',
        width: 2,
      },
      theme: {
        mode: this.isDark ? 'dark' : 'light',
        palette: 'palette1',
        monochrome: {
          enabled: false,
          color: '#255aee',
          shadeTo: this.isDark ? 'dark' : 'light',
          shadeIntensity: 0.65
        },
      }
    };
  }

}
