export interface KpiSnapshot {
  kpiLabel?: string;
  value?: number;
  unit?: string;
  previousValue?: number;
  difference?: number;
  improved?: boolean;
}
