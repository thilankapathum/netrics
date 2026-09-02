import {Component, computed, OnInit, QueryList, signal, ViewChildren} from '@angular/core';
import {CellKpiSeries} from '../../../../../models/apexCharts/CellKpiSeries';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Linechart} from '../../../../../components/charts/linechart/linechart/linechart';
import {RouterLink} from '@angular/router';
import {RatDto} from '../../../../../models/pulse/RatDto';
import {CellNameDto} from '../../../../../models/pulse/CellNameDto';
import {SectorDto} from '../../../../../models/pulse/SectorDto';
import {StandardKpiDto} from '../../../../../models/pulse/StandardKpiDto';
import {StandardkpiService} from '../../../../../service/pulse/ltefdd/standardkpi.service';
import {AlertService} from '../../../../../components/alert/alert.service';
import {KpidayService} from '../../../../../service/pulse/ltefdd/kpiday.service';
import {ChartService} from '../../../../../service/components/chart/chart.service';
import {RatService} from '../../../../../service/pulse/rat-service';
import {SharedService} from '../../../../../service/pulse/shared-service';
import {CellService} from '../../../../../service/pulse/cell-service';
import {CellNameService} from '../../../../../service/pulse/cell-name.service';
import {SectorService} from '../../../../../service/pulse/sector-service';
import {HttpErrorResponse} from '@angular/common/http';

/** All loaded data for one KPI tile. */
export interface KpiTile {
  kpiName: string;
  kpiLabel: string;
  series: CellKpiSeries[];
  loading: boolean;
}

@Component({
  selector: 'app-upgrade-review',
  imports: [ReactiveFormsModule, FormsModule, Linechart, RouterLink],
  templateUrl: './upgrade-review.html',
  styleUrl: './upgrade-review.css'
})
export class UpgradeReview implements OnInit{

  @ViewChildren(Linechart) lineCharts!: QueryList<Linechart>;

  // ── Grid layout ──────────────────────────────────────────────────────────
  selectedCols = signal<1 | 2 | 3>(2);

  // ── Global filters ────────────────────────────────────────────────────────
  selectedGranularity = signal<string>('day-average');
  trendPeriod = signal<'week' | 'month' | 'quarter'>('month');
  upgradeDate = signal<string>('');

  // ── RAT ──────────────────────────────────────────────────────────────────
  rat = signal<RatDto | undefined>(undefined);
  rats: RatDto[] = [];
  loadingRats = signal(false);

  // ── Chart mode ────────────────────────────────────────────────────────────
  selectedChartType = signal<'s_cell_s_kpi' | 'm_cell_s_kpi'>('s_cell_s_kpi');

  // ── Cell selection ────────────────────────────────────────────────────────
  cellSelected = signal(false);
  cellName = signal('');                        // primary cell (s_cell mode)
  selectedCells = signal<CellNameDto[]>([]);    // all selected cells

  queryCell = signal('');
  filteredCells = signal<CellNameDto[]>([]);
  filteredSectors = signal<SectorDto[]>([]);

  // ── KPI selection ─────────────────────────────────────────────────────────
  standardKpis: StandardKpiDto[] = [];
  selectedKpiNames = signal<Set<string>>(new Set());
  loadingStandardKpis = signal(false);

  // ── Tiles (one per selected KPI) ──────────────────────────────────────────
  kpiTiles = signal<KpiTile[]>([]);

  // ── Color palette (shared across cells in multi-cell mode) ────────────────
  cellColorMap: Record<string, string> = {};
  readonly colorPalette = [
    '#008FFB', '#00E396', '#FEB019', '#FF4560', '#775DD0',
    '#4caf50', '#ffdd00', '#546E7A', '#8D5B4C', '#C5D86D',
    '#2b908f', '#c200ff', '#66ca5b', '#ff00bf', '#ff8a47',
    '#00ff0c', '#e9006b', '#4ab5e7', '#9c2b08', '#caff00'
  ];

  getCellColor(cellName: string): string {
    // cellColorMap is populated during series fetch for m_cell mode
    return this.cellColorMap[cellName] ?? '#008FFB';
  }

