import {Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewChild} from '@angular/core';
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
import {Subscription} from 'rxjs';

export type ChartOptions = {
  series: ApexAxisChartSeries | ApexNonAxisChartSeries;
  chart: ApexChart;
  xaxis: ApexXAxis;
  yaxis: ApexYAxis;
  dataLabels: ApexDataLabels;
  stroke: ApexStroke;
  legend: ApexLegend;
  theme: ApexTheme;
  grid: ApexGrid;
};

@Component({
  selector: 'app-linechart',
  imports: [CommonModule, ChartComponent],
  templateUrl: './linechart.html',
  styleUrl: './linechart.css'
})
export class Linechart implements OnInit, OnChanges, OnDestroy {

  @ViewChild("chart") chart!: ChartComponent;
  @Input() chartSeries: any;
  public chartOptions: Partial<ChartOptions> = {};

  public showChart:boolean = true;

  // currentTheme: string = '';
  // isDark: boolean = false;

  private themeSub!: Subscription;

  constructor(
    private themeService: DaisyUiThemeService
  ) {
  }

  ngOnInit(): void {
    // this.currentTheme = this.themeService.getCurrentTheme();
    // this.isDark = this.currentTheme === 'netrics_dark';
    this.initializeChart();

    // Subscribe to theme changes
    this.themeSub = this.themeService.theme$.subscribe(theme => {
      this.updateChartTheme(theme);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    const series = this.chartSeries;

    if (this.chart) {
      this.chart.updateSeries(series, true);
    } else {
      this.chartOptions = {
        ...this.chartOptions,
        series: series
      };

      // Force re-render when data changes
      this.showChart = false;
      setTimeout(() => {
        this.showChart = true;
      }, 0);
    }
  }


  ngOnDestroy(): void {
    if (this.themeSub) {
      this.themeSub.unsubscribe();
    }
  }


  private initializeChart(): void {
    const currentTheme = this.themeService.getCurrentTheme();
    const isDark = currentTheme === 'netrics_dark';

    // Hide chart temporarily to force re-render
    this.showChart = false;

    this.chartOptions = {
      series: [],
      chart: {
        fontFamily: 'Inter',
        type: 'line',
        height: 300,
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
        background: isDark ? 'oklch(27% 0.006 286.033)' : 'oklch(98% 0.003 247.858)'
      },
      xaxis: {
        type: 'datetime',
        labels: {
          datetimeUTC: false,
          style: {
            colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
          }
        }
      },
      yaxis: {
        title: {
          style: {
            color: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
          }
        },
        labels: {
          formatter: (val: number) => val.toFixed(2),
          style: {
            colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
          }
        }
      },
      legend: {
        show: true,
        showForSingleSeries: true,
        position: 'top',
        labels: {
          colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
        }
      },
      stroke: {
        curve: 'smooth',
        width: 2,
      },
      grid: {
        show: true,
        borderColor: isDark ? 'oklch(37% 0.013 285.805)' : 'oklch(92% 0.013 255.508)'
      },
      theme: {
        mode: isDark ? 'dark' : 'light',
        palette: 'palette1',
        monochrome: {
          enabled: false,
          color: '#255aee',
          shadeTo: isDark ? 'dark' : 'light',
          shadeIntensity: 0.65
        },
      }
    };

    // Show chart again in next tick to force re-render
    setTimeout(() => {
      this.showChart = true;
    }, 10);
  }


  private updateChartTheme(theme: string): void {
    const isDark = theme === 'netrics_dark';

    // Hide chart temporarily to force re-render
    this.showChart = false;

    // Update chart options with new theme
    this.chartOptions = {
      ...this.chartOptions,
      xaxis: {
        ...this.chartOptions.xaxis,
        labels: {
          ...this.chartOptions.xaxis?.labels,
          style: {
            colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
          }
        }
      },
      yaxis: {
        ...this.chartOptions.yaxis,
        title: {
          ...this.chartOptions.yaxis?.title,
          style: {
            color: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
          }
        },
        labels: {
          ...this.chartOptions.yaxis?.labels,
          style: {
            colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
          }
        }
      },
      legend: {
        ...this.chartOptions.legend,
        labels: {
          colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
        }
      },
      grid: {
        show: true,
        borderColor: isDark ? 'oklch(37% 0.013 285.805)' : 'oklch(92% 0.013 255.508)'
      },
      theme: {
        ...this.chartOptions.theme,
        mode: isDark ? 'dark' : 'light',
        monochrome: {
          ...this.chartOptions.theme?.monochrome,
          shadeTo: isDark ? 'dark' : 'light'
        }
      },
      chart: {
        ...this.chartOptions,
        background: isDark ? 'oklch(27% 0.006 286.033)' : 'oklch(98% 0.003 247.858)',
        type: 'line',
        height: 300,
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
      }
    };

    // Show chart again in next tick to force re-render
    setTimeout(() => {
      this.showChart = true;
    }, 10);
  }

}
