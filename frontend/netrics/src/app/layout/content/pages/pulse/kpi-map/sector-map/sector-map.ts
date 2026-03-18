import {Component, OnInit} from '@angular/core';
import * as L from 'leaflet';
import {MapSector} from '../../../../../../models/pulse/MapSector';

@Component({
  selector: 'app-sector-map',
  imports: [],
  templateUrl: './sector-map.html',
  styleUrl: './sector-map.css'
})
export class SectorMap implements OnInit {

  private map!: L.Map;
  private sectorLayer = new L.LayerGroup();


  ngOnInit(): void {
    this.initMap();
    this.loadSectors();
  }

  initMap() {
    this.map = L.map('map').setView([7.8731, 80.7718], 8);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {attribution: 'OSM'}).addTo(this.map);

    this.sectorLayer.addTo(this.map);

    //-- Reload when moving
    this.map.on('moveend', () => {
      this.loadSectors();
    });
  }

  loadSectors() {
    const bounds = this.map.getBounds();

    const params: any = {
      minLat: bounds.getSouth(),
      maxLat: bounds.getNorth(),
      minLng: bounds.getWest(),
      maxLng: bounds.getEast()
    };

    // TODO: Retrieve data
    // this.kpiMapService.getData();
    const data = [
      {
        "siteCode": "KANDY1",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-18-1",
        "latitude": 7.8731,
        "longitude": 80.7718,
        "azimuth": 0,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 200
      },
      {
        "siteCode": "KANDY1",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-18-2",
        "latitude": 7.8731,
        "longitude": 80.7718,
        "azimuth": 120,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 200
      },
      {
        "siteCode": "KANDY1",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-18-3",
        "latitude": 7.8731,
        "longitude": 80.7718,
        "azimuth": 240,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 200
      },
      {
        "siteCode": "KANDY1",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-90-1",
        "latitude": 7.8731,
        "longitude": 80.7718,
        "azimuth": 0,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 400
      },
      {
        "siteCode": "KANDY1",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-90-2",
        "latitude": 7.8731,
        "longitude": 80.7718,
        "azimuth": 120,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 400
      },
      {
        "siteCode": "KANDY1",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-90-3",
        "latitude": 7.8731,
        "longitude": 80.7718,
        "azimuth": 240,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 400
      },
      {
        "siteCode": "KANDY2",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-18-1",
        "latitude": 7.931638126100931,
        "longitude": 80.7291512064211,
        "azimuth": 60,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 200
      },
      {
        "siteCode": "KANDY2",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-18-2",
        "latitude": 7.931638126100931,
        "longitude": 80.7291512064211,
        "azimuth": 180,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 200
      },
      {
        "siteCode": "KANDY2",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-18-3",
        "latitude": 7.931638126100931,
        "longitude": 80.7291512064211,
        "azimuth": 300,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 200
      },
      {
        "siteCode": "KANDY2",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-90-1",
        "latitude": 7.931638126100931,
        "longitude": 80.7291512064211,
        "azimuth": 60,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 400
      },
      {
        "siteCode": "KANDY2",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-90-2",
        "latitude": 7.931638126100931,
        "longitude": 80.7291512064211,
        "azimuth": 180,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 400
      },
      {
        "siteCode": "KANDY2",
        "siteName": "Kandy 1",
        "cellName": "KANDY1-90-3",
        "latitude": 7.931638126100931,
        "longitude": 80.7291512064211,
        "azimuth": 300,
        "beamwidth": 65,
        "kpiValue": 99,
        "radius": 400
      }
    ];

    this.renderSectors(data);
  }

  reloadSectors(): void {
    this.loadSectors();
  }

  renderSectors(sectors: MapSector[]): void {
    this.sectorLayer.clearLayers();

    sectors.sort((a, b) => b.radius - a.radius);

    sectors.forEach(sector => {
      const polygon = this.drawSector(
        sector.latitude,
        sector.longitude,
        sector.azimuth,
        sector.beamwidth,
        sector.radius

        // this.getDynamicRadius
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
        `<b> ${sector.cellName}</b> <br>
        Site: ${sector.siteName}<br/>`
      );

      polygon.on('click', () => {
        this.onSectorClick(sector);
      });

      polygon.addTo(this.sectorLayer);
    });
  }

  drawSector(lat: number, lng: number, azimuth: number, beamwidth: number, radius: number): L.Polygon {
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

  onSectorClick(sector: MapSector) {
    console.log('Sector clicked:', sector);
    alert(`Cell: ${sector.cellName}\nKPI: ${sector.kpiValue}`);
  }

}
