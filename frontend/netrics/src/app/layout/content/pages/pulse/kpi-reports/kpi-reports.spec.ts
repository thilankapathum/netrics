import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KpiReports } from './kpi-reports';

describe('KpiReports', () => {
  let component: KpiReports;
  let fixture: ComponentFixture<KpiReports>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KpiReports]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KpiReports);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
