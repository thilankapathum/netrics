import { TestBed } from '@angular/core/testing';

import { StandardkpiService } from './standardkpi.service';

describe('StandardkpiService', () => {
  let service: StandardkpiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StandardkpiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
