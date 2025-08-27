import { TestBed } from '@angular/core/testing';

import { LtefddstandardkpiService } from './ltefddstandardkpi.service';

describe('LtefddstandardkpiService', () => {
  let service: LtefddstandardkpiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LtefddstandardkpiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
