import {Component, OnDestroy, OnInit} from '@angular/core';
import {AlertMessage, AlertService, AlertType} from '../alert.service';
import {Subscription} from 'rxjs';
import {NgClass} from '@angular/common';

const AUTO_CLOSE_MS = 5000;
const LEAVE_ANIMATION_MS = 250;

@Component({
  selector: 'app-alert-component',
  imports: [
    NgClass
  ],
  templateUrl: './alert-component.html',
  styleUrl: './alert-component.css'
})
export class AlertComponent implements OnInit, OnDestroy {

  readonly typeMeta: Record<AlertType, {icon: string; badgeClass: string}> = {
    success: { icon: '✓', badgeClass: 'toast-badge-success' },
    error: { icon: '✕', badgeClass: 'toast-badge-error' },
    warning: { icon: '!', badgeClass: 'toast-badge-warning' },
    info: { icon: 'i', badgeClass: 'toast-badge-info' },
  };

  alerts: AlertMessage[] = [];
  leavingIds = new Set<number>();

  private subscription!: Subscription;
  private knownIds = new Set<number>();
  private timers = new Map<number, ReturnType<typeof setTimeout>>();

  constructor(private alertService: AlertService) {}

  ngOnInit(): void {
    this.subscription = this.alertService.alerts$.subscribe(alerts => {
      this.alerts = alerts;

      for (const alert of alerts) {
        if (!this.knownIds.has(alert.id)) {
          this.knownIds.add(alert.id);
          this.timers.set(alert.id, setTimeout(() => this.close(alert.id), AUTO_CLOSE_MS));
        }
      }
    });
  }

  close(id: number) {
    if (this.leavingIds.has(id)) return;

    const timer = this.timers.get(id);
    if (timer) clearTimeout(timer);
    this.timers.delete(id);
    this.knownIds.delete(id);

    this.leavingIds.add(id);
    setTimeout(() => {
      this.leavingIds.delete(id);
      this.alertService.remove(id);
    }, LEAVE_ANIMATION_MS);
  }

  ngOnDestroy(): void {
    if (this.subscription) this.subscription.unsubscribe();
    this.timers.forEach(timer => clearTimeout(timer));
  }

}
