import { TestBed } from '@angular/core/testing';

import { AlarmSourceService } from './alarm-source-service';

describe('AlarmSourceService', () => {
  let service: AlarmSourceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AlarmSourceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
