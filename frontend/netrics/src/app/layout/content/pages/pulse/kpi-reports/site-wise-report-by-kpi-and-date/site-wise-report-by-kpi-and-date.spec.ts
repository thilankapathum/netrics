import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SiteWiseReportByKpiAndDate } from './site-wise-report-by-kpi-and-date';

describe('SiteWiseReportByKpiAndDate', () => {
  let component: SiteWiseReportByKpiAndDate;
  let fixture: ComponentFixture<SiteWiseReportByKpiAndDate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SiteWiseReportByKpiAndDate]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SiteWiseReportByKpiAndDate);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
