import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KpiMap } from './kpi-map';

describe('KpiMap', () => {
  let component: KpiMap;
  let fixture: ComponentFixture<KpiMap>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KpiMap]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KpiMap);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
