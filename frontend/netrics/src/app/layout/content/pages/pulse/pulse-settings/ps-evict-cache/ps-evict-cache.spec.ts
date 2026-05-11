import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsEvictCache } from './ps-evict-cache';

describe('PsEvictCache', () => {
  let component: PsEvictCache;
  let fixture: ComponentFixture<PsEvictCache>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PsEvictCache]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsEvictCache);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
