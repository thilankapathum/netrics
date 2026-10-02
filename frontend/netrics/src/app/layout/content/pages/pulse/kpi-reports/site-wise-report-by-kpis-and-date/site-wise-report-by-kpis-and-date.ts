import {Component, computed, CUSTOM_ELEMENTS_SCHEMA, OnDestroy, signal} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {interval, Subscription, switchMap, takeWhile} from 'rxjs';
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
  selector: 'app-site-wise-report-by-kpis-and-date',
  imports: [
    FormsModule
  ],
  templateUrl: './site-wise-report-by-kpis-and-date.html',
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  host: {class: 'block min-w-0'},
})
export class SiteWiseReportByKpisAndDate implements OnDestroy {

  rat = signal('');
  rats: RatDto[] = [];

  selectedKpis = signal<string[]>([]);
  standardKpis = signal<StandardKpiDto[]>([]);
  kpiFilter = signal('');
  filteredKpis = computed(() => {
    const f = this.kpiFilter().toLowerCase();
    return this.standardKpis().filter(k => !f || (k.label ?? k.kpiName ?? '').toLowerCase().includes(f));
  });

  selectedKpiDtos = computed(() => this.selectedKpis()
    .map(name => this.standardKpis().find(k => k.kpiName === name))
    .filter((k): k is StandardKpiDto => !!k));

  areaType = signal<string | undefined>('');
  areaTypes: AreaTypeDto[] = [];

  area = signal<string | undefined>('');
  areas: AreaDto[] = [];

  granularity = signal('');
  granularities: GranularityDto[] = [];

  startDate = signal<string>('');
  endDate = signal<string>('');

  downloadingFile = signal(false);
  loadingRats: boolean = false;
  loadingStandardKpis: boolean = false;
  loadingAreaTypes: boolean = false;
  loadingAreas: boolean = false;
  loadingGranularities: boolean = false;

  invalidRange = computed(() => {
    const start = this.startDate();
    const end = this.endDate();
    if (!start || !end) return true;
    return new Date(end).getTime() < new Date(start).getTime();
  })

  jobId = signal<string | null>(null);
  jobStatus = signal<'IDLE' | 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'>('IDLE');
  jobFileName = signal<string | null>(null);
  jobError = signal<string | null>(null);
  private pollSub?: Subscription;

  readonly today = new Date().toLocaleDateString('en-CA'); // local YYYY-MM-DD

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

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

  loadingAll() {
    return this.loadingRats || this.loadingStandardKpis || this.loadingAreaTypes || this.loadingAreas || this.loadingGranularities;
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
        this.alertService.error('Error retrieving RATs', 'Error', `${err.status} ${err.statusText}`);
        this.loadingRats = false;
      }
    })
  }

  getAllStandardKpiByRat(rat: RatDto) {
    this.loadingStandardKpis = true;
    this.standardKpis.set([]);
    this.selectedKpis.set([]);
    this.standardKpiService.getAllStandardKpi(rat.name!).subscribe({
      next: data => {
        this.standardKpis.set(data);
        if (data.length === 0) {
          this.alertService.error(`Error retrieving Standard KPI for ${rat.label}`);
        }
        this.loadingStandardKpis = false;
      }, error: error => {
        console.error(error);
        this.alertService.error('Standard KPI retrieval failed', 'Error', `${error.status} ${error.statusText}`);
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
        this.alertService.error('Error retrieving Granularities', 'Error', `${err.status} ${err.statusText}`);
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
          this.alertService.error('Error getting Area-types', 'Error', `${error.status}:${error.statusText}`);
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
        this.alertService.error('Error getting Areas', 'Error', `${error.status}:${error.statusText}`);
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
  }

  toggleKpi(kpiName: string, checked: boolean) {
    this.selectedKpis.update(list =>
      checked ? [...list, kpiName] : list.filter(k => k !== kpiName));
  }

  selectAreaType(areaType: string) {
    this.areaType.set(areaType);
    this.getAreasByAreaType(this.areaType()!);
  }

  selectGranularity(granularity: string) {
    this.granularity.set(granularity);
  }

  onDateRangeChange(event: any) {
    // <calendar-range> emits "YYYY-MM-DD/YYYY-MM-DD"
    const [start, end] = (event.target.value as string).split('/');
    this.startDate.set(start ?? '');
    this.endDate.set(end ?? '');
  }

  startReportJob() {
    if (this.blocker()) {
      this.alertService.warning(this.blocker());
      return;
    }

    this.jobStatus.set('PENDING');
    this.jobFileName.set(null);
    this.jobError.set(null);

    this.kpiReportService.submitSiteWiseMultiKpiReportJob(
      this.rat(), this.granularity(), this.selectedKpis(), this.startDate(), this.endDate(), this.area()!
    ).subscribe({
      next: (res) => {
        this.jobId.set(res.jobId);
        this.pollJobStatus(res.jobId);
      },
      error: (error) => {
        this.jobStatus.set('IDLE');
        this.alertService.error('Error starting report job', 'Error', `${error.status} ${error.statusText}`);
      }
    });
  }

  private pollJobStatus(jobId: string) {
    this.pollSub?.unsubscribe();
    this.pollSub = interval(3000).pipe(
      switchMap(() => this.kpiReportService.getJobStatus(jobId)),
      takeWhile(res => res.status === 'PENDING' || res.status === 'PROCESSING', true)
    ).subscribe({
      next: (res) => {
        this.jobStatus.set(res.status as any);
        if (res.status === 'COMPLETED') {
          this.jobFileName.set(res.fileName);
        } else if (res.status === 'FAILED') {
          this.jobError.set(res.errorMessage);
        }
      },
      error: (error) => {
        this.jobStatus.set('IDLE');
        this.alertService.error('Error checking report status', 'Error', `${error.status} ${error.statusText}`);
      }
    });
  }

  downloadReadyFile() {
    const id = this.jobId();
    if (!id || this.downloadingFile()) return;

    this.downloadingFile.set(true);
    this.kpiReportService.downloadJobResult(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = this.jobFileName() ?? 'multi_kpi_export.csv';
        a.click();
        window.URL.revokeObjectURL(url);
        this.downloadingFile.set(false);
        this.jobStatus.set('IDLE');
        this.jobId.set(null);
      },
      error: (error) => {
        this.downloadingFile.set(false);
        this.alertService.error('Error downloading report', 'Error', `${error.status} ${error.statusText}`);
      }
    });
  }

  //================ UTILS ================================

  /** Why the report can't be generated yet; '' when it can. */
  blocker = computed(() => {
    if (this.invalidRange()) return 'Select a date range to generate the report.';
    if (!this.selectedKpis().length) return 'Select at least one KPI to generate the report.';
    return '';
  });

  statusText = computed(() => {
    switch (this.jobStatus()) {
      case 'PENDING': return 'Queued. The report starts when a worker is free.';
      case 'PROCESSING': return 'Generating report. You can leave this page open and keep working.';
      case 'COMPLETED': return `Ready: ${this.jobFileName()}`;
      case 'FAILED': return `Report failed: ${this.jobError() ?? 'unknown error'}. Check the filters and try again.`;
      default: return this.blocker();
    }
  });
}
