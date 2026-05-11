import {Injectable, signal} from '@angular/core';
import {LatLngExpression} from 'leaflet';

@Injectable({
  providedIn: 'root'
})
export class SharedService {

  //-- General
  returnPage:string = '';
  selectedGranularity = signal<string>('day-average');
  selectedRat = signal<string>('ltefdd');
  selectedStandardKpi = signal('');
  areaType = signal<string | undefined>('');
  area = signal<string | undefined>('')
  aggregation = signal<'day' | 'week' | 'month'>('day')

  //-- Pulse Component
  bandWise: boolean = false;
  selectedBand = signal('');
  excludeZeroes: boolean = false;
  currentPage: number = 0;

  //-- Cell Analysis
  selectedCell = signal('');

  //-- Cell Map
  cellMapDate = signal<string>('');
  minLng: number = 0;
  minLat: number = 0;
  maxLng: number = 0;
  maxLat: number = 0;
  zoom: number = 10;
  viewCoordinates: LatLngExpression = [7.8731, 80.7718];

  clearAll() {
    this.returnPage = '';
    this.selectedGranularity.set('day-average');
    this.selectedRat.set('ltefdd');
    this.selectedStandardKpi.set('');
    this.selectedCell.set('');
    this.area.set('');
    this.areaType.set('');
    this.excludeZeroes = false;
    this.currentPage = 0;
    this.bandWise = false;
    this.selectedBand.set('');
    this.cellMapDate.set('');

    this.minLng = 0;
    this.minLat = 0;
    this.maxLng = 0;
    this.maxLat = 0;
    this.zoom = 10;
    this.viewCoordinates = [7.8731, 80.7718];
  }

}
