import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CellMapEngParaModify } from './cell-map-eng-para-modify';

describe('CellMapEngParaModify', () => {
  let component: CellMapEngParaModify;
  let fixture: ComponentFixture<CellMapEngParaModify>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CellMapEngParaModify]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CellMapEngParaModify);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
