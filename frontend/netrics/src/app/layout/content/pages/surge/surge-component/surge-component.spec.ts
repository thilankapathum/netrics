import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SurgeComponent } from './surge-component';

describe('SurgeComponent', () => {
  let component: SurgeComponent;
  let fixture: ComponentFixture<SurgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SurgeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SurgeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
