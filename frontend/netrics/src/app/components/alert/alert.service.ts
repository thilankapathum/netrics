import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';


export type AlertType = 'info'|'success' | 'warning'| 'error';

export interface AlertMessage{
  id: number;
  message: string;
  type: AlertType;
}


@Injectable({
  providedIn: 'root'
})
export class AlertService {

  private alerts: AlertMessage[] = [];
  private alertSubject = new BehaviorSubject<AlertMessage[]>([]);
  alerts$ = this.alertSubject.asObservable();
  private counter = 0;

  private push(message: string, type: AlertType) {
    const alert: AlertMessage = { id: ++this.counter, message, type };
    this.alerts = [...this.alerts, alert];
    this.alertSubject.next(this.alerts);

    // Auto-remove after 5 seconds
    setTimeout(() => this.remove(alert.id), 5000);
  }

  info(message: string) { this.push(message, 'info'); }
  success(message: string) { this.push(message, 'success'); }
  warning(message: string) { this.push(message, 'warning'); }
  error(message: string) { this.push(message, 'error'); }

  remove(id: number) {
    this.alerts = this.alerts.filter(a => a.id !== id);
    this.alertSubject.next(this.alerts);
  }

  clear() {
    this.alerts = [];
    this.alertSubject.next(this.alerts);
  }


}
