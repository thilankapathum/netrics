import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsCreateSite } from './ps-create-site';

describe('PsCreateSite', () => {
  let component: PsCreateSite;
  let fixture: ComponentFixture<PsCreateSite>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PsCreateSite]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsCreateSite);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
