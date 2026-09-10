import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsStandardRawKpiMapping } from './ps-standard-raw-kpi-mapping';

describe('PsStandardRawKpiMapping', () => {
  let component: PsStandardRawKpiMapping;
  let fixture: ComponentFixture<PsStandardRawKpiMapping>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PsStandardRawKpiMapping]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsStandardRawKpiMapping);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
