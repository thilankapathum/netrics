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
import {LineChart} from '../../../../../components/charts/linechart/line-chart/line-chart';
import {KpiTrendDto} from '../../../../../models/pulse/KpiTrendDto';
import {KpidayService} from '../../../../../service/pulse/ltefdd/kpiday.service';
import {Observable} from 'rxjs';
import {KpiDataDto} from '../../../../../models/pulse/KpiDataDto';
import {ChartService} from '../../../../../service/components/chart/chart.service';
import {Linechart} from '../../../../../components/charts/linechart/linechart/linechart';
import {WorstCellCommentDto} from '../../../../../models/pulse/WorstCellCommentDto';
import {WorstCellCommentService} from '../../../../../service/pulse/dashboard/worst-cell-comment-service';
import {WorstCell} from '../../../../../models/pulse/WorstCell';
import {WorstCellsAndCommentsDto} from '../../../../../models/pulse/WorstCellsAndCommentsDto';

@Component({
  selector: 'app-dashboard-component',
  imports: [
    RouterLink,
    FormsModule,
    DatePipe,
    DecimalPipe,
    Linechart
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

  selectedPeriod = signal('day');
  selectedKpiTrendPeriod = signal<'month' | 'week' | 'quarter'>('month')

  selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');

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

  // isDropDownOpen:boolean = false;


  constructor(private router: Router,
              private areaTypeService: AreaTypeService,
              private areaService: AreaService,
              private dashboardService: DashboardService,
              private standardKpiService: StandardkpiService,
              private alertService: AlertService,
              private datePipe: DatePipe,
              private kpiDayService: KpidayService,
              private chartService: ChartService,
              private worstCellCommentService: WorstCellCommentService
  ) {
  }

  ngOnInit(): void {
    this.getAllStandardKpi(this.selectedRat())
  }

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
          this.alertService.error("KPI are unavailable for the RAT");
        }
      }, error: error => {
        console.log("Error getAllStandardKpi:");
        console.error(error);
        this.alertService.error("Standard KPI retrieval failed");
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
          this.alertService.error('Error getting areaTypes');
        }
      }
    )
  }

  getAreasByAreaType(areaTypeName: string) {
    this.areaService.getAreasByAreaTypes(areaTypeName).subscribe({
      next: data => {
        this.areas = data;
        this.area.set(this.areas.at(0)?.name);
        this.getTimestamps(this.selectedStandardKpi(), this.selectedPeriod(), this.area()!, this.selectedRat());
      }, error: error => {
        console.log(error);
        this.alertService.error('Error getting areasByAreaType');
      }
    })
  }

  getTimestamps(kpiName: string, period: string, areaName: string, ratName: string) {
    this.dashboardService.getTimestamps(kpiName, period, areaName, ratName).subscribe({
      next: data => {
        this.timestamps = data;
        this.timestamp.set(this.timestamps.at(0)!);
        this.getWorstCells(this.timestamp(), this.excludeZeroes);
      }, error: error => {
        console.log(error);
        this.alertService.error('Error getting timestamps');
      }
    })
  }

  //---------- SELECT FILTERS -------------

  selectStandardKpi(standardKpiName: string) {
    this.getTimestamps(standardKpiName, this.selectedPeriod(), this.area()!, this.selectedRat());
  }

  selectAreaType(areaType: string) {
    this.areaType.set(areaType);
    this.getAreasByAreaType(this.areaType()!);
  }

  selectArea(areaName: string) {
    this.area.set(areaName);
    this.getTimestamps(this.selectedStandardKpi(), this.selectedPeriod(), this.area()!, this.selectedRat());
  }

  //--------- WORST-CELLS ------------------------

  onExcludeZeroesChange(event: Event) {
    this.getWorstCells(this.timestamp(), this.excludeZeroes);
  }

  getWorstCells(date: Date, excludeZeroes: boolean) {
    this.loadingWorstCells = true;
    this.dashboardService.getWorstCellsByKpiAndArea(
      this.datePipe.transform(date, 'yyyy-MM-dd')!,
      this.selectedStandardKpi(),
      this.selectedPeriod(),
      this.area()!,
      excludeZeroes,
      this.selectedRat())
      .subscribe({
        next: data => {
          this.worstCells = data;
          this.setWorstCellComments(this.worstCells);
          this.loadingWorstCells = false;
        }, error: error => {
          console.log(error);
          this.loadingWorstCells = false;
          this.alertService.error('Error getting worstCellsByKpiAndArea');
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
    for (let comment of this.selectedWorstCellComments) {
      console.log('createdAt: ', comment.createdAt);
      console.log('modifiedAt: ', comment.lastModifiedAt);
    }
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
        this.alertService.error(`Error getting Comments for ${worstCell.cellName} ${worstCell.kpiLabel}`);
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
        this.alertService.success(`Comment '${this._comment()}' is added to ${worstCell.cellName} successfully!`);
        this._comment.set('');
        this.openDropdownCellId = null;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error creating comment: ${comment}`);
      }
    })
  }

  updateComment(worstCell: WorstCellsWithLatestDto, commentId: number, comment: string) {
    console.log('comment', comment);
    console.log('commentId', commentId);
    this.worstCellCommentService.updateComment(comment, commentId).subscribe({
      next: data => {
        console.log('data',data);
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
        this.alertService.success(`Comment '${comment?.comment}' is updated to ${worstCell.cellName} successfully!`);
        // this.openDropdownCellId = null;

      }, error: error => {
        console.log(error);
        this.alertService.error(`Error updating comment: ${comment} of ${worstCell.cellName}`);
      }
    })
  }

  editingComment(comment: string, event: Event) {
    event.stopPropagation();
    this.isEditingComment = true;
    this._comment.set(comment);
  }

  //----------- RAT SELECTION --------------------

  setSelectedRat(rat: 'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'): void {
    this.selectedRat.set(rat);
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
    // this.getTrendDataByKpi(this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.selectedRat());
  }

  getTrendDataByKpi(kpiName: string, cellName: string, period: string, ratName: string) {
    this.selectedCell.set(cellName);
    this.loadingKpiTrend = true;
    this.kpiTrendData = [];
    this.getTrendDataByKpiNameAndCell(kpiName, cellName, 'quarter', this.selectedRat())
      .subscribe({
        next: data => {
          this.chartSeries = this.chartService.buildSeriesKpiDataDto(data);
          this.loadingKpiTrend = false;
        }, error: err => {
          this.loadingKpiTrend = false;
          console.log("Error getDataByKpiLabelAndCell:");
          console.error(err);
          this.alertService.error("KPI Data retrieval failed");
        }
      })
  }

  getTrendDataByKpiNameAndCell(kpiName: string, cellName: string, period: string, ratName: string): Observable<KpiDataDto[]> {
    return this.kpiDayService.getDataByKpiAndCell(kpiName, cellName, period, ratName);
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

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const clickInside = (event.target as HTMLElement).closest('.dropdown');
    if (!clickInside) {
      this.openDropdownCellId = null;
    }
  }

}
