import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnomalySummaryModal } from './anomaly-summary-modal';

describe('AnomalySummaryModal', () => {
  let component: AnomalySummaryModal;
  let fixture: ComponentFixture<AnomalySummaryModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnomalySummaryModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AnomalySummaryModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
