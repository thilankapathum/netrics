import {Component, HostListener, OnInit, signal} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {FormBuilder, FormsModule} from '@angular/forms';
import {AreaTypeDto} from '../../../../../models/pulse/AreaTypeDto';
import {AreaTypeService} from '../../../../../service/pulse/area-type-service';
import {AlertService} from '../../../../../components/alert/alert.service';
import {AreaDto} from '../../../../../models/pulse/AreaDto';
import {AreaService} from '../../../../../service/pulse/area-service';
import {DashboardService} from '../../../../../service/pulse/dashboard/dashboard-service';
import {StandardKpiDto} from '../../../../../models/pulse/StandardKpiDto';
import {StandardkpiService} from '../../../../../service/pulse/ltefdd/standardkpi.service';
import {WorstCellsWithLatestDto} from '../../../../../models/pulse/WorstCellsWithLatestDto';
import {DatePipe, DecimalPipe} from '@angular/common';
import {KpiTrendDto} from '../../../../../models/pulse/KpiTrendDto';
import {KpidayService} from '../../../../../service/pulse/ltefdd/kpiday.service';
import {Observable} from 'rxjs';
import {KpiDataDto} from '../../../../../models/pulse/KpiDataDto';
import {ChartService} from '../../../../../service/components/chart/chart.service';
import {Linechart} from '../../../../../components/charts/linechart/linechart/linechart';
import {WorstCellCommentDto} from '../../../../../models/pulse/WorstCellCommentDto';
import {WorstCellCommentService} from '../../../../../service/pulse/dashboard/worst-cell-comment-service';
import {WorstCellsAndCommentsDto} from '../../../../../models/pulse/WorstCellsAndCommentsDto';
import {AuthService} from '../../../../../auth/service/auth-service';
import {KeycloakProfile} from 'keycloak-js';
import {PdbCreateCells} from './pdb-create-cells/pdb-create-cells';
import {PsStandardKpi} from '../pulse-settings/ps-standard-kpi/ps-standard-kpi';

@Component({
  selector: 'app-dashboard-component',
  imports: [
    RouterLink,
    FormsModule,
    DatePipe,
    DecimalPipe,
    Linechart,
    PdbCreateCells,
    PsStandardKpi
  ],
  providers: [DatePipe],
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.css'
})
export class DashboardComponent implements OnInit {

  areaTypes: AreaTypeDto[] = [];
  areaType = signal<string | undefined>('');

  areas: AreaDto[] = [];
  area = signal<string | undefined>('')

  timestamps: Date[] = [];
  timestamp = signal<Date>(new Date());

  standardKpis: StandardKpiDto[] = [];
  selectedStandardKpi = signal('');

  selectedPeriod = signal('week');
  selectedKpiTrendPeriod = signal<'month' | 'week' | 'quarter'>('month')

  selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');

  selectedGranularity = signal<'day-average' | 'busy-hour'>('day-average');

  selectedCell = signal('');

  worstCells: Array<WorstCellsWithLatestDto> = [];

  _comment = signal('');
  selectedWorstCellComments: Array<WorstCellCommentDto> = [];
  worstCellsAndComments: Array<WorstCellsAndCommentsDto> = [];

  openDropdownCellId: number | null = null; //-- To track the specific worst-cell to display the popover

  kpiTrendData: KpiTrendDto[] = [];
  chartSeries: any = null;

  loadingKpiTrend: boolean = false;
  loadingWorstCells: boolean = false;
  excludeZeroes: boolean = false;

  isEditingComment: boolean = false;
  isAddingComment: boolean = false;
  isDeletingComment: boolean = false;    //TODO: Add deleting confirmation function

  editingCommentId: number | null = null;

  userProfile: KeycloakProfile = {};

  showCreateWorstCellsModal: boolean = false;

  constructor(private router: Router,
              private areaTypeService: AreaTypeService,
              private areaService: AreaService,
              private dashboardService: DashboardService,
              private standardKpiService: StandardkpiService,
              private alertService: AlertService,
              private datePipe: DatePipe,
              private kpiDayService: KpidayService,
              private chartService: ChartService,
              private worstCellCommentService: WorstCellCommentService,
              private authService: AuthService
  ) {
  }

  ngOnInit(): void {
    this.getAllStandardKpi(this.selectedRat())
    this.getUserProfile();
  }

