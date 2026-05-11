import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CellMapCellAnalysis } from './cell-map-cell-analysis';

describe('CellMapCellAnalysis', () => {
  let component: CellMapCellAnalysis;
  let fixture: ComponentFixture<CellMapCellAnalysis>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CellMapCellAnalysis]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CellMapCellAnalysis);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
