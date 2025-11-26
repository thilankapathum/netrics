import { TestBed } from '@angular/core/testing';

import { AreaTypeService } from './area-type-service';

describe('AreaTypeService', () => {
  let service: AreaTypeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AreaTypeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
