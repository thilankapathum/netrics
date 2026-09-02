import {Component, computed, effect, inject, input, OnInit, signal} from '@angular/core';
import {AnomalyCellsService} from '../../../../../service/pulse/anomaly-cells-service';
import {AlarmCorrelationFilter, AnomalyCellDto, SortDir, SortField} from '../../../../../models/pulse/AnomalyCellDto';
import {DecimalPipe} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {RatDto} from '../../../../../models/pulse/RatDto';
import {GranularityDto} from '../../../../../models/pulse/GranularityDto';
import {AreaTypeDto} from '../../../../../models/pulse/AreaTypeDto';
import {AreaDto} from '../../../../../models/pulse/AreaDto';
import {KeycloakProfile} from 'keycloak-js';
import {GranularityService} from '../../../../../service/pulse/granularity-service';
import {AreaTypeService} from '../../../../../service/pulse/area-type-service';
import {AreaService} from '../../../../../service/pulse/area-service';
import {AlertService} from '../../../../../components/alert/alert.service';
import {AuthService} from '../../../../../auth/service/auth-service';
import {UserAreaService} from '../../../../../service/pulse/user-area-service';
import {RatService} from '../../../../../service/pulse/rat-service';
import {CellAnalysisModal} from './cell-analysis-modal/cell-analysis-modal';
import {CellMapCellAnalysis} from '../kpi-map/cell-map-cell-analysis/cell-map-cell-analysis';
import {StandardKpiDto} from '../../../../../models/pulse/StandardKpiDto';
import {StandardkpiService} from '../../../../../service/pulse/ltefdd/standardkpi.service';
import {AnomalySummaryModal} from './anomaly-summary-modal/anomaly-summary-modal';

@Component({
  selector: 'app-anomaly-cells',
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    RouterLink,
    FormsModule,
    CellAnalysisModal,
    CellMapCellAnalysis,
    AnomalySummaryModal
  ],
  templateUrl: './anomaly-cells.html',
  styleUrl: './anomaly-cells.css'
})
export class AnomalyCells implements OnInit {

  period = input<string>('day');

  rat = signal<string | undefined>(undefined);
  rats: RatDto[] = [];

  granularity = signal<string | undefined>(undefined);
  granularities: GranularityDto[] = [];

  areaType = signal<string | undefined>('');
  areaTypes: AreaTypeDto[] = [];

  area = signal<string | undefined>('')
  areas: AreaDto[] = [];

  userArea = signal<AreaDto | undefined>(undefined);
  userProfile: KeycloakProfile = {};

  kpiFilter = signal<string | null>(null);
  standardKpis: StandardKpiDto[] = [];  // adjust type to your actual model

  // Table state
  cells = signal<AnomalyCellDto[]>([]);
  totalElements = signal(0);
  totalPages = signal(0);
  page = signal(0);
  pageSize = signal(10);
  sortBy = signal<SortField>('severity');
  sortDir = signal<SortDir>('desc');
  severityFilter = signal<string | null>(null);
  alarmCorrelationFilter = signal<AlarmCorrelationFilter>('all');
  loadingAnomalyCells = signal(false);

  pageSizeOptions = [10, 20, 50, 100];

  pageNumbers = computed(() => {
    const total = this.totalPages();
    const current = this.page();
    // Show a small window of page numbers around the current page
    const windowSize = 5;
    const start = Math.max(0, Math.min(current - Math.floor(windowSize / 2), total - windowSize));
    const end = Math.min(total, start + windowSize);
    return Array.from({length: Math.max(0, end - start)}, (_, i) => start + i);
  });

  loadingRats: boolean = false;
  loadingGranularity: boolean = false;
  loadingAreaTypes: boolean = false;
  loadingAreas: boolean = false;
  loadingStandardKpis: boolean = false;

  showCellAnalysisModal = signal<boolean>(false);
  showSummaryModal = signal<boolean>(false);
  selectedCellName = signal<string>('');
  standardKpi = signal<string | undefined>(undefined);

