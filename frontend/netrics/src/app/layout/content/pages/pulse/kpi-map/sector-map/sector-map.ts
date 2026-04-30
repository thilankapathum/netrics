import {Component, EventEmitter, Input, OnChanges, OnInit, Output, signal, SimpleChanges} from '@angular/core';
import * as L from 'leaflet';
import {MapSector} from '../../../../../../models/pulse/MapSector';
import {MapCellService} from '../../../../../../service/pulse/map-cell/map-cell-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {MapCellDto} from '../../../../../../models/pulse/MapCellDto';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {DatePipe} from '@angular/common';
import {debounceTime, filter, finalize, forkJoin, Subject, takeUntil, tap} from 'rxjs';
import {MapCellThrSetAndThresholds} from '../../../../../../models/pulse/map-cell/MapCellThrSetAndThresholds';
import {SharedService} from '../../../../../../service/pulse/shared-service';
import {SiteDto} from '../../../../../../models/pulse/SiteDto';

@Component({
  selector: 'app-sector-map',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    DatePipe
  ],
  templateUrl: './sector-map.html',
  styleUrl: './sector-map.css'
})
export class SectorMap implements OnInit, OnChanges {

  @Input() ratName: string = '';
  @Input() granularityName: string = '';
  @Input() standardKpiName: string = '';
  @Input() date: string = '';
  @Input() areaName: string = '';
  @Input() loadingAll: boolean = false;
  @Input() mapCellThrSetAndThresholds = signal<MapCellThrSetAndThresholds | undefined>(undefined);
  @Output() selectedCellName = new EventEmitter<string>();
  @Output() openAnalysisDialog = new EventEmitter<boolean>();
  @Input() showSiteLabels:boolean = true;

  private map!: L.Map;
  private sectorLayer = new L.LayerGroup();
  private labelLayer = new L.LayerGroup();

  private reloadTrigger = new Subject<void>();
  private siteLabelTrigger = new Subject<void>();

  currentCells: MapCellDto[] = [];

  private loadedTiles = new Map<string, MapCellDto[]>();
  private loadedSiteTiles = new Map<string, SiteDto[]>();

  private readonly LABEL_ZOOM_THRESHOLD = 13;
  private readonly SITE_TILE_MAX_ZOOM = 14;

  loadingCells: boolean = false;
  private isMapReady: boolean = false;

  constructor(private mapCellService: MapCellService,
              private alertService: AlertService,
              private sharedService: SharedService,) {
  }

  ngOnInit(): void {
    this.initMap();

    //-- To avoid multiple API calls within a small time-window due to sudden changes to many @Input values
    this.reloadTrigger.pipe(
      filter(() => this.isMapReady),
      filter(() => this.hasValidInputs()),
      debounceTime(250)
    ).subscribe(() => {
      this.loadCells();
    });

    this.siteLabelTrigger.pipe(
      filter(() => this.isMapReady),
      filter(() => this.map.getZoom() >= this.LABEL_ZOOM_THRESHOLD),
      filter(() => this.showSiteLabels),
      debounceTime(250)
    ).subscribe(() => {
      this.loadSiteLabels()
    })
  }

  ngOnChanges(changes: SimpleChanges): void {
    const filterChanged = changes['ratName'] ||
      changes['standardKpiName'] ||
      changes['granularityName'] ||
      changes['date'] ||
      changes['areaName'];

    if (filterChanged) {
      this.loadedTiles.clear();   // Filters changed → old tiles are stale
      this.loadedSiteTiles.clear();
      this.reloadTrigger.next();
      this.siteLabelTrigger.next();
    }

    if (changes['showSiteLabels']) {
      if(this.showSiteLabels){
        this.siteLabelTrigger.next();
      } else {
        this.labelLayer.clearLayers();
      }
    }
  }

