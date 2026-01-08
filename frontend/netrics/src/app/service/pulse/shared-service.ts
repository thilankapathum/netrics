import {Injectable, signal} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SharedService {

  selectedGranularity = signal<'day-average' | 'busy-hour'>('day-average');
  selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');
  selectedStandardKpi = signal('');
  selectedCell = signal('');
  areaType = signal<string | undefined>('');
  area = signal<string | undefined>('')
  aggregation = signal<'day' | 'week' | 'month'>('day')
  bandWise: boolean = false;
  selectedBand = signal('');
  excludeZeroes: boolean = false;
  currentPage: number = 0;

  clearAll(){
    this.selectedStandardKpi.set('');
    this.selectedCell.set('');
    this.area.set('');
    this.areaType.set('');
    this.excludeZeroes = false;
    this.currentPage = 0;
    this.bandWise = false;
    this.selectedBand.set('');
  }

}
