import {Component, Input, OnChanges, OnInit, signal, SimpleChanges} from '@angular/core';
import * as L from 'leaflet';
import {MapSector} from '../../../../../../models/pulse/MapSector';
import {MapCellService} from '../../../../../../service/pulse/map-cell/map-cell-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {MapCellDto} from '../../../../../../models/pulse/MapCellDto';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {DatePipe} from '@angular/common';
import {debounceTime, filter, Subject, takeUntil} from 'rxjs';
import {MapCellThrSetAndThresholds} from '../../../../../../models/pulse/map-cell/MapCellThrSetAndThresholds';

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
  @Input() loadingAll: boolean = false;
  @Input() mapCellThrSetAndThresholds = signal<MapCellThrSetAndThresholds | undefined>(undefined);

  private map!: L.Map;
  private sectorLayer = new L.LayerGroup();
  private reloadTrigger = new Subject<void>();

  loadingCells: boolean = false;
  private isMapReady: boolean = false;

  constructor(private mapCellService: MapCellService,
              private alertService: AlertService,) {
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
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ratName'] || changes['granularityName'] || changes['standardKpiName'] || changes['date']) {
      this.reloadTrigger.next();
    }
  }

  initMap() {
    this.map = L.map('map').setView([7.8731, 80.7718], 8);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {attribution: 'OSM'}).addTo(this.map);

    this.sectorLayer.addTo(this.map);

    this.isMapReady = true;

    //-- Reload when moving
    this.map.on('moveend', () => {
      this.reloadTrigger.next();
    });

  }

  loadCells() {

    if (!this.hasValidInputs()) return;

    this.loadingCells = true;
    const bounds = this.map.getBounds();

    const params: any = {
      minLat: bounds.getSouth(),
      maxLat: bounds.getNorth(),
      minLng: bounds.getWest(),
      maxLng: bounds.getEast()
    };

    this.mapCellService.getCellsByStandardKpi(params.minLng, params.minLat, params.maxLng, params.maxLat, this.standardKpiName, this.ratName, this.granularityName, this.date).subscribe(
      {
        next: data => {
          this.renderCells(data);
          this.loadingCells = false;
        }, error: err => {
          console.log(err);
          this.alertService.error(`Error ${err.message}`);
          this.loadingCells = false;
        }
      }
    )
  }

  reloadCells(): void {
    if (!this.map) return; // map not ready
    this.reloadTrigger.next();
  }

  renderCells(cells: MapCellDto[]): void {
    this.sectorLayer.clearLayers();

    cells.sort((a, b) => b.radius - a.radius);

    cells.forEach(cell => {
      const polygon = this.drawCell(
        cell.latitude,
        cell.longitude,
        cell.azimuth,
        cell.beamwidth,
        cell.radius
      );

      const polygonColor = this.getPolygonColor(cell.kpiValue);

      const defaultStyle = {
        color: polygonColor,
        fillColor: polygonColor,
        fillOpacity: 0.4,
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
        `<b> ${cell.cellName}</b> <br>
        Site: ${cell.siteName}<br/>
        KPI: ${cell.kpiLabel} <b>${cell.kpiValue}</b><br/>`
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

    // polygon.bringToFront();
  }

  resetSectorStyle(polygon: L.Polygon, defaultStyle: any) {
    polygon.setStyle(defaultStyle);
  }

  getDynamicRadius() {

  }

  getPolygonColor(kpiValue: number): string {
    if (!this.mapCellThrSetAndThresholds()?.thresholds || this.mapCellThrSetAndThresholds()?.thresholds?.length === 0) {
      return 'grey';
    }

    const match = this.mapCellThrSetAndThresholds()?.thresholds?.find(thr =>
      kpiValue >= thr.minValue && kpiValue < thr.maxValue);

    return match ? match.color : 'grey';
  }

  //TODO: Open cell analysis window on sector click
  onSectorClick(sector: MapSector) {
    alert(`Cell: ${sector.cellName}\nKPI: ${sector.kpiValue}`);
  }

  hasValidInputs(): boolean {
    return !!(
      this.standardKpiName &&
      this.ratName &&
      this.granularityName &&
      this.date
    );
  }

}
