import { Component } from '@angular/core';
import {SectorMap} from './sector-map/sector-map';

@Component({
  selector: 'app-kpi-map',
  imports: [
    SectorMap
  ],
  templateUrl: './kpi-map.html',
  styleUrl: './kpi-map.css'
})
export class KpiMap {

}
