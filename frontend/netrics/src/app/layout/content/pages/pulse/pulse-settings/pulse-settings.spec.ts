import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PulseSettings } from './pulse-settings';

describe('PulseSettings', () => {
  let component: PulseSettings;
  let fixture: ComponentFixture<PulseSettings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PulseSettings]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PulseSettings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
