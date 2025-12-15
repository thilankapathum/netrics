import {Injectable, signal} from '@angular/core';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SharedService {

  selectedGranularity = signal<'day-average' | 'busy-hour'>('day-average');
  selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');
  selectedStandardKpi = signal('');
  selectedCell = signal('');
  district = signal('');
  aggregation = signal<'day' | 'week' | 'month'>('day')
  excludeZeroes: boolean = false;
  currentPage: number = 0;
  //day.avg-busy.hr

  clearAll(){
    this.selectedStandardKpi.set('');
    this.selectedCell.set('');
    this.district.set('');
    this.excludeZeroes = false;
    this.currentPage = 0;
  }

}
