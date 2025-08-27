import { TestBed } from '@angular/core/testing';

import { LtefddbasickpiService } from './ltefddbasickpi.service';

describe('LtefddbasickpiService', () => {
  let service: LtefddbasickpiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LtefddbasickpiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