  // ----------- GETTERS ---------------------------



  getAllStandardKpi(ratName: string) {
    this.standardKpis = [];
    this.standardKpiService.getAllStandardKpi(ratName).subscribe({
      next: data => {
        this.standardKpis = data;
        if (this.standardKpis.length > 0) {
          this.selectedStandardKpi.set(this.standardKpis[0].kpiName!);
          this.getAreaTypes();
          // this.selectKpi(this.selectedStandardKpi(), ratName);    // Getting Worst-cells and Trend-data
        } else {
          this.alertService.error(`KPI are unavailable for the RAT ${ratName}`);
        }
      }, error: error => {
        console.log("Error getAllStandardKpi");
        console.error(error);
        this.alertService.error(`Error getting Standard KPIs! (${error.status}:${error.statusText})`);
      }
    })
  }

  getAreaTypes() {
    this.areaTypeService.getAllAreaTypes().subscribe({
        next: data => {
          this.areaTypes = data;
          this.areaType.set(this.areaTypes.at(0)?.name);
          this.getAreasByAreaType(this.areaType()!);
        }, error: error => {
          console.log(error);
          this.alertService.error(`Error getting Area-types! (${error.status}:${error.statusText})`);
        }
      }
    )
  }

  getAreasByAreaType(areaTypeName: string) {
    this.areaService.getAreasByAreaTypes(areaTypeName).subscribe({
      next: data => {
        this.areas = data;
        this.area.set(this.areas.at(0)?.name);
        this.getTimestamps(this.selectedStandardKpi(), this.selectedPeriod(), this.area()!, this.selectedRat(), this.selectedGranularity());
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Areas! (${error.status}:${error.statusText})`);
      }
    })
  }

  getTimestamps(kpiName: string, period: string, areaName: string, ratName: string, granularityName: string) {
    this.dashboardService.getTimestamps(kpiName, period, areaName, ratName, granularityName).subscribe({
      next: data => {
        this.timestamps = data;

        const currentTimestamp = this.timestamps.find(ts => ts === this.timestamp());

        if (currentTimestamp === undefined) {
          this.timestamp.set(this.timestamps.at(0)!);
        }

        this.getWorstCells(this.timestamp(), this.excludeZeroes);
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Timestamps! (${error.status}:${error.statusText})`);
      }
    })
  }

  //---------- SELECT FILTERS -------------

  setSelectedGranularity(granularity: 'day-average' | 'busy-hour') {
    this.selectedGranularity.set(granularity);
    this.getTimestamps(this.selectedStandardKpi(), this.selectedPeriod(), this.area()!, this.selectedRat(), this.selectedGranularity());
  }

  selectStandardKpi(standardKpiName: string) {
    this.worstCells = [];
    this.getTimestamps(standardKpiName, this.selectedPeriod(), this.area()!, this.selectedRat(), this.selectedGranularity());
  }

  selectAreaType(areaType: string) {
    this.areaType.set(areaType);
    this.getAreasByAreaType(this.areaType()!);
  }

  selectArea(areaName: string) {
    this.area.set(areaName);
    this.getTimestamps(this.selectedStandardKpi(), this.selectedPeriod(), this.area()!, this.selectedRat(), this.selectedGranularity());
  }

  //--------- WORST-CELLS ------------------------

  onExcludeZeroesChange(event: Event) {
    this.getWorstCells(this.timestamp(), this.excludeZeroes);
  }

  getWorstCells(date: Date, excludeZeroes: boolean) {
    this.loadingWorstCells = true;
    this.worstCells = [];
    this.selectedWorstCellComments = []
    this.worstCellsAndComments = []
    this.dashboardService.getWorstCellsByKpiAndArea(
      this.datePipe.transform(date, 'yyyy-MM-dd')!,
      this.selectedStandardKpi(),
      this.selectedPeriod(),
      this.area()!,
      excludeZeroes,
      this.selectedRat(),
      this.selectedGranularity())
      .subscribe({
        next: data => {
          this.worstCells = data;
          this.setWorstCellComments(this.worstCells);
          this.loadingWorstCells = false;
        }, error: error => {
          console.log(error);
          this.loadingWorstCells = false;
          // this.alertService.error('Error getting Worst cells');
          this.alertService.error(`Error getting Worst cells! (${error.status}:${error.statusText})`);

        }
      });
  }

  //---------- WORST-CELL COMMENTS --------------------------

  openDropdownAndLoadComments(cell: WorstCellsWithLatestDto, event: Event) {
    event.stopPropagation();
    this.isEditingComment = false;
    this._comment.set('');
    this.openDropdownCellId = cell.id;
    this.getWorstCellComments(cell);
  }

  //-- Set comments for the selected cell to be displayed in the popover menu
  getWorstCellComments(worstCell: WorstCellsWithLatestDto) {
    this.selectedWorstCellComments = []
    const worstCellAndComments = this.worstCellsAndComments.find(wc => wc.worstCell == worstCell);
    this.selectedWorstCellComments = worstCellAndComments?.comments!;
  }

  //-- To display comment available/unavailable icon
  doesWorstCellHaveComments(worstCell: WorstCellsWithLatestDto): boolean {
    const worstCellAndComments = this.worstCellsAndComments.find(wc => wc.worstCell == worstCell);
    return worstCellAndComments?.comments.length! > 0;
  }

  //-- To set comments of the all worst-cells, at worst-cell querying time
  setWorstCellComments(worstCells: WorstCellsWithLatestDto[]) {
    for (let cell of worstCells) {
      this.getAllWorstCellComments(cell);
    }
  }

  //-- Get all comments of all worst-cells for the selected standard KPI
  getAllWorstCellComments(worstCell: WorstCellsWithLatestDto) {
    this.selectedWorstCellComments = [];
    this.worstCellCommentService.getCommentByWorstCell(worstCell.id).subscribe({
      next: data => {
        this.worstCellsAndComments.push({
          worstCell: worstCell, comments: data
        })
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Comments for ${worstCell.cellName} ${worstCell.kpiLabel} (${error.status}:${error.statusText})`);
      }
    })
  }


  createComment(worstCell: WorstCellsWithLatestDto, comment: string) {
    this.worstCellCommentService.createComment(comment, worstCell.id).subscribe({
      next: data => {
        const worstCellAndComments = this.worstCellsAndComments.find(wc => wc.worstCell.id === worstCell.id);

        //-- Update frontend's array to instantly display added comment
        if (worstCellAndComments) {
          worstCellAndComments.comments.push(data);
        } else {
          this.worstCellsAndComments.push({
            worstCell: worstCell, comments: [data]
          });
        }
        this.alertService.info(`Comment '${this._comment()}' is added to ${worstCell.cellName}`);
        this._comment.set('');
        this.isAddingComment = false;
        this.openDropdownCellId = null;
      }, error: error => {
        this._comment.set('');
        this.isAddingComment = false;
        this.openDropdownCellId = null;
        console.log(error);
        this.alertService.error(`Error creating comment! (${error.status}:${error.statusText})`);
      }
    })
  }

  updateComment(worstCell: WorstCellsWithLatestDto, commentId: number, comment: string) {
    this.worstCellCommentService.updateComment(comment, commentId).subscribe({
      next: data => {
        const worstCellAndComments = this.worstCellsAndComments.find(wc => wc.worstCell.id === worstCell.id);
        const comment = worstCellAndComments?.comments.find(c => c.id === data.id);

        if (comment) {
          comment.comment = data.comment;
          comment.lastModifiedAt = data.lastModifiedAt;
          comment.lastModifiedBy = data.lastModifiedBy;
        } else {
          worstCellAndComments?.comments.push(data);
        }
        this.isEditingComment = false;
        this.editingCommentId = null;
        this.alertService.info(`Comment '${comment?.comment}' is updated to ${worstCell.cellName}`);
        this._comment.set('');
        // this.openDropdownCellId = null;

      }, error: error => {
        console.log(error);
        this.alertService.error(`Error updating comment! (${error.status}:${error.statusText})`);
        this.isEditingComment = false;
        this.editingCommentId = null;
        this._comment.set('');
      }
    })
  }

  deleteComment(worstCell: WorstCellsWithLatestDto, commentId: number) {
    this.worstCellCommentService.deleteComment(commentId).subscribe({
      next: data => {
        const worstCellAndComments = this.worstCellsAndComments.find(wc => wc.worstCell.id === worstCell.id);

        if (worstCellAndComments) {
          worstCellAndComments.comments = worstCellAndComments.comments.filter(c => c.id !== data.id);
          this.alertService.info(`Comment was deleted successfully!`);
        }

        this.getAllWorstCellComments(worstCell);
        this.openDropdownCellId = null;


      }, error: error => {
        console.log(error);
        this.alertService.error(`Error deleting comment! (${error.status}:${error.statusText})`);
        this.getAllWorstCellComments(worstCell);
        this.openDropdownCellId = null;
      }
    })
  }

  editingComment(comment: string, commentId: number, event: Event) {
    event.stopPropagation();
    this.isEditingComment = true;
    this.editingCommentId = commentId;
    this._comment.set(comment);
  }

  addingComment() {
    this.isAddingComment = true;
  }

  //----------- RAT SELECTION --------------------

  setSelectedRat(rat: 'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'): void {
    this.selectedRat.set(rat);
    this.worstCells = [];
    // this.queryDateRanges();

    switch (rat) {
      case "ltefdd":
        this.getAllStandardKpi('ltefdd');
        break;
      case "ltetdd":
        this.getAllStandardKpi('ltetdd');
        break;
      case "nr":
        this.getAllStandardKpi('nr');
        break;
      case "umts":
        this.getAllStandardKpi('umts');
        break;
      case "gsm":
        this.getAllStandardKpi('gsm');
        break;
      default:
        this.getAllStandardKpi('ltefdd');
    }
  }

  //----------- KPI TREND CHART ----------------------

  onPeriodChange(event: Event) {
    this.getTrendDataByKpi(this.selectedStandardKpi(), this.selectedCell(), this.selectedKpiTrendPeriod(), this.selectedRat(), this.selectedGranularity());
  }

  getTrendDataByKpi(kpiName: string, cellName: string, period: string, ratName: string, granularityName: string) {
    this.selectedCell.set(cellName);
    this.loadingKpiTrend = true;
    this.kpiTrendData = [];
    this.getTrendDataByKpiNameAndCell(kpiName, cellName, 'quarter', ratName, granularityName)
      .subscribe({
        next: data => {
          this.chartSeries = this.chartService.buildSeriesKpiDataDto(data);
          this.loadingKpiTrend = false;
        }, error: err => {
          this.loadingKpiTrend = false;
          console.log("Error getDataByKpiLabelAndCell:");
          console.error(err);
          this.alertService.error(`Error getting data! (${err.status}:${err.statusText})`);
        }
      })
  }

  getTrendDataByKpiNameAndCell(kpiName: string, cellName: string, period: string, ratName: string, granularityName: string): Observable<KpiDataDto[]> {
    return this.kpiDayService.getDataByKpiAndCell(kpiName, cellName, period, ratName, granularityName);
  }

  //----------- UTILITY ------------------------------

  isKpiValueRed(kpiLabel: string, value: number): boolean {
    const standardKpi = this.standardKpis.find(kpi => kpi.label == kpiLabel);
    if (!standardKpi || standardKpi.threshold == null || !standardKpi.worstOrder) {
      return false;
    }
    if (standardKpi.worstOrder === 'ASC') {
      return value! < standardKpi.threshold;
    }
    if (standardKpi.worstOrder === 'DESC') {
      return value! > standardKpi.threshold;
    }
    return false;
  }

  //-- Click outside a popover menu
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const clickInside = (event.target as HTMLElement).closest('.dropdown');
    if (!clickInside) {
      this.openDropdownCellId = null;
      this._comment.set('');
      this.isEditingComment = false;
      this.editingCommentId = null;
      this.isAddingComment = false;
    }
  }

  isAuthorized(comment: WorstCellCommentDto): boolean {
    return comment.createdBy === this.userProfile.id;
  }

  isTrendDataAvailable() {
    return !(this.chartSeries == null);
  }

  //---------- OPEN MODALS ------------------------

  openCreateWorstCellsModal() {
    this.showCreateWorstCellsModal = true;
  }

  //---------- CLOSE MODALS -----------------------

  closeCreateWorstCellsModal() {
    this.showCreateWorstCellsModal = false;
  }

  //============== USER VALIDATION =============================

  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
  }

  hasAnyRole(roles:string[]){
    return this.authService.hasAnyRole(roles);
  }


}
