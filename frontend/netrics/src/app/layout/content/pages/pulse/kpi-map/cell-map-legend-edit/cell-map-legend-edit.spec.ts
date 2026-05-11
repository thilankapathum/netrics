import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CellMapLegendEdit } from './cell-map-legend-edit';

describe('CellMapLegendEdit', () => {
  let component: CellMapLegendEdit;
  let fixture: ComponentFixture<CellMapLegendEdit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CellMapLegendEdit]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CellMapLegendEdit);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
