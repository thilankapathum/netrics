import {Component, OnInit} from '@angular/core';
import * as L from 'leaflet';
import {MapSector} from '../../../../../../models/pulse/MapSector';
import {MapCellService} from '../../../../../../service/pulse/map-cell-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {MapCellDto} from '../../../../../../models/pulse/MapCellDto';

@Component({
  selector: 'app-sector-map',
  imports: [],
  templateUrl: './sector-map.html',
  styleUrl: './sector-map.css'
})
export class SectorMap implements OnInit {

  private map!: L.Map;
  private sectorLayer = new L.LayerGroup();

  constructor(private mapCellService: MapCellService,
              private alertService: AlertService,) {
  }


  ngOnInit(): void {
    this.initMap();
    this.loadCells();
  }

  initMap() {
    this.map = L.map('map').setView([7.8731, 80.7718], 8);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {attribution: 'OSM'}).addTo(this.map);

    this.sectorLayer.addTo(this.map);

    //-- Reload when moving
    this.map.on('moveend', () => {
      this.loadCells();
    });
  }

  loadCells() {
    const bounds = this.map.getBounds();

    const params: any = {
      minLat: bounds.getSouth(),
      maxLat: bounds.getNorth(),
      minLng: bounds.getWest(),
      maxLng: bounds.getEast()
    };

    //TODO: Apply correct arguments
    this.mapCellService.getCellsByStandardKpi(params.minLng, params.minLat, params.maxLng, params.maxLat, 'cell_availability', 'ltefdd', 'day-average').subscribe(
      {
        next: data => {
          this.renderCells(data);
          console.log(data);
        } , error: err=> {
          console.log(err);
          this.alertService.error(`Error ${err.message}`);
        }
      }
    )
  }

  reloadCells(): void {
    this.loadCells();
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

      const defaultStyle = {
        color: 'red',
        fillColor: 'pink',
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

  //TODO: Open cell analysis window on sector click
  onSectorClick(sector: MapSector) {
    console.log('Sector clicked:', sector);
    alert(`Cell: ${sector.cellName}\nKPI: ${sector.kpiValue}`);
  }

}
