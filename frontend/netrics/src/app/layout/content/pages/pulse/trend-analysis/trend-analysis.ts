import {Component, computed, DestroyRef, ElementRef, inject, OnInit, signal, ViewChild} from '@angular/core';
import {takeUntilDestroyed, toObservable} from '@angular/core/rxjs-interop';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {catchError, finalize, forkJoin, map, Observable, of, switchMap, tap} from 'rxjs';
import {Linechart} from '../../../../../components/charts/linechart/linechart/linechart';
import {AlertService} from '../../../../../components/alert/alert.service';
import {AuthService} from '../../../../../auth/service/auth-service';
import {KpidayService} from '../../../../../service/pulse/ltefdd/kpiday.service';
import {StandardkpiService} from '../../../../../service/pulse/ltefdd/standardkpi.service';
import {RatService} from '../../../../../service/pulse/rat-service';
import {AreaTypeService} from '../../../../../service/pulse/area-type-service';
import {AreaService} from '../../../../../service/pulse/area-service';
import {UserAreaService} from '../../../../../service/pulse/user-area-service';
import {BandService} from '../../../../../service/pulse/band-service';
import {SharedService} from '../../../../../service/pulse/shared-service';
import {RatDto} from '../../../../../models/pulse/RatDto';
import {StandardKpiDto} from '../../../../../models/pulse/StandardKpiDto';
import {AreaTypeDto} from '../../../../../models/pulse/AreaTypeDto';
import {AreaDto} from '../../../../../models/pulse/AreaDto';
import {BandDto} from '../../../../../models/pulse/BandDto';
import {KpiTrendDto} from '../../../../../models/pulse/KpiTrendDto';

/** One trend line. Carries its own RAT so multi-RAT chart types only need a new spec builder. */
export interface TrendSeriesSpec {
  ratName: string;
  kpiName: string;
  unit: string;
  areaName: string;
  bandName?: string;
  name: string;   // legend
  color: string;  // fixed per item so colours don't shift as selection changes
}

type ChartType = 'm_area' | 'm_kpi' | 'm_band';
type Period = 'month' | 'quarter' | 'year';

const PALETTE = [
  '#008FFB', '#00E396', '#FEB019', '#FF4560', '#775DD0',
  '#4caf50', '#546E7A', '#8D5B4C', '#C5D86D', '#2b908f',
  '#c200ff', '#66ca5b', '#ff00bf', '#ff8a47', '#e9006b',
  '#4ab5e7', '#9c2b08', '#caff00', '#00ff0c', '#ffdd00'
];

@Component({
  selector: 'app-trend-analysis',
  imports: [FormsModule, RouterLink, Linechart],
  templateUrl: './trend-analysis.html'
})
export class TrendAnalysis implements OnInit {
  private kpidayService = inject(KpidayService);
  private standardKpiService = inject(StandardkpiService);
  private ratService = inject(RatService);
  private areaTypeService = inject(AreaTypeService);
  private areaService = inject(AreaService);
  private userAreaService = inject(UserAreaService);
  private bandService = inject(BandService);
  private authService = inject(AuthService);
  private sharedService = inject(SharedService);
  private alertService = inject(AlertService);
  private destroyRef = inject(DestroyRef);

  readonly chartTypes: { value: ChartType; label: string }[] = [
    {value: 'm_area', label: 'Multi-Area'},
    {value: 'm_kpi', label: 'Multi-KPI'},
    {value: 'm_band', label: 'Multi-Band'}
  ];
  readonly periods: Period[] = ['month', 'quarter', 'year'];

  // ── Filters ───────────────────────────────────────────────────────────────
  chartType = signal<ChartType>('m_area');
  rats = signal<RatDto[]>([]);
  rat = signal<string>(this.sharedService.selectedRat());
  granularity = signal<string>('day-average');
  period = signal<Period>('month');

  standardKpis = signal<StandardKpiDto[]>([]);
  kpiName = signal<string>('');                        // single-KPI modes
  kpiNames = signal<Set<string>>(new Set());           // multi-KPI mode

  areaTypes = signal<AreaTypeDto[]>([]);
  areaType = signal<string>('');
  areas = signal<AreaDto[]>([]);
  areaName = signal<string>('');

  bands = signal<BandDto[]>([]);

  /** Only ticked items are fetched; each list starts with its first item ticked. */
  selectedAreas = signal<Set<string>>(new Set());
  selectedBands = signal<Set<string>>(new Set());

  /** Loaded trends keyed by request, so ticking another item only fetches that one. */
  // ponytail: unbounded for the page's lifetime; fine for a few hundred series, add LRU if it grows.
  private cache = new Map<string, KpiTrendDto[]>();

  loading = signal(false);
  /** In-flight lookup requests (RATs, KPIs, bands, area types, areas). */
  private pending = signal(0);
  /** Filters stay disabled until lookups and the trend fetch have settled. */
  busy = computed(() => this.loading() || this.pending() > 0);

