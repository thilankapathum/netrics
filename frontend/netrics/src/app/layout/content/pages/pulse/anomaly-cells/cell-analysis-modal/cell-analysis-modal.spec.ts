import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CellAnalysisModal } from './cell-analysis-modal';

describe('CellAnalysisModal', () => {
  let component: CellAnalysisModal;
  let fixture: ComponentFixture<CellAnalysisModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CellAnalysisModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CellAnalysisModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
