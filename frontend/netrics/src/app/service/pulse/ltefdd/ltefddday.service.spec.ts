import { TestBed } from '@angular/core/testing';

import { LtefdddayService } from './ltefddday.service';

describe('LtefdddayService', () => {
  let service: LtefdddayService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LtefdddayService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
