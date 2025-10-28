import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DaisyUiThemeService {
  private themeSubject = new BehaviorSubject<string>('netrics_light');
  theme$ = this.themeSubject.asObservable();

  setTheme(theme: string) {
    this.themeSubject.next(theme);
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }

  getCurrentTheme(): string {
    return document.documentElement.getAttribute('data-theme') || 'netrics_light';
  }
}
