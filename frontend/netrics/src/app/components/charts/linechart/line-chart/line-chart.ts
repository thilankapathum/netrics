import {Component, Inject, input, Input, OnChanges, OnInit, OnDestroy, Renderer2, SimpleChanges, ViewChild} from '@angular/core';
import {CommonModule, DOCUMENT} from '@angular/common';
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
    xaxis: {
      type: 'datetime',
      labels: {
        // style: {colors: '#fff'}
      }
    },
    yaxis: {
      title: {text: 'KPI Value'},
      labels: {
        formatter: (val: number) => val.toFixed(2),
        // style: {colors: '#fff'}
      }
    },
    legend: {
      show: true,
      showForSingleSeries: true,
      position: 'top',
      labels:{

      }
    },
    stroke: {
      curve: 'smooth',
      width: 2,
    },
    theme: {
      mode: 'light', // Initialize with a default mode
      palette: 'palette1',
      monochrome: {
        enabled: false,
        color: '#255aee',
        shadeTo: 'dark',
        shadeIntensity: 0.65
      },
    }
  };

  constructor(private ltefdddayservice: LtefdddayService,
              private themeService: DaisyUiThemeService) {
  }

  ngOnInit(): void {
    // Set initial theme
    this.setTheme(this.themeService.getCurrentTheme());

    // Subscribe to theme changes
    this.themeSub = this.themeService.theme$.subscribe(theme => {
      this.setTheme(theme);

      // Update the chart with new theme
      if (this.chart) {
        this.chart.updateOptions(this.chartOptions, true, true);
      }
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

  private setTheme(theme: string): void {
    if (!this.chartOptions.theme) {
      this.chartOptions.theme = {};
    }

    if (theme === 'netrics_dark') {
      this.chartOptions.theme.mode = 'dark';
    } else if (theme === 'netrics_light') {
      this.chartOptions.theme.mode = 'light';
    }
  }
}
