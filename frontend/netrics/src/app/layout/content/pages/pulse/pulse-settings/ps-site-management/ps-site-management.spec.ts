import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsSiteManagement } from './ps-site-management';

describe('PsSiteManagement', () => {
  let component: PsSiteManagement;
  let fixture: ComponentFixture<PsSiteManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PsSiteManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsSiteManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
