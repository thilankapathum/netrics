import { TestBed } from '@angular/core/testing';

import { AnomalyCellsService } from './anomaly-cells-service';

describe('AnomalyCellsService', () => {
  let service: AnomalyCellsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AnomalyCellsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
