import { TestBed } from '@angular/core/testing';

import { MapCellThrSetService } from './map-cell-thr-set-service';

describe('MapCellThrSetService', () => {
  let service: MapCellThrSetService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MapCellThrSetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
