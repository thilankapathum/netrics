import {Component, OnInit} from '@angular/core';
import {AlertMessage, AlertService} from '../alert.service';
import {Subscription} from 'rxjs';
import {NgClass} from '@angular/common';

@Component({
  selector: 'app-alert-component',
  imports: [
    NgClass
  ],
  templateUrl: './alert-component.html',
  styleUrl: './alert-component.css'
})
export class AlertComponent implements OnInit {

  alerts: AlertMessage[] = [];
  private subscription!: Subscription;

  constructor(private alertService: AlertService) {}

  ngOnInit(): void {
    this.subscription = this.alertService.alerts$.subscribe(alerts => {
      this.alerts = alerts;
    });
  }

  getTypeClass(type: string) {
    switch (type) {
      case 'success': return 'alert-success';
      case 'warning': return 'alert-warning';
      case 'error': return 'alert-error';
      default: return 'alert-info';
    }
  }

  close(id: number) {
    this.alertService.remove(id);
  }

  ngOnDestroy(): void {
    if (this.subscription) this.subscription.unsubscribe();
  }

}
