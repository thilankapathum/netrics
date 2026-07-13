import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpgradeReview } from './upgrade-review';

describe('UpgradeReview', () => {
  let component: UpgradeReview;
  let fixture: ComponentFixture<UpgradeReview>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpgradeReview]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpgradeReview);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