  // ── Derived ───────────────────────────────────────────────────────────────
  loadingAny = computed(() =>
    this.loadingRats() || this.loadingStandardKpis() ||
    this.kpiTiles().some(t => t.loading)
  );

  gridClass = computed(() => {
    switch (this.selectedCols()) {
      case 1: return 'grid-cols-1';
      case 3: return 'grid-cols-1 lg:grid-cols-3';
      default: return 'grid-cols-1 md:grid-cols-2';
    }
  });

  constructor(
    private standardKpiService: StandardkpiService,
    private alertService: AlertService,
    private kpidayService: KpidayService,
    private chartService: ChartService,
    private ratService: RatService,
    private sharedService: SharedService,
    private cellService: CellService,
    private cellNameService: CellNameService,
    private sectorService: SectorService
  ) {}

  ngOnInit(): void {
    this.getAllRats();
  }

  setSelectedCols(cols: 1 | 2 | 3): void {
    this.selectedCols.set(cols);
  }

  clearUpgradeDate(): void {
    this.upgradeDate.set('');
  }

  // ── RAT ──────────────────────────────────────────────────────────────────

  getAllRats(): void {
    this.loadingRats.set(true);
    this.ratService.getAllRats().subscribe({
      next: data => {
        this.rats = data;
        this.loadingRats.set(false);
      },
      error: err => {
        this.alertService.error('Retrieving RATs failed', 'Error', `${err.status} ${err.statusText}`);
        this.loadingRats.set(false);
      }
    });
  }

  getRatByName(ratName: string): RatDto | undefined {
    const found = this.rats.find(r => r.name === ratName);
    if (found) {
      this.rat.set(found);
    } else {
      this.alertService.error('RAT is unavailable');
    }
    return found;
  }

  // ── Standard KPIs ────────────────────────────────────────────────────────

  loadStandardKpis(ratName: string): void {
    this.loadingStandardKpis.set(true);
    this.standardKpis = [];
    this.standardKpiService.getAllStandardKpi(ratName).subscribe({
      next: data => {
        this.standardKpis = data;
        this.loadingStandardKpis.set(false);

        // Retain only selections that are valid for the new RAT
        this.selectedKpiNames.update(prev => {
          const validNames = new Set(data.map((k: StandardKpiDto) => k.kpiName!));
          const next = new Set<string>();
          prev.forEach(n => { if (validNames.has(n)) next.add(n); });
          return next;
        });

        // Rebuild tiles to reflect valid KPIs
        this.rebuildAllTiles();
      },
      error: () => {
        this.alertService.error('Standard KPI retrieval failed');
        this.loadingStandardKpis.set(false);
      }
    });
  }

  // ── KPI checkbox toggle ──────────────────────────────────────────────────

  toggleKpi(kpiName: string): void {
    this.selectedKpiNames.update(prev => {
      const next = new Set(prev);
      if (next.has(kpiName)) {
        next.delete(kpiName);
        // Remove the corresponding tile immediately
        this.kpiTiles.update(tiles => tiles.filter(t => t.kpiName !== kpiName));
      } else {
        next.add(kpiName);
        // Add a new tile and fetch data for it
        this.addTileForKpi(kpiName);
      }
      return next;
    });
  }

  isKpiSelected(kpiName: string): boolean {
    return this.selectedKpiNames().has(kpiName);
  }

  // ── Tile management ──────────────────────────────────────────────────────

  private addTileForKpi(kpiName: string): void {
    const kpiDto = this.standardKpis.find(k => k.kpiName === kpiName);
    if (!kpiDto) return;

    const tile: KpiTile = {
      kpiName,
      kpiLabel: kpiDto.label ?? kpiName,
      series: [],
      loading: true
    };
    this.kpiTiles.update(tiles => [...tiles, tile]);

    const cells = this.getActiveCells();
    if (cells.length === 0) return;

    this.fetchSeriesForTile(kpiName, cells);
  }

