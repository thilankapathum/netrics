import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsKpiSourceMapping } from './ps-kpi-source-mapping';

describe('PsKpiSourceMapping', () => {
  let component: PsKpiSourceMapping;
  let fixture: ComponentFixture<PsKpiSourceMapping>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PsKpiSourceMapping]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsKpiSourceMapping);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
