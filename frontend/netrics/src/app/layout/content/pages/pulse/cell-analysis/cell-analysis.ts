import {Component, OnDestroy, OnInit, computed, signal} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, takeUntil} from 'rxjs/operators';
import {StandardKpiDto} from '../../../../../models/pulse/StandardKpiDto';
import {StandardkpiService} from '../../../../../service/pulse/ltefdd/standardkpi.service';
import {AlertService} from '../../../../../components/alert/alert.service';
import {KpidayService} from '../../../../../service/pulse/ltefdd/kpiday.service';
import {ChartService} from '../../../../../service/components/chart/chart.service';
import {Linechart} from '../../../../../components/charts/linechart/linechart/linechart';
import {RatService} from '../../../../../service/pulse/rat-service';
import {RatDto} from '../../../../../models/pulse/RatDto';
import {SharedService} from '../../../../../service/pulse/shared-service';
import {CellNameDto} from '../../../../../models/pulse/CellNameDto';
import {CellNameService} from '../../../../../service/pulse/cell-name.service';
import {CellKpiSeries} from '../../../../../models/apexCharts/CellKpiSeries';
import {SectorDto} from '../../../../../models/pulse/SectorDto';
import {SectorService} from '../../../../../service/pulse/sector-service';
import {CellService} from '../../../../../service/pulse/cell-service';
import {HttpErrorResponse} from '@angular/common/http';
import {KpiDataWithOperandsDto} from '../../../../../models/pulse/KpiDataWithOperandsDto';
import {StandardRawKpiMappingService} from '../../../../../service/pulse/standard-raw-kpi-mapping-service';

@Component({
  selector: 'app-cell-analysis',
  imports: [
    ReactiveFormsModule,
    FormsModule,
    Linechart,
    RouterLink
  ],
  templateUrl: './cell-analysis.html',
  styleUrl: './cell-analysis.css'
})
export class CellAnalysis implements OnInit, OnDestroy {

  selectedGranularity = signal<string>('day-average');
  selectedRat = signal<string>('ltefdd');
  rat = signal<RatDto | undefined>(undefined);
  rats: RatDto[] = [];
  cellName = signal('');
  selectedStandardKpi = signal('');
  initialStandardKpi: string = '';
  standardKpis: StandardKpiDto[] = [];
  chartSeries = signal<CellKpiSeries[]>([]);
  trendPeriod = signal<'week' | 'month' | 'quarter'>('month');
  selectedChartType = signal<'s_cell_s_kpi' | 'm_cell_s_kpi' | 's_cell_m_kpi'>('s_cell_s_kpi');

  chartCellName = computed(() =>
    this.chartSeries().map(s => s.name));

  queryCell = signal('');
  filteredCells = signal<Array<CellNameDto>>([]);
  filteredSectors = signal<Array<SectorDto>>([]);

  private pendingRequests = signal<number>(0);
  loadingTrendData = computed(() => this.pendingRequests() > 0);
  loadingRats = signal(false);
  loadingStandardKpis = signal<boolean>(false);

  cellSelected = signal<boolean>(false);
  isCellSearchDropDownOpen = signal<boolean>(false);
  showOperands = signal<boolean>(false);
  standardRawKpiMappingAvailable = signal<boolean>(false);

  cellColorMap: Record<string, string> = {};
  colorPalette = [
    '#008FFB', '#00E396', '#FEB019', '#FF4560', '#775DD0',
    '#4caf50', '#ffdd00', '#546E7A', '#8D5B4C', '#C5D86D',
    '#2b908f', '#c200ff', '#66ca5b', '#ff00bf', '#ff8a47',
    '#00ff0c', '#e9006b', '#4ab5e7', '#9c2b08', '#caff00'
  ];

  // ---- lifecycle / race-condition guards ----
  private readonly destroy$ = new Subject<void>();
  private readonly searchTerms$ = new Subject<string>();
  /** Bumped every time a batch of trend-data fetches is (re)started, so late
   *  responses from a superseded batch can be detected and ignored. */
  private currentFetchToken = 0;

