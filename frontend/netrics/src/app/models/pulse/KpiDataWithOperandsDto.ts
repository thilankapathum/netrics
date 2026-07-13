export interface KpiDataWithOperandsDto {
  timestamp?: Date;
  cellName?: string;
  kpiLabel?: string;
  kpiValue?: number;
  numeratorKpiValue?: number;
  denominatorKpiValue?: number;
}
