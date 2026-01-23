import { TestBed } from '@angular/core/testing';

import { GranularityService } from './granularity-service';

describe('GranularityService', () => {
  let service: GranularityService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GranularityService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
