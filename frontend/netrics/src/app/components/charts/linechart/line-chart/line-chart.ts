import {Component, Input, OnChanges, OnInit, OnDestroy, SimpleChanges, ViewChild} from '@angular/core';
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
import {LtefdddayService} from '../../../../service/pulse/ltefdd/ltefddday.service';
import {KpiTrendDto} from '../../../../models/pulse/KpiTrendDto';
import {KpiDataDto} from '../../../../models/pulse/KpiDataDto';
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
};

@Component({
  selector: 'app-line-chart',
  standalone: true,
  imports: [CommonModule, ChartComponent],
  templateUrl: './line-chart.html',
  styleUrls: ['./line-chart.css']
})
export class LineChart implements OnInit, OnChanges, OnDestroy {

  @ViewChild("chart") chart!: ChartComponent;
  @Input() kpiTrendData: Array<KpiTrendDto | KpiDataDto> = [];
  private themeSub!: Subscription;

  // Add a flag to force chart re-render
  public showChart = true;
  public chartOptions: Partial<ChartOptions> = {};

  constructor(private ltefdddayservice: LtefdddayService,
              private themeService: DaisyUiThemeService) {
  }

  ngOnInit(): void {
    // Initialize chart with current theme
    this.initializeChart();

    // Subscribe to theme changes
    this.themeSub = this.themeService.theme$.subscribe(theme => {
      this.updateChartTheme(theme);
    });
  }

  ngOnDestroy(): void {
    if(this.themeSub){
      this.themeSub.unsubscribe();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['kpiTrendData'] && this.kpiTrendData) {
      const series = this.buildSeries(this.kpiTrendData);

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

  private initializeChart(): void {
    const currentTheme = this.themeService.getCurrentTheme();
    const isDark = currentTheme === 'netrics_dark';

    this.chartOptions = {
      series: this.kpiTrendData ? this.buildSeries(this.kpiTrendData) : [],
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
        background: isDark ? 'oklch(27% 0.006 286.033)' : 'oklch(98% 0.003 247.858)'
      },
      xaxis: {
        type: 'datetime',
        labels: {
          style: {
            colors: isDark ? '#a6adba' : '#374151'
          }
        }
      },
      yaxis: {
        title: {
          // text: 'KPI Value',
          style: {
            color: isDark ? '#a6adba' : '#374151'
          }
        },
        labels: {
          formatter: (val: number) => val.toFixed(2),
          style: {
            colors: isDark ? '#a6adba' : '#374151'
          }
        }
      },
      legend: {
        show: true,
        showForSingleSeries: true,
        position: 'top',
        labels: {
          colors: isDark ? '#a6adba' : '#374151'
        }
      },
      stroke: {
        curve: 'smooth',
        width: 2,
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
            colors: isDark ? '#a6adba' : '#374151'
          }
        }
      },
      yaxis: {
        ...this.chartOptions.yaxis,
        title: {
          ...this.chartOptions.yaxis?.title,
          style: {
            color: isDark ? '#a6adba' : '#374151'
          }
        },
        labels: {
          ...this.chartOptions.yaxis?.labels,
          style: {
            colors: isDark ? '#a6adba' : '#374151'
          }
        }
      },
      legend: {
        ...this.chartOptions.legend,
        labels: {
          colors: isDark ? '#a6adba' : '#374151'
        }
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
        // type: this.chartOptions.chart?.type
      }
    };

    // Show chart again in next tick to force re-render
    setTimeout(() => {
      this.showChart = true;
    }, 10);
  }

  private buildSeries(kpiTrendDataDto: KpiTrendDto[]): ApexAxisChartSeries {
    const grouped = kpiTrendDataDto.reduce((acc, curr) => {
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
