import { TestBed } from '@angular/core/testing';

import { PulseSettingService } from './pulse-setting-service';

describe('PulseSettingService', () => {
  let service: PulseSettingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PulseSettingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
