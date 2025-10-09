import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CellAnalysis } from './cell-analysis';

describe('CellAnalysis', () => {
  let component: CellAnalysis;
  let fixture: ComponentFixture<CellAnalysis>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CellAnalysis]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CellAnalysis);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
