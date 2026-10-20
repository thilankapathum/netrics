import {Component, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {catchError, forkJoin, map, Observable, of, switchMap} from 'rxjs';
import {SiteService} from '../../../../../../service/beam/site-service';
import {SectorService} from '../../../../../../service/beam/sector-service';
import {AntennaService} from '../../../../../../service/beam/antenna-service';
import {ElectricalTiltService} from '../../../../../../service/beam/electrical-tilt-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {AuthService} from '../../../../../../auth/service/auth-service';
import {SiteDto} from '../../../../../../models/beam/SiteDto';
import {SectorDto} from '../../../../../../models/beam/SectorDto';
import {AntennaDto} from '../../../../../../models/beam/AntennaDto';
import {ElectricalTiltDto} from '../../../../../../models/beam/ElectricalTiltDto';
import {AuditDto} from '../../../../../../models/beam/AuditDto';
import {UserService} from '../../../../../../service/pulse/user-service';
import {antennaToNode, NodeType, sectorToNode, tiltToNode, TreeNodeVm} from '../tree-node.model';
import {NodeEditModal} from '../node-edit-modal/node-edit-modal';

interface AntennaVm {
  antenna: AntennaDto;
  tilts: ElectricalTiltDto[];
}

interface SectorVm {
  sector: SectorDto;
  antennas: AntennaVm[];
  /** antennas/tilts failed to load; the rest of the site still renders */
  failed?: boolean;
}

export interface ChangeVm {
  verb: 'Edited' | 'Added';
  relative: string;
  absolute: string;
  /** "First L.", or null when the user can't be resolved */
  by: string | null;
  recent: boolean;
}

interface CascadeItem {
  antenna: AntennaDto;
  to: number;
  checked: boolean;
}

interface Cascade {
  sectorName: string;
  from: number | null;
  to: number;
  items: CascadeItem[];
}

/** Nominal half-power beamwidth drawn on the azimuth rose */
const BEAMWIDTH = 65;
const ROSE_RADIUS = 42;
/** Distinct sector colours (see site-tree.css); colour follows the sector index so sector 1 always looks the same */
const SECTOR_COLOURS = 6;
const RECENT_MS = 24 * 3600 * 1000;
const SYSTEM_USERS = ['SYSTEM', 'ANONYMOUS'];
const relativeFormat = new Intl.RelativeTimeFormat('en', {numeric: 'auto'});

@Component({
  selector: 'app-site-tree',
  imports: [CommonModule, FormsModule, RouterLink, NodeEditModal],
  templateUrl: './site-tree.html',
  styleUrl: './site-tree.css'
})
export class SiteTree implements OnInit {
  siteCode = signal<string>('');
  site = signal<SiteDto | undefined>(undefined);
  sectors = signal<SectorVm[]>([]);
  loading = signal(false);
  highlighted = signal<string | null>(null);

  modalOpen = signal(false);
  modalMode: 'create' | 'edit' = 'edit';
  modalNodeType: NodeType = 'site';
  editNode?: TreeNodeVm;
  parentNode?: TreeNodeVm;

  cascade = signal<Cascade | null>(null);
  cascading = signal(false);

  /** Keycloak sub -> "First L." */
  private userNames = new Map<string, string>();

  readonly roseRadius = ROSE_RADIUS;

  constructor(private route: ActivatedRoute,
              private siteService: SiteService,
              private sectorService: SectorService,
              private antennaService: AntennaService,
              private electricalTiltService: ElectricalTiltService,
              private alertService: AlertService,
              private authService: AuthService,
              private userService: UserService) {
  }

  ngOnInit(): void {
    const siteCode = this.route.snapshot.paramMap.get('siteCode') ?? '';
    this.siteCode.set(siteCode);
    this.loading.set(true);
    this.load();
    this.loadUserNames();
  }

  // names come from the realm user list in pulse; without PULSE_READ we just show the time
  private loadUserNames(): void {
    this.userService.getAllUsers().pipe(catchError(() => of([]))).subscribe(users => {
      this.userNames = new Map(users.map(u => [u.userId,
        [u.firstName, u.lastName?.charAt(0) ? u.lastName.charAt(0) + '.' : ''].filter(Boolean).join(' ') || u.username]));
      // re-render with names
      this.sectors.update(s => [...s]);
      this.site.update(s => s && {...s});
    });
  }

  canAdd(): boolean {
    return this.authService.hasAnyRole(['BEAM_CREATE']);
  }

  canEdit(): boolean {
    return this.authService.hasAnyRole(['BEAM_UPDATE']);
  }

  canDelete(): boolean {
    return this.authService.hasAnyRole(['BEAM_DELETE']);
  }

  // ponytail: one request per sector and per antenna; add a site-level aggregate endpoint if sites grow large
  private load(): void {
    const siteCode = this.siteCode();
    forkJoin({
      site: this.siteService.getBySiteCode(siteCode),
      sectors: this.sectorService.getBySiteCode(siteCode).pipe(
        switchMap(sectors => this.all(sectors.map(sector => this.loadSector(sector))))
      )
    }).subscribe({
      next: ({site, sectors}) => {
        this.site.set(site);
        this.sectors.set(sectors.sort((a, b) => (a.sector.sectorIndex ?? 0) - (b.sector.sectorIndex ?? 0)));
        this.loading.set(false);
      },
      error: err => {
        this.loading.set(false);
        this.alertService.httpError(`Failed to load site ${siteCode}`, err);
      }
    });
  }

  private loadSector(sector: SectorDto): Observable<SectorVm> {
    return this.antennaService.getBySectorName(sector.name).pipe(
      switchMap(antennas => this.all(antennas
        .sort((a, b) => (a.antennaIndex ?? 0) - (b.antennaIndex ?? 0))
        .map(antenna => this.electricalTiltService.getByAntennaId(antenna.id!).pipe(
          map(tilts => ({antenna, tilts}))
        )))),
      map((antennas): SectorVm => ({sector, antennas})),
      catchError(err => {
        this.alertService.httpError(`Failed to load antennas for ${sector.name}`, err);
        return of({sector, antennas: [], failed: true});
      })
    );
  }

  /** forkJoin that emits [] for an empty list instead of completing silently */
  private all<T>(requests: Observable<T>[]): Observable<T[]> {
    return requests.length ? forkJoin(requests) : of([]);
  }

  sectorSlot(sector: SectorDto): number {
    return ((sector.sectorIndex ?? 1) - 1) % SECTOR_COLOURS + 1;
  }

  /** Last change to an item; imported rows nobody has touched since return null to keep the sheet quiet */
  change(audit?: AuditDto): ChangeVm | null {
    const edited = !!audit?.lastModifiedAt;
    const at = edited ? audit!.lastModifiedAt : audit?.createdAt;
    const user = edited ? audit!.lastModifiedBy : audit?.createdBy;
    if (!at || (!edited && SYSTEM_USERS.includes(user ?? ''))) return null;

    const date = new Date(at);
    return {
      verb: edited ? 'Edited' : 'Added',
      relative: this.relative(date),
      absolute: date.toLocaleString(undefined, {dateStyle: 'medium', timeStyle: 'short'}),
      by: SYSTEM_USERS.includes(user ?? '') ? 'System' : this.userNames.get(user ?? '') ?? null,
      recent: Date.now() - date.getTime() < RECENT_MS
    };
  }

  /** Plain-text form of change() for tooltips and labels */
  changeText(audit?: AuditDto): string {
    const c = this.change(audit);
    return c ? `${c.verb} ${c.relative}${c.by ? ' by ' + c.by : ''} (${c.absolute})` : '';
  }

  private relative(date: Date): string {
    const ago = (Date.now() - date.getTime()) / 1000;
    if (ago < 60) return 'just now';
    if (ago < 3600) return relativeFormat.format(-Math.round(ago / 60), 'minute');
    if (ago < 86400) return relativeFormat.format(-Math.round(ago / 3600), 'hour');
    if (ago < 86400 * 30) return relativeFormat.format(-Math.round(ago / 86400), 'day');
    return 'on ' + date.toLocaleDateString(undefined, {dateStyle: 'medium'});
  }

  antennaCount(): number {
    return this.sectors().reduce((n, s) => n + s.antennas.length, 0);
  }

  /** SVG path of a beam wedge centred on the azimuth (degrees clockwise from north) */
  wedge(azimuth: number, radius = ROSE_RADIUS): string {
    const [x1, y1] = this.polar(azimuth - BEAMWIDTH / 2, radius);
    const [x2, y2] = this.polar(azimuth + BEAMWIDTH / 2, radius);
    return `M0 0L${x1} ${y1}A${radius} ${radius} 0 0 1 ${x2} ${y2}Z`;
  }

  polar(azimuth: number, radius: number): [number, number] {
    const rad = azimuth * Math.PI / 180;
    return [+(radius * Math.sin(rad)).toFixed(2), +(-radius * Math.cos(rad)).toFixed(2)];
  }

  // --- modal wiring ---

  onEditSite(): void {
    this.openEdit({type: 'site', label: this.site()!.siteCode, data: this.site()!});
  }

  onEditSector(s: SectorVm): void {
    this.openEdit(sectorToNode(s.sector));
  }

  onEditAntenna(a: AntennaVm): void {
    this.openEdit(antennaToNode(a.antenna));
  }

  onEditTilt(tilt: ElectricalTiltDto): void {
    this.openEdit(tiltToNode(tilt));
  }

  onAddSector(): void {
    this.openAdd('sector', {type: 'site', label: this.site()!.siteCode, data: this.site()!});
  }

  onAddAntenna(s: SectorVm): void {
    this.openAdd('antenna', sectorToNode(s.sector));
  }

  onAddTilt(a: AntennaVm): void {
    this.openAdd('tilt', antennaToNode(a.antenna));
  }

  private openEdit(node: TreeNodeVm): void {
    this.editNode = node;
    this.parentNode = undefined;
    this.modalMode = 'edit';
    this.modalNodeType = node.type;
    this.modalOpen.set(true);
  }

  private openAdd(nodeType: NodeType, parent: TreeNodeVm): void {
    this.editNode = undefined;
    this.parentNode = parent;
    this.modalMode = 'create';
    this.modalNodeType = nodeType;
    this.modalOpen.set(true);
  }

  onModalDeleted(): void {
    this.modalOpen.set(false);
    this.load();
  }

  onModalClosed(): void {
    this.modalOpen.set(false);
  }

  onModalSaved(node: TreeNodeVm): void {
    this.modalOpen.set(false);
    // re-read the site so the sheet and the rose always reflect what the server stored
    this.load();

    if (this.modalMode === 'edit' && node.type === 'sector') {
      this.offerAzimuthCascade(this.editNode!.data as SectorDto, node.data as SectorDto);
    }
  }

  /** Sector azimuth changed: offer to rotate its antennas by the same amount */
  private offerAzimuthCascade(before: SectorDto, after: SectorDto): void {
    const sv = this.sectors().find(s => s.sector.name === after.name);
    if (after.azimuth == null || before.azimuth === after.azimuth || !sv?.antennas.length) return;

    const from = before.azimuth ?? null;
    const rotate = (azimuth?: number) => from == null || azimuth == null
      ? after.azimuth!
      : ((azimuth + after.azimuth! - from) % 360 + 360) % 360;

    this.cascade.set({
      sectorName: after.name,
      from,
      to: after.azimuth,
      items: sv.antennas.map(a => ({antenna: a.antenna, to: rotate(a.antenna.azimuth), checked: true}))
    });
  }

  rotation(c: Cascade): string {
    if (c.from == null) return '';
    const delta = ((c.to - c.from + 540) % 360) - 180;
    return (delta > 0 ? '+' : '') + delta + '°';
  }

  selectedCount(c: Cascade): number {
    return c.items.filter(i => i.checked).length;
  }

  applyCascade(): void {
    const c = this.cascade()!;
    const selected = c.items.filter(i => i.checked);
    this.cascading.set(true);

    forkJoin(selected.map(i => this.antennaService.updateAntenna({...i.antenna, azimuth: i.to}).pipe(
      map(() => true),
      catchError(err => {
        this.alertService.httpError(`Failed to update antenna ${i.antenna.antennaIndex}`, err);
        return of(false);
      })
    ))).subscribe(results => {
      const updated = results.filter(Boolean).length;
      if (updated) this.alertService.success(`Updated azimuth on ${updated} ${updated === 1 ? 'antenna' : 'antennas'}`);
      this.cascading.set(false);
      this.cascade.set(null);
      this.load();
    });
  }
}