  constructor(private activatedRoute: ActivatedRoute,
              private standardKpiService: StandardkpiService,
              private alertService: AlertService,
              private kpidayService: KpidayService,
              private chartService: ChartService,
              private ratService: RatService,
              private sharedService: SharedService,
              private cellService: CellService,
              private cellNameService: CellNameService,
              private sectorService: SectorService,
              private standardRawKpiMappingService: StandardRawKpiMappingService) {

    if (sharedService.selectedCell() === '' && sharedService.selectedStandardKpi() === '') {
      this.cellSelected.set(false);
    } else {
      this.cellSelected.set(true);
      this.selectedGranularity.set(sharedService.selectedGranularity());
      this.selectedRat.set(sharedService.selectedRat());
      this.cellName.set(sharedService.selectedCell());
      this.initialStandardKpi = sharedService.selectedStandardKpi();
    }
  }

  ngOnInit(): void {
    this.getAllRats();

    // Debounced cell search: a single HTTP call per settled query, not one per keystroke.
    this.searchTerms$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(term => this.performCellSearch(term));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  setSelectedGranularity(granularity: 'day-average' | 'busy-hour') {
    this.selectedGranularity.set(granularity);
    if (this.cellSelected()) {
      this.refreshTrendDataForSelection();
    }
  }

  getAllRats(): void {
    this.loadingRats.set(true);
    this.ratService.getAllRats().pipe(takeUntil(this.destroy$)).subscribe({
      next: data => {
        this.rats = data;
        this.loadingRats.set(false);
        if (this.cellSelected()) {
          this.getRat(this.selectedRat());
        }
      }, error: err => {
        console.error(err);
        this.loadingRats.set(false);
        this.alertService.error('Retrieving RATs failed', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }

  getRat(ratName: string): RatDto | undefined {
    const _rat = this.rats.find(rat => rat.name === ratName);
    if (_rat) {
      this.setRat(_rat);
    } else {
      console.error('RAT is unavailable');
      this.alertService.error('RAT is unavailable');
      this.loadingRats.set(false);
    }
    return _rat;
  }

  setRat(rat: RatDto) {
    this.rat.set(rat);
    this.getAllStandardKpi(this.rat()?.name!);
  }

  getAllStandardKpi(ratName: string) {
    this.loadingStandardKpis.set(true);
    this.standardKpis = [];
    this.standardKpiService.getAllStandardKpi(ratName).pipe(takeUntil(this.destroy$)).subscribe({
      next: data => {
        this.standardKpis = data;
        if (this.standardKpis.length > 0) {
          const kpiInit = this.standardKpis.find(k => k.kpiName == this.initialStandardKpi);
          const kpiSelect = this.standardKpis.find(k => k.kpiName == this.selectedStandardKpi());

          if (this.selectedStandardKpi() !== '' && kpiSelect !== undefined) {
            this.selectedStandardKpi.set(kpiSelect.kpiName!);
          } else if (this.initialStandardKpi !== '' && kpiInit !== undefined) {
            this.selectedStandardKpi.set(kpiInit.kpiName!);
          } else {
            this.selectedStandardKpi.set(this.standardKpis[0].kpiName!);
          }
          this.selectKpi(this.selectedStandardKpi(), ratName, false, this.selectedGranularity());
          this.loadingStandardKpis.set(false);
        } else {
          this.alertService.error("KPI are unavailable for the RAT");
          this.loadingStandardKpis.set(false);
        }
      }, error: error => {
        console.error(error);
        this.alertService.error('Standard KPI retrieval failed', 'Error', `${error.status} ${error.statusText}`);
        this.loadingStandardKpis.set(false);
      }
    });
  }

  selectKpi(kpi: string, ratName: string, selectByOption: boolean, granularityName: string) {
    this.standardRawKpiMappingService.isMappingAvailable(ratName, kpi).pipe(takeUntil(this.destroy$)).subscribe({
      next: available => {
        this.standardRawKpiMappingAvailable.set(available);
        if (!available) {
          this.showOperands.set(false);
        }
        this.fetchTrendDataForKpi(kpi, ratName, selectByOption, granularityName);
      },
      error: error => {
        console.error("Error getting standardRawKpiMappingAvailable:", error);
        this.alertService.error('Standard-Raw-KPI-Mapping retrieval failed', 'Error', `${error.status} ${error.statusText}`);
        this.standardRawKpiMappingAvailable.set(false);
        this.showOperands.set(false);
        this.fetchTrendDataForKpi(kpi, ratName, selectByOption, granularityName);
      }
    });
  }

  private fetchTrendDataForKpi(kpi: string, ratName: string, selectByOption: boolean, granularityName: string) {
    if (selectByOption) {
      const cells = this.chartSeries().map(s => s.cellName);
      this.refetchCells(cells, kpi, ratName, granularityName);
    } else {
      if (this.selectedChartType() === 's_cell_s_kpi') {
        const token = this.nextFetchToken();
        this.chartSeries.set([]);
        this.getTrendDataByKpiAndCell(kpi, this.cellName(), this.trendPeriod(), ratName, granularityName, token);
      } else {
        const token = this.nextFetchToken();
        this.getTrendDataByKpiAndCell(kpi, this.cellName(), this.trendPeriod(), ratName, granularityName, token);
      }
    }
  }

  /** Clears the chart, bumps the fetch token, and re-fetches trend data for every given cell. */
  private refetchCells(cells: string[], kpi: string, ratName: string, granularityName: string): void {
    const token = this.nextFetchToken();
    this.chartSeries.set([]);
    for (const cell of cells) {
      this.getTrendDataByKpiAndCell(kpi, cell, this.trendPeriod(), ratName, granularityName, token);
    }
  }

  private nextFetchToken(): number {
    return ++this.currentFetchToken;
  }

  getTrendDataByKpiAndCell(kpiName: string, cellName: string, period: string, ratName: string,
                           granularityName: string, token: number = this.currentFetchToken) {
    if (this.showOperands() && this.selectedChartType() === 's_cell_s_kpi') {
      this.getTrendDataByKpiAndCellWithOperands(kpiName, cellName, period, ratName, granularityName, token);
    } else {
      this.getTrendDataByKpiAndCellPlain(kpiName, cellName, period, ratName, granularityName, token);
    }
  }

  private getTrendDataByKpiAndCellPlain(kpiName: string, cellName: string, period: string, ratName: string,
                                        granularityName: string, token: number) {
    this.pendingRequests.update(n => n + 1);
    const _granularity = this.resolveGranularity(period, granularityName);

    this.kpidayService.getDataByKpiAndCell(kpiName, cellName, period, ratName, _granularity)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: data => {
          this.pendingRequests.update(n => Math.max(0, n - 1));

          if (token !== this.currentFetchToken) {
            return; // a newer batch superseded this request; discard the stale result
          }

          if (data.length > 0) {
            if (this.selectedChartType() === 's_cell_s_kpi') {
              this.chartSeries.set(this.chartService.buildSeriesForCell(data));
            } else if (this.selectedChartType() === 'm_cell_s_kpi') {
              const seriesForCell = this.chartService.buildSeriesForCell(data);
              seriesForCell.forEach(s => {
                if (!this.cellColorMap[s.cellName]) {
                  const usedColors = Object.values(this.cellColorMap);
                  this.cellColorMap[s.cellName] = this.colorPalette.find(c => !usedColors.includes(c)) ?? '#000000';
                }
                s.color = this.cellColorMap[s.cellName];
              });
              this.chartSeries.update(existing => {
                const cellNamesToAdd = seriesForCell.map(s => s.name);
                const filtered = existing.filter(s => !cellNamesToAdd.includes(s.name));
                return [...filtered, ...seriesForCell];
              });
            }
          } else {
            console.error(`KPI trend data unavailable for the cell ${cellName}`);
            this.alertService.error(`KPI trend data unavailable for ${cellName}`);
          }
        }, error: error => {
          this.pendingRequests.update(n => Math.max(0, n - 1));
          console.error('Error retrieving KPI Trend data');
          console.error(error);
          this.alertService.error('KPI Trend data retrieval failed');
        }
      });
  }

  private getTrendDataByKpiAndCellWithOperands(kpiName: string, cellName: string, period: string, ratName: string,
                                               granularityName: string, token: number) {
    this.pendingRequests.update(n => n + 1);
    const _granularity = this.resolveGranularity(period, granularityName);

    this.kpidayService.getDataByKpiAndCellWithOperands(kpiName, cellName, period, ratName, _granularity)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: KpiDataWithOperandsDto[]) => {
          this.pendingRequests.update(n => Math.max(0, n - 1));

          if (token !== this.currentFetchToken) {
            return;
          }

          if (data.length > 0) {
            this.chartSeries.set(this.chartService.buildSeriesForCellWithOperands(data, cellName));
          } else {
            console.error(`KPI operand data unavailable for the cell ${cellName}`);
            this.alertService.error(`KPI trend data unavailable for ${cellName}`);
          }
        }, error: error => {
          this.pendingRequests.update(n => Math.max(0, n - 1));
          console.error('Error retrieving KPI operand trend data');
          console.error(error);
          this.alertService.error('KPI Trend data retrieval failed');
        }
      });
  }

