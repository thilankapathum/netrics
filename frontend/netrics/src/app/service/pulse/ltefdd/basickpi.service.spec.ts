import { TestBed } from '@angular/core/testing';

import { BasickpiService } from './basickpi.service';

describe('BasickpiService', () => {
  let service: BasickpiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BasickpiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
