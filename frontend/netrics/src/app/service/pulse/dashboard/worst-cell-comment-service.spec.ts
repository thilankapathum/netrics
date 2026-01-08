import { TestBed } from '@angular/core/testing';

import { WorstCellCommentService } from './worst-cell-comment-service';

describe('WorstCellCommentService', () => {
  let service: WorstCellCommentService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WorstCellCommentService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