  initMap() {
    const viewCoordinates = this.sharedService.viewCoordinates;
    const zoom = this.sharedService.zoom;
    this.map = L.map('map').setView(viewCoordinates, zoom);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {attribution: 'OSM'}).addTo(this.map);

    this.sectorLayer.addTo(this.map);
    this.labelLayer.addTo(this.map);

    this.isMapReady = true;

    this.map.on('moveend', () => {
      this.reloadTrigger.next();
      this.siteLabelTrigger.next();
    });

    this.map.on('zoomend', () => {
      this.loadedTiles.clear();
      this.reloadTrigger.next();

      if (!this.showSiteLabels || this.map.getZoom() < this.LABEL_ZOOM_THRESHOLD) {
        this.labelLayer.clearLayers();
      } else {
        this.siteLabelTrigger.next();
      }
    });
  }

  loadCells() {

    if (!this.hasValidInputs()) return;

    const zoom = Math.min(this.map.getZoom(), 14);
    const bounds = this.map.getBounds();
    const tileRange = this.getTileRange(bounds, zoom);

    const missingTiles: { z: number; x: number; y: number }[] = [];

    for (let x = tileRange.minX; x <= tileRange.maxX; x++) {
      for (let y = tileRange.minY; y <= tileRange.maxY; y++) {
        const key = `${zoom}:${x}:${y}`;
        if (!this.loadedTiles.has(key)) {
          missingTiles.push({z: zoom, x, y});
        }
      }
    }

    if (missingTiles.length === 0) {
      // All tiles already cached — just re-render from memory
      this.renderFromCache(zoom, tileRange);
      return;
    }

    this.loadingCells = true;

    const requests = missingTiles.map(({z, x, y}) =>
      this.mapCellService.getCellsByTile(
        z, x, y,
        this.standardKpiName, this.ratName,
        this.granularityName, this.date, this.areaName
      ).pipe(
        tap(cells => this.loadedTiles.set(`${z}:${x}:${y}`, cells))
      )
    );

    forkJoin(requests).pipe(
      finalize(() => this.loadingCells = false)
    ).subscribe({
      next: () => this.renderFromCache(zoom, tileRange),
      error: err => this.alertService.error(`Error loading tiles: ${err.message}`)
    });

    const bounds2 = this.map.getBounds();

    this.sharedService.minLat = bounds2.getSouth();
    this.sharedService.maxLat = bounds2.getNorth();
    this.sharedService.minLng = bounds2.getWest();
    this.sharedService.maxLng = bounds2.getEast();

    this.sharedService.zoom = this.map.getZoom();
    this.sharedService.viewCoordinates = this.map.getCenter();
  }

  private loadSiteLabels(): void {
    const zoom = Math.min(this.map.getZoom(), this.SITE_TILE_MAX_ZOOM);
    const bounds = this.map.getBounds();
    const tileRange = this.getTileRange(bounds, zoom);

    const missingTiles: { z: number; x: number; y: number }[] = [];

    for (let x = tileRange.minX; x <= tileRange.maxX; x++) {
      for (let y = tileRange.minY; y <= tileRange.maxY; y++) {
        const key = `site:${zoom}:${x}:${y}`;
        if (!this.loadedSiteTiles.has(key)) {
          missingTiles.push({z: zoom, x, y});
        }
      }
    }

    if (missingTiles.length === 0) {
      this.renderSiteLabelsFromCache(zoom, tileRange);
      return;
    }

    const requests = missingTiles.map(({z, x, y}) =>
      this.mapCellService.getSitesByTile(z, x, y).pipe(
        tap(sites => this.loadedSiteTiles.set(`site:${z}:${x}:${y}`, sites))
      )
    );

    forkJoin(requests).subscribe({
      next: () => this.renderSiteLabelsFromCache(zoom, tileRange),
      error: err => console.error('Site labels failed:', err)
    });

  }

  private renderSiteLabelsFromCache(
    zoom: number,
    tileRange: {
      minX: number;
      maxX: number;
      minY: number;
      maxY: number
    }): void {

    this.labelLayer.clearLayers();

    const allSites: SiteDto[] = [];
    for (let x = tileRange.minX; x <= tileRange.maxX; x++) {
      for (let y = tileRange.minY; y <= tileRange.maxY; y++) {
        const sites = this.loadedSiteTiles.get(`site:${zoom}:${x}:${y}`);
        if (sites) allSites.push(...sites);
      }
    }

    // Deduplicate by siteCode (tile boundaries can return same site twice)
    const unique = [...new Map(allSites.map(s => [s.siteCode, s])).values()];

    unique.forEach(site => {
      const icon = L.divIcon({
        className: '',
        html: `
          <div style="transform:translate(-50%,-130%); pointer-events:none;">
            <span style="font-size:12px; font-family: 'Inter', sans-serif;  font-weight:600; color:#3F3F46;
                          padding:1px 5px; white-space:nowrap;
                         ">
              ${site.siteCode}
            </span>
          </div>`,
        iconAnchor: [0, 0]
      });

      L.marker([site.latitude!, site.longitude!], {icon, interactive: false})
        .addTo(this.labelLayer);
    });
  }


  reloadCells(): void {
    if (!this.map) return; // map not ready
    this.reloadTrigger.next();
  }

  renderCells(cells: MapCellDto[]): void {
    this.sectorLayer.clearLayers();

    cells.sort((a, b) => b.radius - a.radius);

    cells.forEach(cell => {

      const dynamicRadius = this.getDynamicRadius(cell.radius);

      const polygon = this.drawCell(
        cell.latitude,
        cell.longitude,
        cell.azimuth,
        cell.beamwidth,
        dynamicRadius
      );

      const polygonColor = this.getPolygonColor(cell.kpiValue);

      const defaultStyle = {
        color: '#808080',
        fillColor: polygonColor,
        fillOpacity: 0.6,
        weight: 1
      };

      polygon.setStyle(defaultStyle);

      polygon.on('mouseover', () => {
        this.highlightSector(polygon);
      });

      polygon.on('mouseout', () => {
        this.resetSectorStyle(polygon, defaultStyle);
      });

      polygon.bindTooltip(
        ` <span class="font-semibold text-primary"> ${cell.cellName}</span>    <br>
        <span class="text-gray-800">${cell.kpiLabel}: <b>${cell.kpiValue?.toFixed(2)}</b></span><br/>
        <span class="text-xs text-gray-400">${cell.siteCode} - ${cell.siteName}</span> <br/>`
      );

      polygon.on('click', () => {
        this.onSectorClick(cell);
      });

      polygon.addTo(this.sectorLayer);
    });
  }

  drawCell(lat: number, lng: number, azimuth: number, beamwidth: number, radius: number): L.Polygon {
    const points: L.LatLngExpression[] = [];
    const startAngle = azimuth - beamwidth / 2;
    const endAngle = azimuth + beamwidth / 2;

    points.push([lat, lng]);

    const step = 1;

    for (let angle = startAngle; angle <= endAngle; angle += step) {

      const adjustedAngle = 90 - angle;
      const rad = adjustedAngle * Math.PI / 180;

      const dx = radius * Math.cos(rad);
      const dy = radius * Math.sin(rad);

      const newLat = lat + (dy / 111320);
      const newLng = lng + (dx / (111320 * Math.cos(lat * Math.PI / 180)));

      points.push([newLat, newLng]);
    }

    points.push([lat, lng]);

    return L.polygon(points, {
      smoothFactor: 0
    });
  }

  highlightSector(polygon: L.Polygon) {
    polygon.setStyle({
      weight: 3,
      fillOpacity: 0.6
    });
  }

  resetSectorStyle(polygon: L.Polygon, defaultStyle: any) {
    polygon.setStyle(defaultStyle);
  }

  getDynamicRadius(baseRadius: number): number {
    const zoom = this.map.getZoom();
    const zoomFactor = (20 - zoom) / 10; // normalized inverse
    return baseRadius * zoomFactor;
  }

  getPolygonColor(kpiValue: number): string {
    if (!this.mapCellThrSetAndThresholds()?.thresholds || this.mapCellThrSetAndThresholds()?.thresholds?.length === 0) {
      return 'grey';
    }

    const match = this.mapCellThrSetAndThresholds()?.thresholds?.find(thr =>
      kpiValue >= thr.minValue && kpiValue < thr.maxValue);

    return match ? match.color : 'grey';
  }

  // private renderSiteLabels(cells: MapCellDto[]): void {
  //   const currentZoom = this.map.getZoom();
  //
  //   // Hide labels when zoomed too far out — too many sites, too little space
  //   if (currentZoom < this.LABEL_ZOOM_THRESHOLD) return;
  //
  //   // Group cells by siteCode — one label per site regardless of cell count
  //   const siteMap = new Map<string, {
  //     lat: number;
  //     lng: number;
  //     siteCode: string;
  //     siteName: string;
  //     cellCount: number
  //   }>();
  //
  //   cells.forEach(cell => {
  //     if (!siteMap.has(cell.siteCode)) {
  //       siteMap.set(cell.siteCode, {
  //         lat: cell.latitude,
  //         lng: cell.longitude,
  //         siteCode: cell.siteCode,
  //         siteName: cell.siteName,
  //         cellCount: 1
  //       });
  //     } else {
  //       siteMap.get(cell.siteCode)!.cellCount++;
  //     }
  //   });
  //
  //   siteMap.forEach(site => {
  //     const icon = L.divIcon({
  //       className: 'site-label',
  //       html: `<div>${site.siteCode}</div>`,
  //       iconSize: [0, 0]
  //     });
  //
  //     const marker = L.marker([site.lat, site.lng], {icon, interactive: false});
  //     marker.addTo(this.labelLayer);
  //   });
  // }

  onSectorClick(sector: MapSector) {
    this.selectedCellName.emit(sector.cellName);
    this.openAnalysisDialog.emit(true);

    // alert(`${sector.cellName}\n${sector.kpiLabel}: ${sector.kpiValue}`);
  }

  hasValidInputs(): boolean {
    return !!(
      this.standardKpiName &&
      this.ratName &&
      this.granularityName &&
      this.date
    );
  }

  private renderFromCache(zoom: number, tileRange: { minX: number; maxX: number; minY: number; maxY: number }): void {

    const allCells: MapCellDto[] = [];
    for (let x = tileRange.minX; x <= tileRange.maxX; x++) {
      for (let y = tileRange.minY; y <= tileRange.maxY; y++) {
        const cells = this.loadedTiles.get(`${zoom}:${x}:${y}`);
        if (cells) allCells.push(...cells);
      }
    }

    // Deduplicate — a cell near a tile boundary appears in 2 tiles due to buffer
    const unique = [...new Map(allCells.map(c => [c.cellName, c])).values()];
    this.currentCells = unique;
    this.renderCells(unique);
  }

  private latLngToTile(lat: number, lng: number, zoom: number): [number, number] {
    const x = Math.floor((lng + 180) / 360 * Math.pow(2, zoom));
    const y = Math.floor(
      (1 - Math.log(Math.tan(lat * Math.PI / 180) +
        1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * Math.pow(2, zoom)
    );
    return [x, y];
  }

  private getTileRange(bounds: L.LatLngBounds, zoom: number) {
    const [minX, minY] = this.latLngToTile(
      bounds.getNorth(), bounds.getWest(), zoom);
    const [maxX, maxY] = this.latLngToTile(
      bounds.getSouth(), bounds.getEast(), zoom);
    return {minX, maxX: maxX, minY, maxY};
  }

}
