import {Component, computed, OnInit, signal} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {StandardKpiDto} from '../../../../../models/pulse/StandardKpiDto';
import {StandardkpiService} from '../../../../../service/pulse/ltefdd/standardkpi.service';
import {AlertService} from '../../../../../components/alert/alert.service';
import {KpidayService} from '../../../../../service/pulse/ltefdd/kpiday.service';
import {KpiTrendDto} from '../../../../../models/pulse/KpiTrendDto';
import {ChartService} from '../../../../../service/components/chart/chart.service';
import {Linechart} from '../../../../../components/charts/linechart/linechart/linechart';
import {RatService} from '../../../../../service/pulse/rat-service';
import {RatDto} from '../../../../../models/pulse/RatDto';
import {SharedService} from '../../../../../service/pulse/shared-service';
import {CellNameDto} from '../../../../../models/pulse/CellNameDto';
import {CellNameService} from '../../../../../service/pulse/cell-name.service';
import {KpiDataDto} from '../../../../../models/pulse/KpiDataDto';
import {CellKpiSeries} from '../../../../../models/apexCharts/CellKpiSeries';
import {SectorDto} from '../../../../../models/pulse/SectorDto';
import {SectorService} from '../../../../../service/pulse/sector-service';
import {CellService} from '../../../../../service/pulse/cell-service';
import {HttpErrorResponse} from '@angular/common/http';

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
export class CellAnalysis implements OnInit {

  // selectedGranularity = signal<'day-average' | 'busy-hour'>('day-average');
  selectedGranularity = signal<string>('day-average');
  // selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');
  selectedRat = signal<string>('ltefdd');
  rat = signal<RatDto | undefined>(undefined);
  rats: RatDto[] = [];
  cellName = signal('');
  selectedStandardKpi = signal('');
  initialStandardKpi: string = '';
  kpiTrendData = signal<KpiTrendDto[]>([]);
  standardKpis: StandardKpiDto[] = [];
  chartSeries = signal<CellKpiSeries[]>([]);
  trendPeriod = signal<'week' | 'month' | 'quarter'>('month');
  selectedChartType = signal<'s_cell_s_kpi' | 'm_cell_s_kpi' | 's_cell_m_kpi'>('s_cell_s_kpi');

  chartCellName = computed(() =>
    this.chartSeries().map(s => s.name));

  queryCell = signal('');
  filteredCells = signal<Array<CellNameDto>>([]);
  filteredSectors = signal<Array<SectorDto>>([]);

  // loadingTrendData = signal<boolean>(false);
  private pendingRequests = signal<number>(0);
  loadingTrendData = computed(() => this.pendingRequests() > 0);
  loadingRats = signal(false);
  loadingStandardKpis = signal<boolean>(false);

  cellSelected = signal<boolean>(false);
  isCellSearchDropDownOpen = signal<boolean>(false);

  cellColorMap: Record<string, string> = {};
  colorPalette = [
    '#008FFB', // blue
    '#00E396', // orange
    '#FEB019', // green
    '#FF4560', // red
    '#775DD0', // purple
    '#4caf50', // brown
    '#ffdd00', // pink
    '#546E7A', // gray
    '#8D5B4C', // yellow-green
    '#C5D86D',  // cyan
    '#2b908f', // blue
    '#c200ff', // orange
    '#66ca5b', // green
    '#ff00bf', // red
    '#ff8a47', // purple
    '#00ff0c', // brown
    '#e9006b', // pink
    '#4ab5e7', // gray
    '#9c2b08', // yellow-green
    '#caff00'  // cyan
  ];

  constructor(private activatedRoute: ActivatedRoute,
              private standardKpiService: StandardkpiService,
              private alertService: AlertService,
              private kpidayService: KpidayService,
              private chartService: ChartService,
              private ratService: RatService,
              private sharedService: SharedService,
              private cellService: CellService,
              private cellNameService: CellNameService,
              private sectorService: SectorService) {

    if (sharedService.selectedCell() === '' && sharedService.selectedStandardKpi() === '') {
      this.cellSelected.set(false);
    } else {
      this.cellSelected.set(true);
      this.selectedGranularity.set(sharedService.selectedGranularity());
      this.selectedRat.set(sharedService.selectedRat());
      this.cellName.set(sharedService.selectedCell());
      this.initialStandardKpi = sharedService.selectedStandardKpi();
      // this.getRat(this.selectedRat());
    }
  }

