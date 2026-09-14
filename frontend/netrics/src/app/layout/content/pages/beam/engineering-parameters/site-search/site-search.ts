import {Component, OnDestroy, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, takeUntil} from 'rxjs/operators';
import {SiteService} from '../../../../../../service/pulse/site-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {SiteDto} from '../../../../../../models/pulse/SiteDto';

@Component({
  selector: 'app-site-search',
  imports: [CommonModule, FormsModule],
  templateUrl: './site-search.html',
  styleUrl: './site-search.css'
})
export class SiteSearch implements OnInit, OnDestroy {
  query = signal('');
  results = signal<SiteDto[]>([]);

  private readonly destroy$ = new Subject<void>();
  private readonly searchTerms$ = new Subject<string>();

  constructor(private siteService: SiteService,
              private alertService: AlertService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.searchTerms$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(term => this.performSearch(term));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearch(value: string): void {
    this.query.set(value);
    if (value.length > 1) {
      this.searchTerms$.next(value);
    } else {
      this.results.set([]);
    }
  }

  private performSearch(term: string): void {
    this.siteService.searchSites(term).pipe(takeUntil(this.destroy$)).subscribe({
      next: data => this.results.set(data),
      error: err => this.alertService.error('Site search failed', 'Error', `${err.status} ${err.statusText}`)
    });
  }

  selectSite(site: SiteDto): void {
    this.router.navigate(['/beam/engineering-parameters', site.siteCode]);
  }
}
