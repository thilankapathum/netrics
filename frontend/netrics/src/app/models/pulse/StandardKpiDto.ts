export interface StandardKpiDto {
  kpiName?: string;
  label?: string;
  unit?: string;
  type?: string;
  worstOrder?: string;
  threshold?: number;
  aggregation?: string;
  basicKpi?: string;
  ratName?: string;
}
