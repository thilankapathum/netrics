import {Component, computed, OnDestroy, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {of, Subject} from 'rxjs';
import {catchError, debounceTime, map, switchMap, takeUntil} from 'rxjs/operators';
import {SiteService} from '../../../../../../service/pulse/site-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {SiteDto} from '../../../../../../models/pulse/SiteDto';
import {EpExport} from '../ep-export/ep-export';
import {EngineeringParameterService} from '../../../../../../service/beam/engineering-parameter-service';
import {UserService} from '../../../../../../service/pulse/user-service';
import {EngineeringOverviewDto, GapDto, NameCount} from '../../../../../../models/beam/EngineeringOverviewDto';
import {OperationLogDto} from '../../../../../../models/beam/OperationLogDto';
import {AuthService} from '../../../../../../auth/service/auth-service';
import {AreaService} from '../../../../../../service/pulse/area-service';
import {AreaTypeService} from '../../../../../../service/pulse/area-type-service';
import {UserAreaService} from '../../../../../../service/pulse/user-area-service';
import {AreaDto} from '../../../../../../models/pulse/AreaDto';
import {AreaTypeDto} from '../../../../../../models/pulse/AreaTypeDto';
import {absoluteTime, RECENT_MS, recentSites, relativeTime, SYSTEM_USERS, userNameMap} from '../change-format';

type LoadState = 'loading' | 'ready' | 'failed';

interface ChainLevel {
  count: number;
  label: string;
  /** Parents with no children at the next level; absent on the last level */
  gap?: GapDto;
  child?: string;
}

interface Breakdown {
  title: string;
  of: string;
  rows: (NameCount & { pct: number })[];
}

interface ChangeRow {
  id: number;
  by: string;
  verb: string;
  target: string;
  siteCode: string | null;
  diffs: { field: string; from: string; to: string }[];
  more: number;
  relative: string;
  absolute: string;
  recent: boolean;
}

const CHANGES_PAGE = 15;
/** Used when the user has no mapped area, as on the Worst cells page */
const DEFAULT_AREA_TYPE = 'District';
const BREAKDOWN_TOP = 5;
const DIFFS_SHOWN = 2;
const AUDIT_FIELDS = ['id', 'createdAt', 'createdBy', 'lastModifiedAt', 'lastModifiedBy'];
/** Foreign keys hold ids, so only the fact that they changed is shown */
const REFERENCE_FIELDS = ['site', 'sector', 'antenna', 'antennaType', 'manufacturer', 'band', 'operator', 'infraType'];
const DEGREE_FIELDS = ['azimuth', 'mechanicalTilt', 'electricalTilt'];
const VERBS = {CREATE: 'added', UPDATE: 'edited', DELETE: 'deleted'};

@Component({
  selector: 'app-site-search',
  imports: [CommonModule, FormsModule, RouterLink, EpExport],
  templateUrl: './site-search.html',
  styleUrl: './site-search.css'
})
export class SiteSearch implements OnInit, OnDestroy {
  query = signal('');
  results = signal<SiteDto[]>([]);
  activeIndex = signal(-1);
  searching = signal(false);
  searched = signal(false);

  recent = signal<string[]>(recentSites());

  areaTypes = signal<AreaTypeDto[]>([]);
  areaType = signal('');
  areas = signal<AreaDto[]>([]);
  area = signal('');
  loadingAreas = signal(true);

  overview = signal<EngineeringOverviewDto | null>(null);
  overviewState = signal<LoadState>('loading');
  overviewError = signal('Couldn’t load the inventory.');
  /** A new area is loading; the previous numbers stay up, dimmed */
  reloading = signal(false);
  openGap = signal<string | null>(null);

  private logs = signal<OperationLogDto[]>([]);
  private userNames = signal(new Map<string, string>());
  changesState = signal<LoadState>('loading');
  loadingMore = signal(false);
  hasMore = signal(false);
  private changesPage = 0;

  chain = computed<ChainLevel[]>(() => {
    const o = this.overview();
    if (!o) return [];
    const gap = (key: GapDto['key']) => o.gaps.find(g => g.key === key);
    return [
      {count: o.counts.sites, label: 'sites', gap: gap('sitesWithoutSectors'), child: 'sectors'},
      {count: o.counts.sectors, label: 'sectors', gap: gap('sectorsWithoutAntennas'), child: 'antennas'},
      {count: o.counts.antennas, label: 'antennas', gap: gap('antennasWithoutTilts'), child: 'tilts'},
      {count: o.counts.tilts, label: 'electrical tilts'}
    ];
  });

  breakdowns = computed<Breakdown[]>(() => {
    const b = this.overview()?.breakdowns;
    if (!b) return [];
    return [
      {title: 'Operator', of: 'sites', rows: this.top(b.operator)},
      {title: 'Infra type', of: 'sites', rows: this.top(b.infraType)},
      {title: 'Band', of: 'tilts', rows: this.top(b.band)},
      {title: 'Manufacturer', of: 'antennas', rows: this.top(b.manufacturer)}
    ];
  });

  changeRows = computed(() => this.logs().map(log => this.toRow(log, this.userNames())));

  private readonly destroy$ = new Subject<void>();
  private readonly searchTerms$ = new Subject<string>();
  private readonly area$ = new Subject<string>();

  constructor(private siteService: SiteService,
              private alertService: AlertService,
              private engineeringParameterService: EngineeringParameterService,
              private userService: UserService,
              private authService: AuthService,
              private areaService: AreaService,
              private areaTypeService: AreaTypeService,
              private userAreaService: UserAreaService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.searchTerms$.pipe(
      debounceTime(300),
      takeUntil(this.destroy$)
    ).subscribe(term => this.performSearch(term));

    // switchMap drops the reply for an area the user has already moved away from
    this.area$.pipe(
      switchMap(area => this.engineeringParameterService.overview(area).pipe(catchError(() => of(null)))),
      takeUntil(this.destroy$)
    ).subscribe(o => {
      this.overview.set(o);
      this.overviewState.set(o ? 'ready' : 'failed');
      this.overviewError.set('Couldn’t load the inventory.');
      this.reloading.set(false);
    });
    this.loadUserArea();
    this.loadChanges();
    // names come from the realm user list in pulse; without PULSE_READ the feed says "Someone"
    this.userService.getAllUsers().pipe(catchError(() => of([])), takeUntil(this.destroy$))
      .subscribe(users => this.userNames.set(userNameMap(users)));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearch(value: string): void {
    this.query.set(value);
    this.activeIndex.set(-1);
    if (value.length > 1) {
      this.searching.set(true);
      this.searchTerms$.next(value);
    } else {
      this.results.set([]);
      this.searched.set(false);
    }
  }

  onKeydown(event: KeyboardEvent): void {
    const count = this.results().length;
    if (event.key === 'ArrowDown' && count) {
      event.preventDefault();
      this.activeIndex.update(i => (i + 1) % count);
    } else if (event.key === 'ArrowUp' && count) {
      event.preventDefault();
      this.activeIndex.update(i => (i <= 0 ? count : i) - 1);
    } else if (event.key === 'Enter' && count) {
      this.selectSite(this.results()[Math.max(0, this.activeIndex())]);
      return;
    }
    document.getElementById(`site-opt-${this.activeIndex()}`)?.scrollIntoView({block: 'nearest'});
  }

  private performSearch(term: string): void {
    this.siteService.searchSites(term).pipe(takeUntil(this.destroy$)).subscribe({
      next: data => {
        // drop responses for a term the user has already typed past
        if (term !== this.query()) return;
        this.results.set(data);
        this.searching.set(false);
        this.searched.set(true);
      },
      error: err => {
        this.searching.set(false);
        this.alertService.httpError('Site search failed', err);
      }
    });
  }

  selectSite(site: SiteDto): void {
    this.router.navigate(['/beam/engineering-parameters', site.siteCode]);
  }

  /** The user's mapped area first, as on the Worst cells page; otherwise the first District */
  private loadUserArea(): void {
    this.authService.getUserProfile$().pipe(
      switchMap(profile => this.userAreaService.findByUserId(profile.id!).pipe(catchError(() => of(undefined)))),
      switchMap(userArea => this.areaTypeService.getAllAreaTypes().pipe(map(types => ({userArea, types})))),
      takeUntil(this.destroy$)
    ).subscribe({
      next: ({userArea, types}) => {
        this.areaTypes.set(types);
        const type = types.find(t => t.name === userArea?.areaTypeName)?.name
          ?? types.find(t => t.name === DEFAULT_AREA_TYPE)?.name ?? types[0]?.name ?? '';
        this.loadAreas(type, userArea?.name);
      },
      error: () => this.areasFailed()
    });
  }

  selectAreaType(type: string): void {
    this.loadAreas(type);
  }

  private loadAreas(type: string, preferred?: string): void {
    this.areaType.set(type);
    this.loadingAreas.set(true);
    this.areaService.getAreasByAreaTypes(type).pipe(takeUntil(this.destroy$)).subscribe({
      next: areas => {
        this.areas.set(areas);
        this.loadingAreas.set(false);
        const area = areas.find(a => a.name === preferred)?.name ?? areas[0]?.name;
        if (area) {
          this.selectArea(area);
        } else {
          this.area.set('');
          this.overview.set(null);
          this.overviewState.set('failed');
          this.overviewError.set(`There are no ${type} areas yet.`);
        }
      },
      error: () => this.areasFailed()
    });
  }

  selectArea(area: string): void {
    this.area.set(area);
    this.openGap.set(null);
    if (this.overview()) this.reloading.set(true);
    else this.overviewState.set('loading');
    this.area$.next(area);
  }

  private areasFailed(): void {
    this.loadingAreas.set(false);
    this.overviewState.set('failed');
    this.overviewError.set('Couldn’t load areas.');
  }

  /** Opens a gap's site list from the inventory chain */
  showGap(key: string): void {
    this.openGap.set(key);
    setTimeout(() => document.getElementById(`gap-${key}`)?.scrollIntoView({block: 'nearest', behavior: 'smooth'}));
  }

  onGapToggle(key: string, event: Event): void {
    const open = (event.target as HTMLDetailsElement).open;
    if (open) this.openGap.set(key);
    else if (this.openGap() === key) this.openGap.set(null);
  }

  loadChanges(): void {
    const page = this.changesPage;
    this.loadingMore.set(page > 0);
    this.engineeringParameterService.changes(page, CHANGES_PAGE).pipe(takeUntil(this.destroy$)).subscribe({
      next: logs => {
        this.logs.update(l => [...l, ...logs]);
        this.hasMore.set(logs.length === CHANGES_PAGE);
        this.changesPage++;
        this.changesState.set('ready');
        this.loadingMore.set(false);
      },
      error: err => {
        this.loadingMore.set(false);
        if (page === 0) this.changesState.set('failed');
        else this.alertService.httpError('Couldn’t load more changes', err);
      }
    });
  }

  /** Top rows plus the rest folded into "Other"; pct is the share of the whole column */
  private top(rows: NameCount[]): Breakdown['rows'] {
    const total = rows.reduce((n, r) => n + r.count, 0) || 1;
    const shown = rows.length > BREAKDOWN_TOP + 1 ? rows.slice(0, BREAKDOWN_TOP) : rows;
    const rest = rows.slice(shown.length).reduce((n, r) => n + r.count, 0);
    return [...shown, ...(rest ? [{name: 'Other', count: rest}] : [])]
      .map(r => ({...r, pct: Math.round(r.count / total * 100)}));
  }

  private toRow(log: OperationLogDto, names: Map<string, string>): ChangeRow {
    const date = new Date(log.performedAt);
    const diffs = log.operation === 'UPDATE'
      ? Object.entries(log.changes)
        .filter(([field]) => !AUDIT_FIELDS.includes(field))
        .map(([field, d]) => ({
          field: this.words(field),
          from: REFERENCE_FIELDS.includes(field) ? '' : this.value(field, d.old),
          to: REFERENCE_FIELDS.includes(field) ? 'changed' : this.value(field, d.new)
        }))
      : [];
    return {
      id: log.id,
      by: SYSTEM_USERS.includes(log.performedBy) ? 'System' : names.get(log.performedBy) ?? 'Someone',
      verb: VERBS[log.operation] ?? log.operation.toLowerCase(),
      target: this.target(log),
      siteCode: log.operation === 'DELETE' && log.entityName === 'Site' ? null : log.siteCode,
      diffs: diffs.slice(0, DIFFS_SHOWN),
      more: Math.max(0, diffs.length - DIFFS_SHOWN),
      relative: relativeTime(date),
      absolute: absoluteTime(date),
      recent: Date.now() - date.getTime() < RECENT_MS
    };
  }

  /** What changed, from the live row or, once deleted, the logged snapshot */
  private target(log: OperationLogDto): string {
    const logged = (field: string) => log.changes[field]?.new ?? log.changes[field]?.old ?? null;
    const sector = log.sectorName ?? logged('name');
    const antenna = log.antennaIndex ?? logged('antennaIndex');
    switch (log.entityName) {
      case 'Site':
        // a live site is named by the link that follows
        return log.siteCode && log.operation !== 'DELETE' ? 'site' : `site ${logged('siteCode') ?? ''}`.trim();
      case 'Sector':
        return `sector ${sector ?? ''}`.trim();
      case 'Antenna':
        return antenna != null ? `antenna ${antenna}${log.sectorName ? ' on ' + log.sectorName : ''}` : 'an antenna';
      case 'ElectricalTilt':
        return antenna != null ? `a tilt on antenna ${antenna}${log.sectorName ? ', ' + log.sectorName : ''}` : 'an electrical tilt';
      default:
        return log.entityName;
    }
  }

  private value(field: string, v?: string | null): string {
    if (v == null || v === '') return 'not set';
    return DEGREE_FIELDS.includes(field) ? `${v}°` : v;
  }

  /** mechanicalTilt -> mechanical tilt */
  private words(field: string): string {
    return field.replace(/([A-Z])/g, ' $1').toLowerCase();
  }
}