  /** Chart fills its card; height follows the card so the page never scrolls vertically. */
  @ViewChild('chartCard', {static: true}) chartCard!: ElementRef<HTMLElement>;
  chartHeight = signal(0);
  private results = signal<{ spec: TrendSeriesSpec; data: KpiTrendDto[] }[]>([]);

  // ── Derived ───────────────────────────────────────────────────────────────
  private kpiByName = computed(() => new Map(this.standardKpis().map(k => [k.kpiName!, k])));

  /** Units of the KPIs selected in multi-KPI mode (max 2: left + right axis). */
  selectedUnits = computed(() =>
    new Set([...this.kpiNames()].map(n => this.kpiByName().get(n)?.unit ?? '')));

  unitsText = computed(() => [...this.selectedUnits()].map(u => u || 'no unit').join(' + ') || 'none');

  specs = computed<TrendSeriesSpec[]>(() => {
    const rat = this.rat();
    const spec = (k: StandardKpiDto, areaName: string, name: string, i: number, bandName?: string): TrendSeriesSpec =>
      ({ratName: rat, kpiName: k.kpiName!, unit: k.unit ?? '', areaName, bandName, name, color: PALETTE[i % PALETTE.length]});
    const kpi = this.kpiByName().get(this.kpiName());
    const area = this.areaName();

    switch (this.chartType()) {
      case 'm_area':
        return !kpi ? [] : this.areas().flatMap((a, i) =>
          this.selectedAreas().has(a.name!) ? [spec(kpi, a.name!, a.name!, i)] : []);
      case 'm_kpi':
        return !area ? [] : this.standardKpis().flatMap((k, i) =>
          this.kpiNames().has(k.kpiName!) ? [spec(k, area, k.label!, i)] : []);
      case 'm_band':
        return !kpi || !area ? [] : this.bands().flatMap((b, i) =>
          this.selectedBands().has(b.name!) ? [spec(kpi, area, this.bandLabel(b), i, b.name!)] : []);
    }
  });

  /** Tick list for Multi-Area / Multi-Band. */
  toggleItems = computed(() => {
    switch (this.chartType()) {
      case 'm_area': return this.areas().map(a => ({key: a.name!, name: a.name!}));
      case 'm_band': return this.bands().map(b => ({key: b.name!, name: this.bandLabel(b)}));
      default: return [];
    }
  });
  private selection = computed(() => this.chartType() === 'm_band' ? this.selectedBands : this.selectedAreas);

  bandLabel(b: BandDto) { return `Band ${b.name}`; }
  colorAt(i: number) { return PALETTE[i % PALETTE.length]; }

  series = computed(() => this.results()
    .map(r => ({
      name: r.spec.name,
      unit: r.spec.unit,
      color: r.spec.color,
      data: r.data
        .map(d => ({x: new Date(d.timestamp!).getTime(), y: d.kpiValue == null ? null : Math.round(d.kpiValue * 100) / 100}))
        .sort((a, b) => a.x - b.x)
    }))
    // Empty series must not reach ApexCharts: morphing an empty path on update throws "parser Error" and hangs the renderer.
    .filter(s => s.data.length > 0));

  noData = computed(() => new Set(this.results().filter(r => r.data.length === 0).map(r => r.spec.name)));

  yaxis = computed<ApexYAxis[]>(() => {
    const units = [...new Set(this.series().map(s => s.unit))];
    const labels = {formatter: (v: number) => v == null ? '' : v.toFixed(2)};
    if (units.length <= 1) return [{title: {text: units[0] ?? ''}, labels}];
    return units.map((u, i) => ({
      seriesName: this.series().filter(s => s.unit === u).map(s => s.name),
      opposite: i === 1,
      title: {text: u},
      labels
    }));
  });