  /** Full rebuild: used when global filters change or RAT switches. */
  private rebuildAllTiles(): void {
    const selectedKpis = Array.from(this.selectedKpiNames());
    const cells = this.getActiveCells();

    // Reset all tiles to loading
    this.kpiTiles.set(
      selectedKpis.map(kpiName => {
        const kpiDto = this.standardKpis.find(k => k.kpiName === kpiName);
        return {
          kpiName,
          kpiLabel: kpiDto?.label ?? kpiName,
          series: [],
          loading: true
        };
      })
    );

    if (cells.length === 0) return;

    for (const kpiName of selectedKpis) {
      this.fetchSeriesForTile(kpiName, cells);
    }
  }

  /** Fetch trend data for every cell for a given KPI and update the tile. */
  private fetchSeriesForTile(kpiName: string, cells: string[]): void {
    const ratName = this.rat()?.name;
    if (!ratName) return;

    const granularity = this.resolveGranularity(this.trendPeriod(), this.selectedGranularity());
    const period = this.trendPeriod();

    // Track how many sub-requests are outstanding for this tile
    let pending = cells.length;
    let accumulated: CellKpiSeries[] = [];

    const markLoading = (loading: boolean) =>
      this.kpiTiles.update(tiles =>
        tiles.map(t => t.kpiName === kpiName ? {...t, loading} : t)
      );

    const updateSeries = (series: CellKpiSeries[]) =>
      this.kpiTiles.update(tiles =>
        tiles.map(t => t.kpiName === kpiName ? {...t, series} : t)
      );

    markLoading(true);

    for (const cellName of cells) {
      this.kpidayService.getDataByKpiAndCell(kpiName, cellName, period, ratName, granularity)
        .subscribe({
          next: data => {
            if (data.length > 0) {
              const series = this.chartService.buildSeriesForCell(data);

              // Apply consistent colors in multi-cell mode
              if (this.selectedChartType() === 'm_cell_s_kpi') {
                series.forEach(s => {
                  if (!this.cellColorMap[s.cellName]) {
                    const used = Object.values(this.cellColorMap);
                    this.cellColorMap[s.cellName] =
                      this.colorPalette.find(c => !used.includes(c)) ?? '#000000';
                  }
                  s.color = this.cellColorMap[s.cellName];
                });
              }

              accumulated = [...accumulated, ...series];
            }
            pending--;
            if (pending === 0) {
              updateSeries(accumulated);
              markLoading(false);
            }
          },
          error: () => {
            this.alertService.error(`KPI trend data unavailable for ${cellName}`);
            pending--;
            if (pending === 0) {
              updateSeries(accumulated);
              markLoading(false);
            }
          }
        });
    }
  }

  private getActiveCells(): string[] {
    if (this.selectedChartType() === 'm_cell_s_kpi') {
      return this.selectedCells().map(c => c.cellName!);
    }
    return this.cellName() ? [this.cellName()] : [];
  }

  private resolveGranularity(period: string, selectedGranularity: string): string {
    return period === 'week' ? 'hour' : selectedGranularity;
  }

  // ── Global filter handlers ────────────────────────────────────────────────

  setSelectedGranularity(granularity: 'day-average' | 'busy-hour'): void {
    this.selectedGranularity.set(granularity);
    if (this.cellSelected()) this.rebuildAllTiles();
  }

  onPeriodChange(): void {
    if (this.cellSelected()) this.rebuildAllTiles();
  }

  // ── Cell / Sector selection ───────────────────────────────────────────────

  onSearchCell(value: string): void {
    this.queryCell.set(value);
    if (value.length > 2) {
      this.cellNameService.searchCell(value).subscribe({
        next: data => {
          const sorted = [...data].sort((a, b) => {
            const ra = a.ratName?.toLowerCase() ?? '';
            const rb = b.ratName?.toLowerCase() ?? '';
            const rc = ra.localeCompare(rb);
            return rc !== 0 ? rc : (a.cellName ?? '').localeCompare(b.cellName ?? '');
          });
          this.filteredCells.set(sorted);
          this.searchSector(value);
        }
      });
    } else {
      this.filteredCells.set([]);
      this.filteredSectors.set([]);
    }
  }

