import { Component } from '@angular/core';
import {SiteWiseReportByKpisAndDate} from './site-wise-report-by-kpis-and-date/site-wise-report-by-kpis-and-date';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-kpi-reports',
  imports: [
    SiteWiseReportByKpisAndDate,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './kpi-reports.html',
  styleUrl: './kpi-reports.css'
})
export class KpiReports {

}