  private resolveGranularity(period: string, selectedGranularity: string): string {
    return period === 'week' ? 'hour' : selectedGranularity;
  }

  onPeriodChange(event: Event) {
    this.refreshTrendDataForSelection();
  }

  private refreshTrendDataForSelection(): void {
    if (this.selectedChartType() === 'm_cell_s_kpi') {
      const cells = this.chartSeries().map(s => s.cellName);
      this.refetchCells(cells, this.selectedStandardKpi(), this.rat()?.name!, this.selectedGranularity());
    } else {
      const token = this.nextFetchToken();
      this.getTrendDataByKpiAndCell(
        this.selectedStandardKpi(), this.cellName(), this.trendPeriod(),
        this.rat()?.name!, this.selectedGranularity(), token
      );
    }
  }

  onSearchCell(value: string) {
    this.queryCell.set(value);

    if (value.length > 2) {
      this.searchTerms$.next(value);
    } else {
      this.filteredCells.set([]);
      this.filteredSectors.set([]);
    }
  }

  /** Runs once per debounced, de-duplicated search term (not once per keystroke,
   *  and not once per sort comparison). */
  private performCellSearch(value: string): void {
    this.searchSector(value);

    this.cellNameService.searchCell(value).pipe(takeUntil(this.destroy$)).subscribe({
      next: data => {
        const sortedData = [...data].sort((a, b) => {
          const ratA = a.ratName?.toLowerCase() ?? '';
          const ratB = b.ratName?.toLowerCase() ?? '';
          const ratCompare = ratA.localeCompare(ratB);
          if (ratCompare !== 0) {
            return ratCompare; // sort by ratName first
          }

          const nameA = a.cellName?.toLowerCase() ?? '';
          const nameB = b.cellName?.toLowerCase() ?? '';
          return nameA.localeCompare(nameB); // then sort by cellName
        });

        this.filteredCells.set(sortedData);
      }
    });
  }

