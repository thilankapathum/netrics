import { TestBed } from '@angular/core/testing';

import { StandardRawKpiMappingService } from './standard-raw-kpi-mapping-service';

describe('StandardRawKpiMappingService', () => {
  let service: StandardRawKpiMappingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StandardRawKpiMappingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
