import { TestBed } from '@angular/core/testing';

import { CellNameService } from './cell-name.service';

describe('CellNameService', () => {
  let service: CellNameService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CellNameService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
