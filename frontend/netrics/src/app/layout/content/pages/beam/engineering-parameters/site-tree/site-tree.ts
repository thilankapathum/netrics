import {Component, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
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

/** Nominal half-power beamwidth drawn on the azimuth rose */
const BEAMWIDTH = 65;
const ROSE_RADIUS = 42;

@Component({
  selector: 'app-site-tree',
  imports: [CommonModule, RouterLink, NodeEditModal],
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

  readonly roseRadius = ROSE_RADIUS;

  constructor(private route: ActivatedRoute,
              private siteService: SiteService,
              private sectorService: SectorService,
              private antennaService: AntennaService,
              private electricalTiltService: ElectricalTiltService,
              private alertService: AlertService,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    const siteCode = this.route.snapshot.paramMap.get('siteCode') ?? '';
    this.siteCode.set(siteCode);
    this.loading.set(true);
    this.load();
  }

  canAdd(): boolean {
    return this.authService.hasAnyRole(['BEAM_CREATE']);
  }

  canEdit(): boolean {
    return this.authService.hasAnyRole(['BEAM_UPDATE']);
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

  onModalClosed(): void {
    this.modalOpen.set(false);
  }

  onModalSaved(): void {
    this.modalOpen.set(false);
    // re-read the site so the sheet and the rose always reflect what the server stored
    this.load();
  }
}
