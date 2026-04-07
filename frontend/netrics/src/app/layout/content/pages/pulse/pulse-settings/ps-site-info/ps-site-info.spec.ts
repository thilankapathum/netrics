import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsSiteInfo } from './ps-site-info';

describe('PsSiteInfo', () => {
  let component: PsSiteInfo;
  let fixture: ComponentFixture<PsSiteInfo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PsSiteInfo]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsSiteInfo);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
