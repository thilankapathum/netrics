import { Injectable } from '@angular/core';
import {KpiDataDto} from '../../../models/pulse/KpiDataDto';
import {ApexAxisChartSeries} from 'ng-apexcharts';
import {CellKpiSeries} from '../../../models/apexCharts/CellKpiSeries';

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
      data: data.sort((a, b) => a.x.getTime() - b.x.getTime())
    }));
  }

  private round2(n: number): number {
    return Math.round((n + Number.EPSILON) * 100) / 100;
  }


  public buildSeriesForCell(kpiDataDto: KpiDataDto[]): CellKpiSeries[] {
    const grouped = kpiDataDto.reduce((acc, curr) => {
      const key = `${curr.cellName}||${curr.kpiLabel}`;

      if (!acc[key]) {
        acc[key] = {
          cellName: curr.cellName!,
          kpiLabel: curr.kpiLabel!,
          data: [] as { x: Date; y: number }[]
        };
      }

      acc[key].data.push({
        x: new Date(curr.timestamp!),
        y: this.round2(curr.kpiValue ?? 0)
      });

      return acc;
    }, {} as Record<string, { cellName: string; kpiLabel: string; data: { x: Date; y: number }[] }>);

    return Object.values(grouped).map(entry => ({
      name: `${entry.cellName} - ${entry.kpiLabel}`,
      data: entry.data.sort((a, b) => a.x.getTime() - b.x.getTime()),
      cellName: entry.cellName,
      kpiLabel: entry.kpiLabel,
      type: "line"
    }));
  }




}