  constructor() {
    // Refetch whenever the specs or global filters change; switchMap drops stale responses.
    toObservable(computed(() => ({specs: this.specs(), gran: this.granularity(), period: this.period()})))
      .pipe(
        tap(() => this.loading.set(true)),
        switchMap(({specs, gran, period}) => specs.length
          ? forkJoin(specs.map(s => this.fetch(s, gran, period).pipe(catchError(() => of([] as KpiTrendDto[])))))
            .pipe(map(data => specs.map((spec, i) => ({spec, data: data[i]}))))
          : of([])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(res => {
        this.results.set(res);
        this.loading.set(false);
      });
  }

  private fetch(s: TrendSeriesSpec, gran: string, period: string): Observable<KpiTrendDto[]> {
    const key = [s.ratName, s.kpiName, s.areaName, s.bandName ?? '', gran, period].join('|');
    const hit = this.cache.get(key);
    if (hit) return of(hit);
    return (s.bandName
      ? this.kpidayService.getDataByKpiAreaAndBand(s.kpiName, period, s.areaName, s.ratName, gran, s.bandName)
      : this.kpidayService.getDataByKpiAndArea(s.kpiName, period, s.areaName, s.ratName, gran))
      .pipe(tap(d => this.cache.set(key, d)));
  }

  ngOnInit(): void {
    const ro = new ResizeObserver(([e]) => this.chartHeight.set(Math.floor(e.contentRect.height)));
    ro.observe(this.chartCard.nativeElement);
    this.destroyRef.onDestroy(() => ro.disconnect());

    this.track(this.ratService.getAllRats()).subscribe({
      next: d => this.rats.set(d),
      error: e => this.fail('Retrieving RATs failed', e)
    });
    this.loadRatScoped();
    this.loadAreaTypesAndUserArea();
  }

  // ── Filter handlers ───────────────────────────────────────────────────────
  setChartType(t: ChartType) {
    this.chartType.set(t);
  }

  setRat(rat: string) {
    this.rat.set(rat);
    this.loadRatScoped();
  }

  setAreaType(name: string) {
    this.areaType.set(name);
    this.loadAreas(name);
  }

  toggleKpi(name: string) {
    const next = new Set(this.kpiNames());
    next.has(name) ? next.delete(name) : next.add(name);
    this.kpiNames.set(next);
  }

  /** Multi-KPI: only two distinct units allowed (left + right axis). */
  isKpiDisabled(k: StandardKpiDto): boolean {
    const units = this.selectedUnits();
    return units.size >= 2 && !units.has(k.unit ?? '');
  }

  isSelected(key: string) {
    return this.selection()().has(key);
  }

  toggleSeries(key: string) {
    const next = new Set(this.selection()());
    next.has(key) ? next.delete(key) : next.add(key);
    this.selection().set(next);
  }

  setAllSeries(all: boolean) {
    this.selection().set(new Set(all ? this.toggleItems().map(i => i.key) : []));
  }

  // ── Loaders ───────────────────────────────────────────────────────────────
  /** KPIs and bands depend on RAT; keep selections that still exist. */
  private loadRatScoped() {
    const rat = this.rat();
    this.track(this.standardKpiService.getAllStandardKpi(rat)).subscribe({
      next: kpis => {
        const names = new Set(kpis.map(k => k.kpiName));
        this.standardKpis.set(kpis);
        if (!names.has(this.kpiName())) this.kpiName.set(kpis[0]?.kpiName ?? '');
        this.kpiNames.set(keepOrFirst(this.kpiNames(), kpis.map(k => k.kpiName!)));
      },
      error: e => this.fail('Retrieving KPIs failed', e)
    });
    this.track(this.bandService.getByRatName(rat)).subscribe({
      next: b => {
        this.bands.set(b);
        this.selectedBands.set(keepOrFirst(this.selectedBands(), b.map(x => x.name!)));
      },
      error: e => this.fail('Retrieving bands failed', e)
    });
  }

  private async loadAreaTypesAndUserArea() {
    let userArea: AreaDto | undefined;
    this.pending.update(n => n + 1);
    try {
      const profile = await this.authService.getUserProfile();
      userArea = await new Promise<AreaDto | undefined>(res =>
        this.userAreaService.findByUserId(profile.id!).subscribe({next: res, error: () => res(undefined)}));
    } catch { /* no assigned area: fall back to first area type */ }
    this.pending.update(n => n - 1);

    this.track(this.areaTypeService.getAllAreaTypes()).subscribe({
      next: types => {
        this.areaTypes.set(types);
        const type = userArea?.areaTypeName ?? types[0]?.name ?? '';
        this.areaType.set(type);
        this.loadAreas(type, userArea?.name);
      },
      error: e => this.fail('Retrieving area types failed', e)
    });
  }

  private loadAreas(areaType: string, preferred?: string) {
    if (!areaType) return;
    this.track(this.areaService.getAreasByAreaTypes(areaType)).subscribe({
      next: areas => {
        const enabled = areas.filter(a => a.enabled !== false);
        this.areas.set(enabled);
        this.selectedAreas.set(keepOrFirst(this.selectedAreas(), enabled.map(a => a.name!)));
        const keep = [preferred, this.areaName()].find(n => n && enabled.some(a => a.name === n));
        this.areaName.set(keep ?? enabled[0]?.name ?? '');
      },
      error: e => this.fail('Retrieving areas failed', e)
    });
  }

  private track<T>(o: Observable<T>): Observable<T> {
    this.pending.update(n => n + 1);
    return o.pipe(finalize(() => this.pending.update(n => n - 1)));
  }

  private fail(title: string, e: any) {
    this.alertService.error(title, 'Error', `${e.status} ${e.statusText}`);
  }
}

/** Keep still-valid selections; if none survive, start with just the first item (fast first load). */
function keepOrFirst(current: Set<string>, available: string[]): Set<string> {
  const kept = available.filter(n => current.has(n));
  return new Set(kept.length ? kept : available.slice(0, 1));
}
