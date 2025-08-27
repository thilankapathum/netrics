export interface KpiSnapshot {
  kpiLabel?: string;
  value?: number;
  previousValue?: number;
  difference?: number;
  improved?: boolean;
}
