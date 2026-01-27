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

  selectedGranularity = signal<'day-average' | 'busy-hour'>('day-average');
  selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');
  rat = signal<RatDto | undefined>(undefined);
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

  loadingTrendData = signal<boolean>(false);
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
              private cellService: CellNameService) {

    if (sharedService.selectedCell() === '' && sharedService.selectedStandardKpi() === '') {
      this.cellSelected.set(false);
    } else {
      this.cellSelected.set(true);
      this.selectedGranularity.set(sharedService.selectedGranularity());
      this.selectedRat.set(sharedService.selectedRat());
      this.cellName.set(sharedService.selectedCell());
      this.initialStandardKpi = sharedService.selectedStandardKpi();
      this.getRat(this.selectedRat());
    }
  }

  ngOnInit(): void {
  }

  setSelectedGranularity(granularity: 'day-average' | 'busy-hour') {
    this.selectedGranularity.set(granularity);
    if (this.cellSelected()) {  // Query KPI only if a cell is selected
      this.getRat(this.rat()?.name!);
    }
  }

  getRat(ratName: string) {
    this.ratService.findByName(ratName).subscribe({
      next: data => {
        if (data.name != undefined) {   //-- Validate RAT
          this.rat.set(data);
          this.getAllStandardKpi(this.rat()?.name!);    // Get all standard KPI of the RAT
        } else {
          console.error('RAT is unavailable');
          this.alertService.error('RAT is unavailable');
        }
      }, error: error => {
        console.error('Error retrieving RAT');
        console.error(error);
        this.alertService.error('Error retrieving RAT');
      }
    })
  }

  getAllStandardKpi(ratName: string) {
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
        } else {
          this.alertService.error("KPI are unavailable for the RAT");
        }
      }, error: error => {
        console.log("Error getAllStandardKpi:");
        console.error(error);
        this.alertService.error("Standard KPI retrieval failed");
      }
    })
  }

  selectKpi(kpi: string, ratName: string, selectByOption: boolean, granularityName: string) {
    if (selectByOption) {   //-- To check whether KPI selected by client's Option group. To clear existing charts if KPI is changed

      const cells: string[] = []
      this.chartSeries().forEach(s => cells.push(s.cellName));  //-- Record current cell list to query new KPI for the same cells

      this.chartSeries.set([]); //-- Reset Chart

      //-- Querying newly selected KPI for the already selected cells
      for (let cell of cells) {
        this.getTrendDataByKpiAndCell(kpi, cell, this.trendPeriod(), ratName, granularityName);
      }
    } else {    //-- selectKpi method is called from another method (not from client)
      this.getTrendDataByKpiAndCell(kpi, this.cellName(), this.trendPeriod(), ratName, granularityName);
    }
  }

  getTrendDataByKpiAndCell(kpiName: string, cellName: string, period: string, ratName: string, granularityName: string) {
    this.loadingTrendData.set(true);
    this.kpidayService.getDataByKpiAndCell(kpiName, cellName, period, ratName, granularityName).subscribe({
      next: data => {
        this.kpiTrendData = data;
        if (this.kpiTrendData.length > 0) {
          if (this.selectedChartType() === 's_cell_s_kpi') {    //-- Single-cell Single-KPI scenario
            // this.chartSeries.set(this.chartService.buildSeriesKpiDataDto(data));
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
          this.loadingTrendData.set(false);
        } else {
          console.error(`KPI trend data unavailable for the cell ${cellName}`);
          this.alertService.error(`KPI trend data unavailable for ${cellName}`);
          this.loadingTrendData.set(false);
        }
      }, error: error => {
        this.loadingTrendData.set(false);
        console.error('Error retrieving KPI Trend data');
        console.error(error);
        this.alertService.error('KPI Trend data retrieval failed');
      }
    })
  }

  onPeriodChange(event: Event) {
    this.getTrendDataByKpiAndCell(this.selectedStandardKpi(), this.cellName(), this.trendPeriod(), this.rat()?.name!, this.selectedGranularity())
  }

  onSearchCell(value: string) {
    this.queryCell.set(value);

    if (this.queryCell().length > 2) {
      this.cellService.searchCell(value).subscribe({
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
    } else {
      this.filteredCells.set([]);
    }
  }

  selectCell(cellNameDto: CellNameDto, selectByDropDown: boolean) {
    if (selectByDropDown) {   //-- Check if the RAT of the selecting cell in frontend is different from current RAT
      console.log(this.selectedRat())
      this.chartSeries.set([]);
    }

    this.cellSelected.set(true);
    this.cellName.set(cellNameDto.cellName!);
    this.queryCell.set(cellNameDto.cellName!);
    this.getRat(cellNameDto.ratName!);
  }

  setSelectedChartType(type: 's_cell_s_kpi' | 'm_cell_s_kpi' | 's_cell_m_kpi') {
    this.selectedChartType.set(type);

    switch (type) {
      case 's_cell_s_kpi':
        this.chartSeries.set([]);
        break;
      case "m_cell_s_kpi":
        this.chartSeries.set([]);
        break;
    }
  }

  removeChartCell(name: string) {
    this.chartSeries.update(series =>
      series.filter(s => s.name !== name));
  }

  //---------- UTILITY METHODS -------------------

  isDataInSharedService():boolean {
    const sharedCell: string = this.sharedService.selectedCell();
    const sharedStandardKpi: string = this.sharedService.selectedStandardKpi();
    return sharedCell !== '' && sharedStandardKpi !== '';
  }

  clearSharedServiceData(): void {
    this.sharedService.clearAll();
  }

}
