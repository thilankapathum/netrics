import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsStandardKpi } from './ps-standard-kpi';

describe('PsStandardKpi', () => {
  let component: PsStandardKpi;
  let fixture: ComponentFixture<PsStandardKpi>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PsStandardKpi]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsStandardKpi);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
