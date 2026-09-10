import { TestBed } from '@angular/core/testing';

import { AlarmTypeService } from './alarm-type-service';

describe('AlarmTypeService', () => {
  let service: AlarmTypeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AlarmTypeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
