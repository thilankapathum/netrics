import {Component, computed, CUSTOM_ELEMENTS_SCHEMA, signal} from '@angular/core';
import {DatePipe} from "@angular/common";
import {FormBuilder, FormsModule} from "@angular/forms";
import {firstValueFrom} from 'rxjs';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {AreaTypeDto} from '../../../../../../models/pulse/AreaTypeDto';
import {AreaDto} from '../../../../../../models/pulse/AreaDto';
import {AreaService} from '../../../../../../service/pulse/area-service';
import {RatService} from '../../../../../../service/pulse/rat-service';
import {RatDto} from '../../../../../../models/pulse/RatDto';
import {AreaTypeService} from '../../../../../../service/pulse/area-type-service';
import {GranularityService} from '../../../../../../service/pulse/granularity-service';
import {GranularityDto} from '../../../../../../models/pulse/GranularityDto';
import {KpiReportService} from '../../../../../../service/pulse/kpi-reports/kpi-report-service';
import {StandardKpiDto} from '../../../../../../models/pulse/StandardKpiDto';
import {StandardkpiService} from '../../../../../../service/pulse/ltefdd/standardkpi.service';

@Component({
  selector: 'app-site-wise-report-by-kpi-and-date',
  imports: [
    DatePipe,
    FormsModule
  ],
  templateUrl: './site-wise-report-by-kpi-and-date.html',
  styleUrl: './site-wise-report-by-kpi-and-date.css',
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class SiteWiseReportByKpiAndDate {

  rat = signal('');
  rats: RatDto[] = [];

  standardKpi = signal('');
  standardKpis: StandardKpiDto[] = [];

  areaType = signal<string | undefined>('');
  areaTypes: AreaTypeDto[] = [];

  area = signal<string | undefined>('');
  areas: AreaDto[] = [];

  granularity = signal('');
  granularities: GranularityDto[] = [];

  startDate = signal<string>('');
  endDate = signal<string>('');

  downloadingCsv: boolean = false;
  loadingRats:boolean = false;
  loadingStandardKpis:boolean = false;
  loadingAreaTypes:boolean = false;
  loadingAreas:boolean = false;
  loadingGranularities:boolean = false;


  invalidRange = computed(() => {
    const start = this.startDate();
    const end = this.endDate();

    const startTime = new Date(start).getTime();
    const endTime = new Date(end).getTime();

    if (!start || !end) return true;
    return endTime < startTime;
  })



  constructor(private alertService: AlertService,
              private areaService: AreaService,
              private ratService: RatService,
              private areaTypeService: AreaTypeService,
              private granularityService: GranularityService,
              private kpiReportService: KpiReportService,
              private standardKpiService: StandardkpiService) {
    this.getAllGranularities();
    this.getAllRats();
    this.getAllAreaTypes();
  }

  loadingAll(){
    return this.downloadingCsv || this.loadingRats || this.loadingStandardKpis || this.loadingAreaTypes || this.loadingAreas || this.loadingGranularities;
  }

  //================== GETTERS =============================

  getAllRats(): void {
    this.loadingRats = true;
    this.ratService.getAllRats().subscribe({
      next: data => {
        this.rats = data;
        this.rat.set(this.rats[0].name!);
        this.getAllStandardKpiByRat(this.rats[0]);
        this.loadingRats = false;
      }, error: err => {
        console.error(err);
        this.alertService.error('Error retrieving RATs');
        this.loadingRats = false;
      }
    })
  }

  getAllStandardKpiByRat(rat: RatDto) {
    this.loadingStandardKpis = true;
    this.standardKpis = [];
    this.standardKpiService.getAllStandardKpi(rat.name!).subscribe({
      next: data => {
        this.standardKpis = data;
        if (this.standardKpis.length > 0) {
          this.standardKpi.set(this.standardKpis[0].kpiName!);
        } else {
          this.alertService.error(`Error retrieving Standard KPI for ${rat.label}`);
        }
        this.loadingStandardKpis = false;
      }, error: error => {
        console.log("Error getAllStandardKpi:");
        console.error(error);
        this.alertService.error("Standard KPI retrieval failed");
        this.loadingStandardKpis = false;
      }
    })
  }

  getAllGranularities() {
    this.loadingGranularities = true;
    this.granularityService.getAllGranularities().subscribe({
      next: data => {
        this.granularities = data;
        this.granularity.set(this.granularities[0].name!)
        this.loadingGranularities = false;
      }, error: err => {
        console.error(err);
        this.alertService.error("Error retrieving Granularities");
        this.loadingGranularities = false;
      }
    })
  }

  getAllAreaTypes() {
    this.loadingAreaTypes = true;
    this.areaTypeService.getAllAreaTypes().subscribe({
        next: data => {
          this.areaTypes = data;
          this.areaType.set(this.areaTypes[0].name!);
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
        this.area.set(this.areas.at(0)?.name);
        this.loadingAreas = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Areas! (${error.status}:${error.statusText})`);
        this.loadingAreas = false;
      }
    })
  }

  //================== FILTERS =========================

  selectRat(ratName: string) {
    this.rat.set(ratName);
    const _rat = this.rats.find(r => r.name! === ratName);
    if (_rat) {
      this.getAllStandardKpiByRat(_rat);
    } else {
      this.alertService.error(`Error retrieving Standard KPI for ${ratName}`);
    }
    console.log(this.rat());
  }

  selectStandardKpi(kpiName: string) {
    // this.standardKpi.set(kpiName);
    console.log(kpiName);
  }

  selectAreaType(areaType: string) {
    this.areaType.set(areaType);
    this.getAreasByAreaType(this.areaType()!);
    console.log(this.areaType());
  }

  selectArea(areaName: string) {
    this.area.set(areaName);
    console.log(this.area());
  }

  selectGranularity(granularity: string) {
    this.granularity.set(granularity);
    console.log(granularity);
  }

  onStartDateChange(event: any) {
    // Cally emits event.target.value
    this.startDate.set(event.target.value);
    console.log('Selected date:', this.startDate());
  }

  onEndDateChange(event: any) {
    // Cally emits event.target.value
    this.endDate.set(event.target.value);
    console.log('Selected date:', this.endDate());
  }

  downloadCsv() {

    if (!this.startDate || !this.endDate) {
      this.alertService.warning(`Select a valid date range!`);
      return;
    }

    if (this.invalidRange()) {
      this.alertService.warning(`End date must be later than start date`);
      return;
    }

    this.downloadingCsv = true;
    this.kpiReportService.exportSiteWiseReportByKpiAndDate(this.rat(), this.granularity(), this.standardKpi(), this.startDate(), this.endDate(), this.area()!).subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'kpi_export.csv';
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
        this.downloadingCsv = false;
      },
      error: error => {
        this.downloadingCsv = false;
        console.log("Error exporting KPI report");
        console.error(error);
        this.alertService.error("Error exporting KPI report!");
      }
    })
  }

  //================ UTILS ================================

  invalidRangeMessage(): string {
    const start = this.startDate();
    const end = this.endDate();

    const startTime = new Date(start).getTime();
    const endTime = new Date(end).getTime();

    if (!start && !end) {
      return 'Enter valid Start Time & End time';
    } else if (!start && end) {
      return 'Enter valid Start Time';
    } else if (start && !end) {
      return 'Enter valid End time';
    } else if (endTime < startTime) {
      return 'Enter valid date range';
    } else return '';
  }
}
