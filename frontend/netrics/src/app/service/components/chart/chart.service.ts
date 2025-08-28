import { Injectable } from '@angular/core';
import {KpiDataDto} from '../../../models/pulse/KpiDataDto';
import {ApexAxisChartSeries} from 'ng-apexcharts';

@Injectable({
  providedIn: 'root'
})
export class ChartService {

  public buildSeriesKpiDataDto(kpiDataDto: KpiDataDto[]): ApexAxisChartSeries {
    const grouped = kpiDataDto.reduce((acc, curr) => {
      const key = curr.kpiLabel ?? 'Unknown KPI';
      if (!acc[key]) {
        acc[key] = [];
      }
      acc[key].push({
        x: new Date(curr.timestamp!),
        y: this.round2(curr.kpiValue ?? 0)
      });
      return acc;
    }, {} as Record<string, { x: Date; y: number }[]>);

    return Object.entries(grouped).map(([name, data]) => ({
      name,
      data
    }));
  }

  private round2(n: number): number {
    return Math.round((n + Number.EPSILON) * 100) / 100;
  }

}
