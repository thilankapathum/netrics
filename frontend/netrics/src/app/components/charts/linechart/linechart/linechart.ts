import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild,
  ChangeDetectorRef,
  ElementRef, signal, Injector, effect
} from '@angular/core';
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
  yaxis: ApexYAxis | ApexYAxis[];
  dataLabels: ApexDataLabels;
  stroke: ApexStroke;
  legend: ApexLegend;
  theme: ApexTheme;
  grid: ApexGrid;
  tooltip: ApexTooltip;
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
  @Input() chartHeight: number = 300;
  @Input() showLegend: boolean = true;
  @Input() annotationDate: string = '';
  @Input() yaxisOverride?: ApexYAxis[];
  public chartOptions: Partial<ChartOptions> = {};

  public showChart:boolean = true;

  private themeSub!: Subscription;
  private resizeObserver!: ResizeObserver;
  private lastWidth: number = 0;
  private annotationDateSignal = signal<string>('');

  constructor(
    private themeService: DaisyUiThemeService,
    private el: ElementRef,
    private injector: Injector
  ) {
  }

  ngOnInit(): void {
    this.initializeChart();

    this.themeSub = this.themeService.theme$.subscribe(theme => {
      this.updateChartTheme(theme);
    });

    this.resizeObserver = new ResizeObserver(entries => {
      const newWidth = entries[0]?.contentRect.width ?? 0;
      if (newWidth > 0 && Math.abs(newWidth - this.lastWidth) > 1) {
        this.lastWidth = newWidth;
        this.reflow();
      }
    });
    this.resizeObserver.observe(this.el.nativeElement);

    // ── Reactively apply annotation whenever the date signal changes ──
    effect(() => {
      const date = this.annotationDateSignal();
      // Run outside the reactive context so ApexCharts DOM ops don't
      // get tracked by Angular's signal graph
      setTimeout(() => this.applyAnnotation(date), 50);
    }, { injector: this.injector });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['chartSeries']) {
      if (this.chart) {
        this.chart.updateSeries(this.chartSeries ?? [], true);
      }
    }
    if (changes['yaxisOverride'] && this.chart) {
      this.chart.updateOptions({ yaxis: this.buildYAxis() }, false, true);
    }
    if (changes['annotationDate']) {
      this.annotationDateSignal.set(this.annotationDate);
    }
  }

  ngOnDestroy(): void {
    if (this.themeSub) this.themeSub.unsubscribe();
    if (this.resizeObserver) this.resizeObserver.disconnect();  // ← ADD
  }

  public reflow(): void {
    if (this.chart) {
      this.chart.updateOptions({ chart: { width: '100%' } }, false, false);
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
        height: this.chartHeight,
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
          datetimeUTC: true,
          style: {
            colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
          }
        }
      },
      yaxis: this.buildYAxis(),
      tooltip: {
        x: {
          format: 'yyyy-MM-dd HH:mm'
        }, fixed: {
          enabled: false
        }
      },
      // yaxis: {
      //   title: {
      //     style: {
      //       color: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
      //     }
      //   },
      //   labels: {
      //     formatter: (val: number) => val.toFixed(2),
      //     style: {
      //       colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
      //     }
      //   }
      // },
      legend: {
        show: this.showLegend,
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
    // setTimeout(() => {
    //   this.showChart = true;
    // }, 10);

    setTimeout(() => {
      this.showChart = true;
      setTimeout(() => this.applyAnnotation(this.annotationDate), 50);
    }, 10);
  }


  // private updateChartTheme(theme: string): void {
  //   const isDark = theme === 'netrics_dark';
  //
  //   // Hide chart temporarily to force re-render
  //   this.showChart = false;
  //
  //   // Update chart options with new theme
  //   this.chartOptions = {
  //     ...this.chartOptions,
  //     xaxis: {
  //       ...this.chartOptions.xaxis,
  //       labels: {
  //         ...this.chartOptions.xaxis?.labels,
  //         style: {
  //           colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
  //         }
  //       }
  //     },
  //     yaxis: {
  //       ...this.chartOptions.yaxis,
  //       title: {
  //         ...this.chartOptions.yaxis?!.title,
  //         style: {
  //           color: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
  //         }
  //       },
  //       labels: {
  //         ...this.chartOptions.yaxis?.labels,
  //         style: {
  //           colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
  //         }
  //       }
  //     },
  //     legend: {
  //       ...this.chartOptions.legend,
  //       show: this.showLegend,
  //       labels: {
  //         colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)'
  //       }
  //     },
  //     grid: {
  //       show: true,
  //       borderColor: isDark ? 'oklch(37% 0.013 285.805)' : 'oklch(92% 0.013 255.508)'
  //     },
  //     theme: {
  //       ...this.chartOptions.theme,
  //       mode: isDark ? 'dark' : 'light',
  //       monochrome: {
  //         ...this.chartOptions.theme?.monochrome,
  //         shadeTo: isDark ? 'dark' : 'light'
  //       }
  //     },
  //     chart: {
  //       ...this.chartOptions,
  //       background: isDark ? 'oklch(27% 0.006 286.033)' : 'oklch(98% 0.003 247.858)',
  //       type: 'line',
  //       height: this.chartHeight,
  //       width: '100%',
  //       animations: {
  //         enabled: true,
  //         speed: 300,
  //         animateGradually: {
  //           enabled: true,
  //           delay: 150
  //         }
  //       },
  //       toolbar: {
  //         show: false
  //       }
  //     }
  //   };
  //
  //   // Show chart again in next tick to force re-render
  //   setTimeout(() => {
  //     this.showChart = true;
  //   }, 10);
  // }

  private updateChartTheme(theme: string): void {
    const isDark = theme === 'netrics_dark';
    this.showChart = false;

    const themedYAxis = Array.isArray(this.chartOptions.yaxis)
      ? this.chartOptions.yaxis.map(axis => ({
        ...axis,
        title: { ...axis.title, style: { ...axis.title?.style, color: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)' } },
        labels: { ...axis.labels, style: { ...axis.labels?.style, colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)' } }
      }))
      : {
        ...this.chartOptions.yaxis,
        title: { ...this.chartOptions.yaxis?.title, style: { color: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)' } },
        labels: { ...this.chartOptions.yaxis?.labels, style: { colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)' } }
      };

    this.chartOptions = {
      ...this.chartOptions,
      xaxis: { /* unchanged */ ...this.chartOptions.xaxis, labels: { ...this.chartOptions.xaxis?.labels, style: { colors: isDark ? 'oklch(70% 0.015 286.067)' : 'oklch(55% 0.046 257.417)' } } },
      yaxis: themedYAxis,
      // ...legend, stroke, grid, theme, chart unchanged as before
    };

    setTimeout(() => { this.showChart = true; }, 10);
  }

  private applyAnnotation(date?: string): void {
    if (!this.chart) return;

    const targetDate = date ?? this.annotationDate;

    if (!targetDate) {
      this.chart.updateOptions({ annotations: { xaxis: [] } }, false, false);
      return;
    }

    const ts = new Date(targetDate).getTime();

    this.chart.updateOptions({
      annotations: {
        xaxis: [
          {
            x: ts,
            strokeDashArray: 4,
            borderColor: '#FF4560',
            borderWidth: 2,
            label: {
              text: `Upgrade: ${targetDate}`,
              position: 'top',
              orientation: 'horizontal',
              borderColor: '#FF4560',
              style: {
                color: '#fff',
                background: '#FF4560',
                fontSize: '10px',
                padding: { left: 6, right: 6, top: 2, bottom: 2 }
              }
            }
          }
        ]
      }
    }, false, false);
  }

  private buildYAxis(): ApexYAxis | ApexYAxis[] {
    if (this.yaxisOverride) {
      return this.yaxisOverride;
    }

    const currentTheme = this.themeService.getCurrentTheme();
    const isDark = currentTheme === 'netrics_dark';

    return {
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
    };
  }
}
