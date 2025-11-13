import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BeamComponent } from './beam-component';

describe('BeamComponent', () => {
  let component: BeamComponent;
  let fixture: ComponentFixture<BeamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BeamComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BeamComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