  constructor(private anomalyCellsService: AnomalyCellsService,
              private ratService: RatService,
              private granularityService: GranularityService,
              private areaTypeService: AreaTypeService,
              private areaService: AreaService,
              private alertService: AlertService,
              private router: Router,
              private authService: AuthService,
              private userAreaService: UserAreaService,
              private standardKpiService: StandardkpiService) {
    effect(() => {
      const area = this.area();
      const rat = this.rat();
      const granularity = this.granularity();
      const period = this.period();
      const page = this.page();
      const pageSize = this.pageSize();
      const sortBy = this.sortBy();
      const sortDir = this.sortDir();
      const severityFilter = this.severityFilter();
      const alarmCorrelationFilter = this.alarmCorrelationFilter();
      this.kpiFilter();

      // Guard: don't fetch until all required filters have real values
      if (!area || !rat || !granularity) {
        return;
      }

      this.fetchCells();
    });
  }

  ngOnInit(): void {
    this.getUserProfile();
    this.getGranularities();
  }

  loadingAll() {
    return this.loadingAreaTypes || this.loadingAreas || this.loadingRats || this.loadingGranularity || this.loadingStandardKpis || this.loadingAnomalyCells();
  }

  //------------ FILTERS ---------------------------

  selectAreaType(areaType: string) {
    this.areaType.set(areaType);
    this.getAreasByAreaType(this.areaType()!);
    this.page.set(0);
  }

  selectArea(areaName: string) {
    this.area.set(areaName);
    this.page.set(0);
  }

  setGranularity(granularity: 'day-average' | 'busy-hour') {
    this.granularity.set(granularity);
    this.page.set(0);
  }

  setRat(rat: string) {
    this.rat.set(rat);
    this.getStandardKpisForRat(rat);
    this.page.set(0);
  }

