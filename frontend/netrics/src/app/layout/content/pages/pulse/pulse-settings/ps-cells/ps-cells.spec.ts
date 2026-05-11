import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsCells } from './ps-cells';

describe('PsCells', () => {
  let component: PsCells;
  let fixture: ComponentFixture<PsCells>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PsCells]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsCells);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
