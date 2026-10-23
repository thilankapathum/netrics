import {Component, OnDestroy, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {Subject} from 'rxjs';
import {debounceTime, takeUntil} from 'rxjs/operators';
import {SiteService} from '../../../../../../service/pulse/site-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {SiteDto} from '../../../../../../models/pulse/SiteDto';
import {EpExport} from '../ep-export/ep-export';

@Component({
  selector: 'app-site-search',
  imports: [CommonModule, FormsModule, RouterLink, EpExport],
  templateUrl: './site-search.html',
  styleUrl: './site-search.css'
})
export class SiteSearch implements OnInit, OnDestroy {
  query = signal('');
  results = signal<SiteDto[]>([]);
  activeIndex = signal(-1);
  searching = signal(false);
  searched = signal(false);

  private readonly destroy$ = new Subject<void>();
  private readonly searchTerms$ = new Subject<string>();

  constructor(private siteService: SiteService,
              private alertService: AlertService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.searchTerms$.pipe(
      debounceTime(300),
      takeUntil(this.destroy$)
    ).subscribe(term => this.performSearch(term));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearch(value: string): void {
    this.query.set(value);
    this.activeIndex.set(-1);
    if (value.length > 1) {
      this.searching.set(true);
      this.searchTerms$.next(value);
    } else {
      this.results.set([]);
      this.searched.set(false);
    }
  }

  onKeydown(event: KeyboardEvent): void {
    const count = this.results().length;
    if (event.key === 'ArrowDown' && count) {
      event.preventDefault();
      this.activeIndex.update(i => (i + 1) % count);
    } else if (event.key === 'ArrowUp' && count) {
      event.preventDefault();
      this.activeIndex.update(i => (i <= 0 ? count : i) - 1);
    } else if (event.key === 'Enter' && count) {
      this.selectSite(this.results()[Math.max(0, this.activeIndex())]);
      return;
    }
    document.getElementById(`site-opt-${this.activeIndex()}`)?.scrollIntoView({block: 'nearest'});
  }

  private performSearch(term: string): void {
    this.siteService.searchSites(term).pipe(takeUntil(this.destroy$)).subscribe({
      next: data => {
        // drop responses for a term the user has already typed past
        if (term !== this.query()) return;
        this.results.set(data);
        this.searching.set(false);
        this.searched.set(true);
      },
      error: err => {
        this.searching.set(false);
        this.alertService.httpError('Site search failed', err);
      }
    });
  }

  selectSite(site: SiteDto): void {
    this.router.navigate(['/beam/engineering-parameters', site.siteCode]);
  }
}
