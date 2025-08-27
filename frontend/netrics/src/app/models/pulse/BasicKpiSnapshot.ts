import {KpiSnapshot} from './KpiSnapshot';

export interface BasicKpiSnapshot {
  kpiLabel?: string;
  value?: number;
  previousValue?: number;
  difference?: number;
  improved?: boolean;
  standardKpis?: KpiSnapshot[];
}
