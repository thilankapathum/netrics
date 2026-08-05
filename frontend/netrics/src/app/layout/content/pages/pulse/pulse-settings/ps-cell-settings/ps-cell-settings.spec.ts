import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsCellSettings } from './ps-cell-settings';

describe('PsCellSettings', () => {
  let component: PsCellSettings;
  let fixture: ComponentFixture<PsCellSettings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PsCellSettings]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsCellSettings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
