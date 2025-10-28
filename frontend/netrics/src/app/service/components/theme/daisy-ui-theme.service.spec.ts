import { TestBed } from '@angular/core/testing';

import { DaisyUiThemeService } from './daisy-ui-theme.service';

describe('DaisyUiThemeService', () => {
  let service: DaisyUiThemeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DaisyUiThemeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
