import {Component, computed, ElementRef, OnInit, signal, ViewChild} from '@angular/core';
import {AlarmFilter, AlarmsDto, AlarmSourceDto, AlarmTypeDto} from '../../../../../models/pulse/alarms/AlarmDto';
import {debounceTime, distinctUntilChanged, Subject} from 'rxjs';
import {AlarmService} from '../../../../../service/pulse/alarms/alarm-service';
import {FormsModule} from '@angular/forms';
import {DatePipe, NgClass} from '@angular/common';
import {RouterLink} from '@angular/router';
import {AreaTypeDto} from '../../../../../models/pulse/AreaTypeDto';
import {AreaDto} from '../../../../../models/pulse/AreaDto';
import {KeycloakProfile} from 'keycloak-js';
import {AuthService} from '../../../../../auth/service/auth-service';
import {UserAreaService} from '../../../../../service/pulse/user-area-service';
import {AreaTypeService} from '../../../../../service/pulse/area-type-service';
import {AlertService} from '../../../../../components/alert/alert.service';
import {AreaService} from '../../../../../service/pulse/area-service';
import {AlarmTypeService} from '../../../../../service/pulse/alarms/alarm-type-service';
import {AlarmSourceService} from '../../../../../service/pulse/alarms/alarm-source-service';

// const SEVERITIES = ['CRITICAL', 'MAJOR', 'MINOR', 'WARNING', 'INDETERMINATE', 'CLEARED'];
// const ACK_STATES = ['ACKNOWLEDGED', 'UNACKNOWLEDGED', 'UNKNOWN'];
// const CLEAR_STATES = ['CLEARED', 'UNCLEARED', 'UNKNOWN'];

@Component({
  selector: 'app-alarms',
  imports: [
    FormsModule,
    NgClass,
    DatePipe,
    RouterLink
  ],
  templateUrl: './alarms.html',
  styleUrl: './alarms.css'
})
export class Alarms implements OnInit {
  alarms = signal<AlarmsDto[]>([]);
  loading = signal(false);
  page = signal(0);
  size = signal(25);
  totalElements = signal(0);
  totalPages = computed(() => Math.max(1, Math.ceil(this.totalElements() / this.size())));

  period = signal('week');
  nodeName = signal('');
  alarmName = signal('');
  severity = signal('');

  alarmType = signal('');
  alarmTypes: AlarmTypeDto[] = [];

  ackState = signal('');
  clearState = signal('');

  alarmSource = signal('');
  alarmSources: AlarmSourceDto[] = [];

  // areaId = signal<number | null>(null);
  area = signal<string | undefined>('')
  areas: AreaDto[] = [];

  areaType = signal<string | undefined>('');
  areaTypes: AreaTypeDto[] = [];


  userArea = signal<AreaDto | undefined>(undefined);
  userProfile: KeycloakProfile = {};

  loadingAreaTypes: boolean = false;
  loadingAreas: boolean = false;
  loadingAlarmTypes: boolean = false;
  loadingAlarmSources: boolean = false;

  // readonly severities = SEVERITIES;
  // readonly ackStates = ACK_STATES;
  // readonly clearStates = CLEAR_STATES;
  readonly severities: string[] = [];
  readonly ackStates: string[] = [];
  readonly clearStates: string[] = [];

  private nodeNameInput$ = new Subject<string>();
  private alarmNameInput$ = new Subject<string>();

  hoveredAlarm = signal<AlarmsDto | null>(null);
  popoverStyle = signal<{ top: string; left: string }>({top: '0px', left: '0px'});
  private hidePopoverTimeout: ReturnType<typeof setTimeout> | undefined;

  @ViewChild('infoPopover') infoPopoverRef!: ElementRef<HTMLElement>;

  constructor(private alarmService: AlarmService,
              private authService: AuthService,
              private userAreaService: UserAreaService,
              private areaTypeService: AreaTypeService,
              private alertService: AlertService,
              private areaService: AreaService,
              private alarmTypeService: AlarmTypeService,
              private alarmSourceService: AlarmSourceService,) {
    this.severities = alarmService.SEVERITIES;
    this.ackStates = alarmService.ACK_STATES;
    this.clearStates = alarmService.CLEAR_STATES;

    this.nodeNameInput$.pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(value => {
        this.nodeName.set(value);
        this.page.set(0);
        this.load();
      });

    this.alarmNameInput$.pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(value => {
        this.alarmName.set(value);
        this.page.set(0);
        this.load();
      });
  }

