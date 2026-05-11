import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsSectorInfo } from './ps-sector-info';

describe('PsSectorInfo', () => {
  let component: PsSectorInfo;
  let fixture: ComponentFixture<PsSectorInfo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PsSectorInfo]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsSectorInfo);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
