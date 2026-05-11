import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CellMapLegend } from './cell-map-legend';

describe('CellMapLegend', () => {
  let component: CellMapLegend;
  let fixture: ComponentFixture<CellMapLegend>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CellMapLegend]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CellMapLegend);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
