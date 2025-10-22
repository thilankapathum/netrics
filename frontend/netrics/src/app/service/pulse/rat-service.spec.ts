import { TestBed } from '@angular/core/testing';

import { RatService } from './rat-service';

describe('RatService', () => {
  let service: RatService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RatService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