  onShowOperandsChange(): void {
    const token = this.nextFetchToken();
    this.chartSeries.set([]);
    if (this.cellSelected() && this.selectedChartType() === 's_cell_s_kpi') {
      this.getTrendDataByKpiAndCell(
        this.selectedStandardKpi(), this.cellName(), this.trendPeriod(),
        this.rat()?.name!, this.selectedGranularity(), token
      );
    }
  }

  searchSector(value: string) {
    this.sectorService.searchSectorsByName(value).pipe(takeUntil(this.destroy$)).subscribe({
      next: data => {
        this.filteredSectors.set(data);
      },
      error: (error: HttpErrorResponse) => {
        console.warn('Sector search unavailable:', error.status, error.statusText);
        if (error.status !== 503 && error.status !== 502 && error.status !== 504) {
          this.alertService.error('Sector search failed', 'Error', `${error.status} ${error.statusText}`);
        }
      }
    });
  }

  selectCell(cellNameDto: CellNameDto, selectByDropDown: boolean) {
    if (selectByDropDown) {
      this.chartSeries.set([]);
    }

    this.cellSelected.set(true);
    this.cellName.set(cellNameDto.cellName!);
    this.queryCell.set(cellNameDto.cellName!);
    this.getRat(cellNameDto.ratName!);
  }

