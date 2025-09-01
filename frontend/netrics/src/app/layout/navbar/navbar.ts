import { Component, OnInit, Renderer2, Inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class Navbar implements OnInit {
  isDarkMode = false;

  constructor(
    private renderer: Renderer2,
    @Inject(DOCUMENT) private document: Document
  ) {}

  ngOnInit() {
    // Load saved theme preference or default to light
    const savedTheme = localStorage.getItem('theme') || 'netrics_light';
    this.isDarkMode = savedTheme === 'netrics_dark';
    this.applyTheme(savedTheme);
  }

  onThemeToggle(event: Event) {
    const checkbox = event.target as HTMLInputElement;
    this.isDarkMode = checkbox.checked;
    const theme = this.isDarkMode ? 'netrics_dark' : 'netrics_light';

    this.applyTheme(theme);
    this.saveThemePreference(theme);
  }

  private applyTheme(theme: string) {
    // Set the data-theme attribute on the html element
    this.renderer.setAttribute(this.document.documentElement, 'data-theme', theme);

    // Also update any theme-controller checkboxes to stay in sync
    const themeControllers = this.document.querySelectorAll('.theme-controller') as NodeListOf<HTMLInputElement>;
    themeControllers.forEach(controller => {
      controller.checked = theme === 'netrics_dark';
    });
  }

  private saveThemePreference(theme: string) {
    localStorage.setItem('theme', theme);
  }
}
