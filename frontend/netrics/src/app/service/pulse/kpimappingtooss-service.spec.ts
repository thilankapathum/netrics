import { TestBed } from '@angular/core/testing';

import { KpimappingtoossService } from './kpimappingtooss-service';

describe('KpimappingtoossService', () => {
  let service: KpimappingtoossService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KpimappingtoossService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