  selectSector(sectorDto: SectorDto, rat: RatDto) {
    this.chartSeries.set([]);
    this.cellColorMap = {};

    this.rat.set(rat);

    this.cellService.getCellsBySector(sectorDto.name, rat.name!).pipe(takeUntil(this.destroy$)).subscribe({
      next: cells => {
        if (cells.length === 0) return;

        // Load KPIs for this RAT first, then fetch trend data for all cells
        this.loadingStandardKpis.set(true);
        this.standardKpiService.getAllStandardKpi(rat.name!).pipe(takeUntil(this.destroy$)).subscribe({
          next: kpis => {
            this.standardKpis = kpis;
            if (kpis.length === 0) {
              this.alertService.error('KPI are unavailable for the RAT');
              this.loadingStandardKpis.set(false);
              return;
            }

            // Resolve which KPI to use (preserve previously selected if valid)
            const kpiToUse = kpis.find(k => k.kpiName === this.selectedStandardKpi())?.kpiName
              ?? kpis[0].kpiName!;
            this.selectedStandardKpi.set(kpiToUse);
            this.loadingStandardKpis.set(false);
            this.cellSelected.set(true);

            this.refetchCells(
              cells.map(c => c.cellName!),
              kpiToUse,
              rat.name!,
              this.selectedGranularity()
            );
          },
          error: err => {
            this.alertService.error('Standard KPI retrieval failed', 'Error', `${err.status} ${err.statusText}`);
            this.loadingStandardKpis.set(false);
          }
        });
      },
      error: err => {
        this.alertService.error('Failed to get cells for sector', 'Error', `${err.status} ${err.statusText}`);
      }
    });
  }

  setSelectedChartType(type: 's_cell_s_kpi' | 'm_cell_s_kpi' | 's_cell_m_kpi') {
    const previousType = this.selectedChartType();
    this.selectedChartType.set(type);

    if (type !== 's_cell_s_kpi') {
      this.showOperands.set(false);
    }

    if (previousType === 's_cell_s_kpi' && type === 'm_cell_s_kpi' && this.cellSelected() && this.cellName()) {
      // Carry the single-mode cell into multi-cell mode instead of losing it.
      this.cellColorMap = {};
      this.queryCell.set(this.cellName());
      this.refetchCells([this.cellName()], this.selectedStandardKpi(), this.rat()?.name!, this.selectedGranularity());

    } else if (previousType === 'm_cell_s_kpi' && type === 's_cell_s_kpi' && this.chartSeries().length > 0) {
      // Carry the first-selected multi-cell-mode cell into single-cell mode.
      // Routed through selectKpi() (not a direct fetch) so standardRawKpiMappingAvailable
      // is recomputed for the carried-over cell/KPI pair — otherwise the "Show Operands"
      // toggle would keep reflecting stale multi-cell-mode state.
      const firstCell = this.chartSeries()[0].cellName;
      this.chartSeries.set([]);
      this.cellName.set(firstCell);
      this.queryCell.set(firstCell);
      this.cellSelected.set(true);
      this.selectKpi(this.selectedStandardKpi(), this.rat()?.name!, false, this.selectedGranularity());

    } else {
      this.chartSeries.set([]);
    }
  }

  removeChartCell(name: string) {
    const removed = this.chartSeries().find(s => s.name === name);
    if (removed) {
      delete this.cellColorMap[removed.cellName];
    }
    this.chartSeries.update(series => series.filter(s => s.name !== name));
  }

  /** Clears every chart series and the associated color assignments together,
   *  so the color palette doesn't silently exhaust itself over a long session. */
  clearAllChartCells(): void {
    this.chartSeries.set([]);
    this.cellColorMap = {};
  }

  get modalYAxis(): ApexYAxis[] | undefined {
    if (!this.showOperands() || this.selectedChartType() !== 's_cell_s_kpi') return undefined;
    const label = this.standardKpis.find(k => k.kpiName === this.selectedStandardKpi())?.label ?? 'KPI Value';
    return [
      {seriesName: 'KPI Value', title: {text: label}},
      {seriesName: 'Numerator', opposite: true, title: {text: 'Count'}},
      {seriesName: 'Denominator', opposite: true, show: false}
    ];
  }

  //---------- UTILITY METHODS -------------------

  checkReturnPage(returnPage: string): boolean {
    const sharedCell: string = this.sharedService.selectedCell();
    const sharedStandardKpi: string = this.sharedService.selectedStandardKpi();
    const _returnPage: string = this.sharedService.returnPage;
    return sharedCell !== '' && sharedStandardKpi !== '' && returnPage === _returnPage;
  }

  clearSharedServiceData(): void {
    this.sharedService.clearAll();
  }

  isCellSelected(cellName: string): boolean {
    // Extract cell names from chartSeries which contains the actual cellName property
    const selectedCellNames = this.chartSeries().map(s => s.cellName);
    return selectedCellNames.includes(cellName);
  }

  loadingAll() {
    return this.loadingTrendData() || this.loadingRats() || this.loadingStandardKpis();
  }

}
