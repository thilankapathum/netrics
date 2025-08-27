import {KpiSnapshot} from './KpiSnapshot';

export interface BasicKpiSnapshot {
  kpiLabel?: string;
  value?: number;
  previousValue?: number;
  difference?: number;
  improved?: boolean;
  unit?:string;
  standardKpis?: KpiSnapshot[];
}