  ngOnInit(): void {
    this.getAllRats();
  }

  setSelectedGranularity(granularity: 'day-average' | 'busy-hour') {
    this.selectedGranularity.set(granularity);
    if (this.cellSelected()) {  // Query KPI only if a cell is selected
      this.getRat(this.rat()?.name!);
    }
  }

  getAllRats(): void {
    this.loadingRats.set(true);
    this.ratService.getAllRats().subscribe({
      next: data => {
        this.rats = data;
        this.loadingRats.set(false);
        if (this.cellSelected()) {
          this.getRat(this.selectedRat());
        }
      }, error: err => {
        console.error(err);
        this.alertService.error(`Retirieving RATs failed. ${err.statusCode} ${err.statusText}`);
      }
    })
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
    this.standardKpiService.getAllStandardKpi(ratName).subscribe({
      next: data => {
        this.standardKpis = data;
        if (this.standardKpis.length > 0) {
          const kpiInit = this.standardKpis.find(k => k.kpiName == this.initialStandardKpi)   // default loading initial-standard-kpi
          const kpiSelect = this.standardKpis.find(k => k.kpiName == this.selectedStandardKpi())   // default loading (previously) selected-standard-kpi

          if (this.selectedStandardKpi() !== '' && kpiSelect !== undefined) {
            this.selectedStandardKpi.set(kpiSelect.kpiName!);
          } else if (this.initialStandardKpi !== '' && kpiInit !== undefined) {
            this.selectedStandardKpi.set(kpiInit.kpiName!);
          } else {
            this.selectedStandardKpi.set(this.standardKpis[0].kpiName!);    // Set 1st standard KPI from the list as default KPI
          }
          this.selectKpi(this.selectedStandardKpi(), ratName, false, this.selectedGranularity());    // Getting Worst-cells and Trend-data
          this.loadingStandardKpis.set(false);
        } else {
          this.alertService.error("KPI are unavailable for the RAT");
          this.loadingStandardKpis.set(false);
        }
      }, error: error => {
        console.error(error);
        this.alertService.error(`Standard KPI retrieval failed. ${error.status} ${error.statusText}`);
        this.loadingStandardKpis.set(false);
      }
    })
  }

  selectKpi(kpi: string, ratName: string, selectByOption: boolean, granularityName: string) {
    if (selectByOption) {
      const cells = this.chartSeries().map(s => s.cellName);
      this.chartSeries.set([]);
      for (let cell of cells) {
        this.getTrendDataByKpiAndCell(kpi, cell, this.trendPeriod(), ratName, granularityName);
      }
    } else {
      if (this.selectedChartType() === 's_cell_s_kpi') {
        this.chartSeries.set([]);
      }
      this.getTrendDataByKpiAndCell(kpi, this.cellName(), this.trendPeriod(), ratName, granularityName);
    }
  }

