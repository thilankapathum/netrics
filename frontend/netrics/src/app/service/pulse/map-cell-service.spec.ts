import { TestBed } from '@angular/core/testing';

import { MapCellService } from './map-cell-service';

describe('MapCellService', () => {
  let service: MapCellService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MapCellService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
