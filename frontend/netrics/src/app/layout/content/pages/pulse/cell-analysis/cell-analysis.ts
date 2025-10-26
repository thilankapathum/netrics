import {Component, OnInit, signal} from '@angular/core';
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
import {CellService} from '../../../../../service/pulse/cell-service';

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

  selectedRat = signal('');
  rat = signal<RatDto | undefined>(undefined);
  cellName = signal('');
  selectedStandardKpi = signal('');
  initialStandardKpi: string = '';
  kpiTrendData = signal<KpiTrendDto[]>([]);
  standardKpis: StandardKpiDto[] = [];
  chartSeries = signal<ApexAxisChartSeries>([]);
  trendPeriod = signal<'week' | 'month' | 'quarter'>('month');

  queryCell = signal('');
  filteredCells = signal<Array<CellNameDto>>([]);

  loadingTrendData = signal<boolean>(false);
  cellSelected = signal<boolean>(false);
  isCellSearchDropDownOpen = signal<boolean>(false);

  constructor(private activatedRoute: ActivatedRoute,
              private standardKpiService: StandardkpiService,
              private alertService: AlertService,
              private kpidayService: KpidayService,
              private chartService: ChartService,
              private ratService: RatService,
              private sharedService: SharedService,
              private cellService: CellService) {

    if (sharedService.selectedRat() === '' && sharedService.selectedCell() === '' && sharedService.selectedStandardKpi() === '') {
      this.cellSelected.set(false);
      // this.selectedRat.set(this.activatedRoute.snapshot.params['rat']);
    } else {
      this.cellSelected.set(true);
      this.selectedRat = sharedService.selectedRat;
      this.cellName.set(sharedService.selectedCell());
      this.initialStandardKpi = sharedService.selectedStandardKpi();
      this.getRat(this.selectedRat());
    }
  }

  ngOnInit(): void {
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
          const kpi = this.standardKpis.find(k => k.kpiName == this.initialStandardKpi)   // default loading initial-standard-kpi
          if (this.initialStandardKpi !== '' && kpi !== undefined) {
            this.selectedStandardKpi.set(kpi.kpiName!);
          } else {
            this.selectedStandardKpi.set(this.standardKpis[0].kpiName!);    // Set 1st standard KPI from the list as default KPI
          }
          this.selectKpi(this.selectedStandardKpi(), ratName);    // Getting Worst-cells and Trend-data
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

  selectKpi(kpi: string, ratName: string) {
    this.getTrendDataByKpiAndCell(kpi, this.cellName(), this.trendPeriod(), ratName);
  }

  getTrendDataByKpiAndCell(kpiName: string, cellName: string, period: string, ratName: string) {
    this.loadingTrendData.set(true);
    this.kpidayService.getDataByKpiAndCell(kpiName, cellName, period, ratName).subscribe({
      next: data => {
        this.kpiTrendData = data;
        if (this.kpiTrendData.length > 0) {
          this.chartSeries.set(this.chartService.buildSeriesKpiDataDto(data));
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
    this.getTrendDataByKpiAndCell(this.selectedStandardKpi(), this.cellName(), this.trendPeriod(), this.rat()?.name!)
  }

  onSearchCell(value: string) {
    this.queryCell.set(value);
    if (this.queryCell().length > 2) {
      this.cellService.searchCell(value).subscribe({
        next: data => {
          this.filteredCells.set(data)
        }
      });
    } else {
      this.filteredCells.set([])
    }
  }

  selectCell(cellNameDto: CellNameDto) {
    this.cellSelected.set(true);
    this.cellName.set(cellNameDto.cellName!);
    this.queryCell.set(cellNameDto.cellName!);
    this.getRat(cellNameDto.ratName!);
  }


}