  ngOnInit(): void {
    this.getUserProfile();
    this.getAlarmTypes();
    this.getAlarmSources();
    this.load();
  }

  loadingAll() {
    return this.loadingAreaTypes || this.loadingAreas || this.loadingAlarmTypes || this.loadingAlarmSources || this.loading();
  }

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
        this.onFilterChange();
        this.loadingAreas = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Areas! (${error.status}:${error.statusText})`);
        this.loadingAreas = false;
      }
    })
  }

  getAlarmTypes() {
    this.loadingAlarmTypes = true;
    this.alarmTypeService.getAll().subscribe({
      next: data => {
        this.alarmTypes = data;
        this.loadingAlarmTypes = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Alarms (${error.status}:${error.statusText})`);
        this.loadingAlarmTypes = false;
      }
    })
  }

  getAlarmSources() {
    this.loadingAlarmSources = true;
    this.alarmSourceService.getAll().subscribe({
      next: data => {
        this.alarmSources = data;
        this.loadingAlarmSources = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Alarm Sources (${error.status}:${error.statusText})`);
        this.loadingAlarmSources = false;
      }
    })
  }

  onNodeNameInput(value: string) {
    this.nodeNameInput$.next(value);
  }

  onAlarmNameInput(value: string) {
    this.alarmNameInput$.next(value);
  }

  onFilterChange() {
    this.page.set(0);
    this.load();
  }

  load() {
    this.loading.set(true);
    const filter: AlarmFilter = {
      period: this.period(),
      nodeName: this.nodeName() || undefined,
      severity: this.severity() || undefined,
      alarmType: this.alarmType() || undefined,
      alarmName: this.alarmName() || undefined,
      ackState: this.ackState() || undefined,
      clearState: this.clearState() || undefined,
      alarmSource: this.alarmSource() || undefined,
      areaName: this.area() ?? undefined,
    };

    this.alarmService.getAlarms(filter, this.page(), this.size()).subscribe({
      next: (res) => {
        this.alarms.set(res.content);
        this.totalElements.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  goToPage(p: number) {
    if (p < 0 || p >= this.totalPages()) return;
    this.page.set(p);
    this.load();
  }

  clearFilters() {
    this.nodeName.set('');
    this.alarmName.set('');
    this.severity.set('');
    this.alarmType.set('');
    this.ackState.set('');
    this.clearState.set('');
    this.alarmSource.set('');
    // this.area.set(null);
    this.page.set(0);
    this.load();
  }

  severityBadgeClass(sev: string): string {
    switch (sev) {
      case 'CRITICAL':
        return 'badge-error badge-xs';
      case 'MAJOR':
        return 'badge-warning badge-xs';
      case 'MINOR':
        return 'badge-info badge-xs';
      case 'WARNING':
        return 'badge-secondary badge-xs';
      default:
        return 'badge-ghost badge-xs';
    }
  }

  //----- POPOVER -----
  showInfoPopover(event: MouseEvent, alarm: AlarmsDto) {
    clearTimeout(this.hidePopoverTimeout);
    this.hoveredAlarm.set(alarm);

    const target = event.currentTarget as HTMLElement;
    const rect = target.getBoundingClientRect();
    const popoverWidth = 320;
    const popoverEstHeight = 180;

    // Prefer opening to the left of the icon; flip to the right if there's no room.
    let left = rect.left - popoverWidth - 8;
    if (left < 8) {
      left = rect.right + 8;
    }
    // Clamp vertically so it doesn't run off the bottom of the viewport.
    let top = rect.top - 8;
    top = Math.min(top, window.innerHeight - popoverEstHeight - 8);
    top = Math.max(top, 8);

    this.popoverStyle.set({top: `${top}px`, left: `${left}px`});

    const popoverEl = this.infoPopoverRef.nativeElement as any;
    if (!popoverEl.matches(':popover-open')) {
      popoverEl.showPopover();
    }
  }

  scheduleHideInfoPopover() {
    this.hidePopoverTimeout = setTimeout(() => this.hideInfoPopover(), 150);
  }

  cancelHideInfoPopover() {
    clearTimeout(this.hidePopoverTimeout);
  }

  hideInfoPopover() {
    const popoverEl = this.infoPopoverRef?.nativeElement as any;
    if (popoverEl?.matches(':popover-open')) {
      popoverEl.hidePopover();
    }
    this.hoveredAlarm.set(null);
  }

}