  getTrendDataByKpiAndCell(kpiName: string, cellName: string, period: string, ratName: string, granularityName: string) {
    this.pendingRequests.update(n => n + 1);

    const _granularity = this.resolveGranularity(period, granularityName);

    this.kpidayService.getDataByKpiAndCell(kpiName, cellName, period, ratName, _granularity).subscribe({
      next: data => {
        this.kpiTrendData.set(data);
        if (this.kpiTrendData().length > 0) {
          if (this.selectedChartType() === 's_cell_s_kpi') {    //-- Single-cell Single-KPI scenario
            this.chartSeries.set(this.chartService.buildSeriesForCell(data));
          } else if (this.selectedChartType() === 'm_cell_s_kpi') {   //-- Multi-cell Single KPI scenario

            const seriesForCell = this.chartService.buildSeriesForCell(data);

            //-- Setting consistent color for each cell
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
          this.pendingRequests.update(n => Math.max(0, n - 1));
        } else {
          console.error(`KPI trend data unavailable for the cell ${cellName}`);
          this.alertService.error(`KPI trend data unavailable for ${cellName}`);
          this.pendingRequests.update(n => Math.max(0, n - 1));
        }
      }, error: error => {
        this.pendingRequests.update(n => Math.max(0, n - 1));
        console.error('Error retrieving KPI Trend data');
        console.error(error);
        this.alertService.error(`KPI Trend data retrieval failed. ${error.status} ${error.statusText}`);
      }
    })
  }

  private resolveGranularity(period: string, selectedGranularity: string): string {
    return period === 'week' ? 'hour' : selectedGranularity;
  }

  onPeriodChange(event: Event) {
    if (this.selectedChartType() === 'm_cell_s_kpi') {
      const cells = this.chartSeries().map(s => s.cellName);
      this.chartSeries.set([]);
      for (const cell of cells) {
        this.getTrendDataByKpiAndCell(
          this.selectedStandardKpi(), cell, this.trendPeriod(),
          this.rat()?.name!, this.selectedGranularity()
        );
      }
    } else {
      this.getTrendDataByKpiAndCell(
        this.selectedStandardKpi(), this.cellName(), this.trendPeriod(),
        this.rat()?.name!, this.selectedGranularity()
      );
    }
  }

  onSearchCell(value: string) {
    this.queryCell.set(value);

    if (this.queryCell().length > 2) {

      //-- Querying cells
      this.cellNameService.searchCell(value).subscribe({
        next: data => {
          const sortedData = [...data].sort((a, b) => {

            this.searchSector(value);

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


    } else {
      this.filteredCells.set([]);
      this.filteredSectors.set([]);
    }
  }

  searchSector(value: string) {
    this.sectorService.searchSectorsByName(value).subscribe({
      next: data => {
        this.filteredSectors.set(data);
      },
      error: (error: HttpErrorResponse) => {
        console.warn('Sector search unavailable:', error.status, error.statusText);
        // this.filteredSectors.set([]);  // just clear sectors silently

        // Only alert for unexpected errors, not 503/502/504 gateway issues
        if (error.status !== 503 && error.status !== 502 && error.status !== 504) {
          this.alertService.error(`Sector search failed. ${error.status} ${error.statusText}`);
        }
      }
    });
  }

  selectCell(cellNameDto: CellNameDto, selectByDropDown: boolean) {
    if (selectByDropDown) {   //-- Check if the RAT of the selecting cell in frontend is different from current RAT
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

    this.cellService.getCellsBySector(sectorDto.name, rat.name!).subscribe({
      next: cells => {
        if (cells.length === 0) return;

        // Load KPIs for this RAT first, then fetch trend data for all cells
        this.loadingStandardKpis.set(true);
        this.standardKpiService.getAllStandardKpi(rat.name!).subscribe({
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

            // Now fetch trend data for all cells with a known KPI
            for (const cell of cells) {
              this.getTrendDataByKpiAndCell(
                kpiToUse,
                cell.cellName!,
                this.trendPeriod(),
                rat.name!,
                this.selectedGranularity()
              );
            }
          },
          error: err => {
            this.alertService.error(`Standard KPI retrieval failed. ${err.status} ${err.statusText}`);
            this.loadingStandardKpis.set(false);
          }
        });
      },
      error: err => {
        this.alertService.error(`Failed to get cells for sector. ${err.status} ${err.statusText}`);
      }
    });
  }

  setSelectedChartType(type: 's_cell_s_kpi' | 'm_cell_s_kpi' | 's_cell_m_kpi') {
    this.selectedChartType.set(type);
    this.chartSeries.set([]);
  }

  removeChartCell(name: string) {
    const removed = this.chartSeries().find(s => s.name === name);
    if (removed) {
      delete this.cellColorMap[removed.cellName];
    }
    this.chartSeries.update(series => series.filter(s => s.name !== name));
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
