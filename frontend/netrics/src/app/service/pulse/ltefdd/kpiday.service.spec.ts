import { TestBed } from '@angular/core/testing';

import { KpidayService } from './kpiday.service';

describe('KpidayService', () => {
  let service: KpidayService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KpidayService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