  searchSector(value: string): void {
    this.sectorService.searchSectorsByName(value).subscribe({
      next: data => this.filteredSectors.set(data),
      error: (err: HttpErrorResponse) => {
        if (err.status !== 503 && err.status !== 502 && err.status !== 504) {
          this.alertService.error('Sector search failed', 'Error', `${err.status} ${err.statusText}`);
        }
      }
    });
  }

  selectCell(cellDto: CellNameDto): void {
    if (this.selectedChartType() === 's_cell_s_kpi') {
      // Single-cell mode: replace current cell
      this.selectedCells.set([cellDto]);
      this.cellName.set(cellDto.cellName!);
      this.cellSelected.set(true);
      this.queryCell.set(cellDto.cellName!);

      const rat = this.getRatByName(cellDto.ratName!);
      if (rat) this.loadStandardKpis(rat.name!);

    } else {
      // Multi-cell mode: toggle
      const existing = this.selectedCells();
      const idx = existing.findIndex(c => c.cellName === cellDto.cellName);

      if (idx >= 0) {
        // Deselect
        const updated = existing.filter(c => c.cellName !== cellDto.cellName);
        this.selectedCells.set(updated);
        delete this.cellColorMap[cellDto.cellName!];
        if (updated.length === 0) this.cellSelected.set(false);
      } else {
        // Add
        const ratSwitched = this.rat()?.name !== cellDto.ratName;
        if (ratSwitched) {
          // Different RAT → clear all and restart
          this.selectedCells.set([cellDto]);
          this.cellColorMap = {};
          this.cellSelected.set(true);
          const rat = this.getRatByName(cellDto.ratName!);
          if (rat) this.loadStandardKpis(rat.name!);
          return;
        }
        this.selectedCells.update(prev => [...prev, cellDto]);
        this.cellSelected.set(true);
      }
      this.rebuildAllTiles();
    }
  }

  selectSector(sectorDto: SectorDto, rat: RatDto): void {
    this.cellColorMap = {};
    this.rat.set(rat);
    this.selectedCells.set([]);

    this.cellService.getCellsBySector(sectorDto.name, rat.name!).subscribe({
      next: cells => {
        if (cells.length === 0) return;
        this.selectedCells.set(cells);
        this.cellSelected.set(true);

        this.loadingStandardKpis.set(true);
        this.standardKpiService.getAllStandardKpi(rat.name!).subscribe({
          next: kpis => {
            this.standardKpis = kpis;
            this.loadingStandardKpis.set(false);
            if (kpis.length > 0) this.rebuildAllTiles();
          },
          error: () => {
            this.alertService.error('Standard KPI retrieval failed');
            this.loadingStandardKpis.set(false);
          }
        });
      },
      error: err => this.alertService.error('Failed to get cells', 'Error', `${err.statusText}`)
    });
  }

  isCellSelected(cellName: string): boolean {
    return this.selectedCells().some(c => c.cellName === cellName);
  }

  removeCellFromChart(cellName: string): void {
    delete this.cellColorMap[cellName];
    this.selectedCells.update(prev => prev.filter(c => c.cellName !== cellName));
    if (this.selectedCells().length === 0) this.cellSelected.set(false);
    this.rebuildAllTiles();
  }

  // ── Chart type switch ─────────────────────────────────────────────────────

  setSelectedChartType(type: 's_cell_s_kpi' | 'm_cell_s_kpi'): void {
    this.selectedChartType.set(type);
    this.selectedCells.set([]);
    this.cellName.set('');
    this.cellColorMap = {};
    this.cellSelected.set(false);
    this.kpiTiles.set([]);
  }

  // ── Tile removal (close button on tile) ──────────────────────────────────

  removeTile(kpiName: string): void {
    this.selectedKpiNames.update(prev => {
      const next = new Set(prev);
      next.delete(kpiName);
      return next;
    });
    this.kpiTiles.update(tiles => tiles.filter(t => t.kpiName !== kpiName));
  }

}
