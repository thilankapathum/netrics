import { Component } from '@angular/core';
import {SiteWiseReportByKpiAndDate} from './site-wise-report-by-kpi-and-date/site-wise-report-by-kpi-and-date';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-kpi-reports',
  imports: [
    SiteWiseReportByKpiAndDate,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './kpi-reports.html',
  styleUrl: './kpi-reports.css'
})
export class KpiReports {

}
