import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SectorMap } from './sector-map';

describe('SectorMap', () => {
  let component: SectorMap;
  let fixture: ComponentFixture<SectorMap>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SectorMap]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SectorMap);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
