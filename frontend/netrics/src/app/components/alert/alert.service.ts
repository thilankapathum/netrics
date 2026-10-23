import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';


export type AlertType = 'info'|'success' | 'warning'| 'error';

export interface AlertMessage{
  id: number;
  type: AlertType;
  title: string;
  message: string;
  errorCode?: string | number;
}


@Injectable({
  providedIn: 'root'
})
export class AlertService {

  private alerts: AlertMessage[] = [];
  private alertSubject = new BehaviorSubject<AlertMessage[]>([]);
  alerts$ = this.alertSubject.asObservable();
  private counter = 0;

  private push(message: string, type: AlertType, title: string, errorCode?: string | number) {
    const alert: AlertMessage = { id: ++this.counter, message, type, title, errorCode };
    this.alerts = [...this.alerts, alert];
    this.alertSubject.next(this.alerts);
  }

  info(message: string, title = 'Info') { this.push(message, 'info', title); }
  success(message: string, title = 'Success') { this.push(message, 'success', title); }
  warning(message: string, title = 'Warning') { this.push(message, 'warning', title); }
  error(message: string, title = 'Error', errorCode?: string | number) { this.push(message, 'error', title, errorCode); }

  /** Shows the backend ErrorResponse message/fieldErrors instead of a bare HTTP status */
  httpError(action: string, err: HttpErrorResponse) {
    const body = err.error;
    const fields = body?.fieldErrors?.map((f: any) => `${f.field}: ${f.message}`).join('; ');
    const msg = fields || body?.message || (err.status === 0 ? 'Cannot reach the server' : err.statusText);
    this.error(`${action}: ${msg}`, 'Error', err.status || undefined);
  }

  remove(id: number) {
    this.alerts = this.alerts.filter(a => a.id !== id);
    this.alertSubject.next(this.alerts);
  }

  clear() {
    this.alerts = [];
    this.alertSubject.next(this.alerts);
  }


}