  //----------- GETTERS ----------------------------------


  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
    this.getAreaByUserId(this.userProfile.id!);
  }

  getAreaByUserId(userId: string) {
    this.userAreaService.findByUserId(userId).subscribe({
      next: data => {
        this.userArea.set(data);
        // this.ngOnInitRemaining();
        this.getAreaTypes();

      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Area for User. ${error.status} ${error.statusText}`);
      }
    })
  }

  getAreaTypes() {
    this.loadingAreaTypes = true;
    this.areaTypeService.getAllAreaTypes().subscribe({
        next: data => {
          this.areaTypes = data;
          const districtsAreaType = this.areaTypes.find(at => at.name === 'District');
          if (this.userArea() != null) {
            this.areaType.set(this.userArea()?.areaTypeName);
          } else {
            this.areaType.set(districtsAreaType?.name);
          }
          this.getAreasByAreaType(this.areaType()!);
          this.loadingAreaTypes = false;
        }, error: error => {
          console.log(error);
          this.alertService.error(`Error getting Area-types! (${error.status}:${error.statusText})`);
          this.loadingAreaTypes = false;
        }
      }
    )
  }


  getAreasByAreaType(areaTypeName: string) {
    this.loadingAreas = true;
    this.areaService.getAreasByAreaTypes(areaTypeName).subscribe({
      next: data => {
        this.areas = data;
        if (this.userArea() != null) {
          this.area.set(this.userArea()?.name);
          this.userArea.set(undefined);   // Clear userArea details after initial loading
        } else {
          this.area.set(this.areas.at(0)?.name);
        }

        this.loadingAreas = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Areas! (${error.status}:${error.statusText})`);
        this.loadingAreas = false;
      }
    })
  }

  getGranularities() {
    this.loadingGranularity = true;
    this.granularityService.getAllGranularities().subscribe({
      next: data => {
        this.granularities = data;
        this.granularity.set(this.granularities[0]?.name);
        this.getRats();
        this.loadingGranularity = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error retrieving granularities. ${error.status}:${error.statusText}`);
        this.loadingGranularity = false;
      }
    });
  }

  getRats() {
    this.loadingRats = true;
    this.ratService.getAllRats().subscribe({
      next: data => {
        this.rats = data;
        this.rat.set(this.rats[0]?.name);
        // this.fetchCells();
        this.getStandardKpisForRat(this.rat()!);
        this.loadingRats = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error retrieving RATs. ${error.status}:${error.statusText}`);
        this.loadingRats = false;
      }
    });
  }

  getStandardKpisForRat(ratName: string) {
    this.loadingStandardKpis = true;
    this.standardKpiService.getAllStandardKpi(ratName).subscribe({
      next: data => {
        this.standardKpis = data;
        this.loadingStandardKpis = false;
      },
      error: error => {
        console.log(error);
        this.alertService.error(`Error retrieving KPIs. ${error.status}:${error.statusText}`);
        this.loadingStandardKpis = false;
      }
    });
  }

  //------------- FETCHING CELLS -----------

  private fetchCells(): void {
    this.loadingAnomalyCells.set(true);
    this.anomalyCellsService
      .getAnomalyCells({
        period: this.period(),
        areaName: this.area()!,
        ratName: this.rat()!,
        granularityName: this.granularity()!,
        severity: this.severityFilter(),
        kpiName: this.kpiFilter(),
        sortBy: this.sortBy(),
        sortDir: this.sortDir(),
        page: this.page(),
        pageSize: this.pageSize(),
        alarmCorrelation: this.alarmCorrelationFilter() === 'all' ? null : this.alarmCorrelationFilter(),
      })
      .subscribe({
        next: (res) => {
          this.cells.set(res.content);
          this.totalElements.set(res.totalElements);
          this.totalPages.set(res.totalPages);
          this.loadingAnomalyCells.set(false);
        },
        error: () => {
          this.loadingAnomalyCells.set(false);
        },
      });
  }

  onKpiFilterChange(value: string): void {
    this.kpiFilter.set(value === 'all' ? null : value);
    this.page.set(0);
  }



  onSort(field: SortField): void {
    if (this.sortBy() === field) {
      this.sortDir.set(this.sortDir() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortBy.set(field);
      this.sortDir.set('desc');
    }
    this.page.set(0); // reset to first page on re-sort
  }

  onSeverityFilterChange(value: string): void {
    this.severityFilter.set(value === 'all' ? null : value);
    this.page.set(0);
  }

  onAlarmCorrelationFilterChange(value: string): void {
    this.alarmCorrelationFilter.set(value as AlarmCorrelationFilter);
    this.page.set(0);
  }

  onPageSizeChange(value: string): void {
    this.pageSize.set(Number(value));
    this.page.set(0);
  }

  goToPage(p: number): void {
    if (p >= 0 && p < this.totalPages()) {
      this.page.set(p);
    }
  }

  sortIcon(field: SortField): string {
    if (this.sortBy() !== field) return '';
    return this.sortDir() === 'asc' ? '↑' : '↓';
  }

  // component — replace sortIcon() with two helpers
  sortIconChar(field: SortField): string {
    if (this.sortBy() !== field) return '⇅';
    return this.sortDir() === 'asc' ? '↑' : '↓';
  }

  isActiveSortField(field: SortField): boolean {
    return this.sortBy() === field;
  }

  severityDotClass(severity: string | null): string {
    switch (severity) {
      case 'critical':
        return 'bg-error';
      case 'high':
        return 'bg-warning';
      case 'moderate':
        return 'bg-warning opacity-50';
      default:
        return '';
    }
  }

  severityBorderClass(severity: string | null): string {
    switch (severity) {
      case 'critical':
        return 'border-l-error';
      case 'high':
        return 'border-l-warning';
      case 'moderate':
        return 'border-l-warning/40';
      default:
        return 'border-l-transparent';
    }
  }

  severityLabel(severity: string | null): string {
    switch (severity) {
      case 'critical':
        return 'Critical anomaly — statistically extreme deviation';
      case 'high':
        return 'High anomaly — significant deviation from baseline';
      case 'moderate':
        return 'Moderate anomaly — notable deviation from baseline';
      default:
        return '';
    }
  }

  //--------- MODAL ---------------

  closeCellAnalysisModal(): void {
    this.showCellAnalysisModal.set(false);
  }

  openCellAnalysisModal(cellName:string, kpiName:string): void {
    this.showCellAnalysisModal.set(true);
    this.selectedCellName.set(cellName);
    this.standardKpi.set(kpiName);
  }

  openSummaryModal(): void {
    this.showSummaryModal.set(true);
  }

  closeSummaryModal(): void {
    this.showSummaryModal.set(false);
  }

  protected readonly open = open;
}
