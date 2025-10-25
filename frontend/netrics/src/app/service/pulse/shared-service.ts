import {Injectable, signal} from '@angular/core';
import {RatDto} from '../../models/pulse/RatDto';
import {StandardKpiDto} from '../../models/pulse/StandardKpiDto';

@Injectable({
  providedIn: 'root'
})
export class SharedService {

  selectedRat = signal('');
  selectedStandardKpi = signal('');
  selectedCell = signal('');

}
