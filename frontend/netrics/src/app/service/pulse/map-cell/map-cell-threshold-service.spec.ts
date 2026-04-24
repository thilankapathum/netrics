import { TestBed } from '@angular/core/testing';

import { MapCellThresholdService } from './map-cell-threshold-service';

describe('MapCellThresholdService', () => {
  let service: MapCellThresholdService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MapCellThresholdService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
