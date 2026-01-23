import { Component } from '@angular/core';
import {SiteWiseReportByKpiAndDate} from './site-wise-report-by-kpi-and-date/site-wise-report-by-kpi-and-date';

@Component({
  selector: 'app-kpi-reports',
  imports: [
    SiteWiseReportByKpiAndDate
  ],
  templateUrl: './kpi-reports.html',
  styleUrl: './kpi-reports.css'
})
export class KpiReports {

}
