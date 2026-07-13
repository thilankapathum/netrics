import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnomalyCells } from './anomaly-cells';

describe('AnomalyCells', () => {
  let component: AnomalyCells;
  let fixture: ComponentFixture<AnomalyCells>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnomalyCells]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AnomalyCells);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
