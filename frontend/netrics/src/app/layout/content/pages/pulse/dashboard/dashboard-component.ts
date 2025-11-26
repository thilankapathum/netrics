import {Component, OnInit, signal} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {FormBuilder, FormsModule} from '@angular/forms';
import {AreaTypeDto} from '../../../../../models/pulse/AreaTypeDto';
import {AreaTypeService} from '../../../../../service/pulse/area-type-service';
import {AlertService} from '../../../../../components/alert/alert.service';
import {AreaDto} from '../../../../../models/pulse/AreaDto';
import {AreaService} from '../../../../../service/pulse/area-service';
import {DashboardService} from '../../../../../service/pulse/dashboard/dashboard-service';
// import {DatePipe} from '@angular/common';
import {StandardKpiDto} from '../../../../../models/pulse/StandardKpiDto';
import {StandardkpiService} from '../../../../../service/pulse/ltefdd/standardkpi.service';
import {WorstCellsDashboardDto} from '../../../../../models/pulse/WorstCellsDashboardDto';
import {DatePipe} from '@angular/common';

@Component({
  selector: 'app-dashboard-component',
  imports: [
    RouterLink,
    FormsModule,
    DatePipe
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

  selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');

  worstCells: Array<WorstCellsDashboardDto> = [];


  constructor(private router: Router,
              private areaTypeService: AreaTypeService,
              private areaService: AreaService,
              private dashboardService: DashboardService,
              private standardKpiService: StandardkpiService,
              private alertService: AlertService,
              private datePipe: DatePipe
              ) {
  }

  ngOnInit(): void {
    // this.getAreaTypes();
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
        console.log("Timestamps data: ", data);
        this.timestamps = data;
        this.timestamp.set(this.timestamps.at(0)!);
        this.getWorstCells(this.timestamp());
      }, error: error => {
        console.log(error);
        this.alertService.error('Error getting timestamps');
      }
    })
  }

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
    console.log(areaName);
  }

  getWorstCells(date: Date) {
    this.dashboardService.getWorstCellsByKpiAndArea(
      this.datePipe.transform(date, 'yyyy-MM-dd')!,
      this.selectedStandardKpi(),
      this.selectedPeriod(),
      this.area()!,
      false,
      this.selectedRat())
      .subscribe({
        next: data => {
          this.worstCells = data;
          console.log("Worst cell data: ", data);
        }, error: error => {
          console.log(error);
          this.alertService.error('Error getting worstCellsByKpiAndArea');
        }
      });
  }


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


}
