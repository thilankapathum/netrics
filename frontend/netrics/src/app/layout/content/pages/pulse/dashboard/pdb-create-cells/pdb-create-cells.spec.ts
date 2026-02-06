import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PdbCreateCells } from './pdb-create-cells';

describe('PdbCreateCells', () => {
  let component: PdbCreateCells;
  let fixture: ComponentFixture<PdbCreateCells>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PdbCreateCells]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PdbCreateCells);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
