import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PulseSettingUserAreaMapping } from './pulse-setting-user-area-mapping';

describe('PulseSettingUserAreaMapping', () => {
  let component: PulseSettingUserAreaMapping;
  let fixture: ComponentFixture<PulseSettingUserAreaMapping>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PulseSettingUserAreaMapping]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PulseSettingUserAreaMapping);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
