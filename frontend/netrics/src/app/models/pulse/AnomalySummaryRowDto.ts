export interface AnomalySummaryRowDto {
  kpiName: string;
  kpiLabel: string;
  critical: number;
  high: number;
  moderate: number;
  total: number;
}

export interface AnomalySummaryDto {
  rows: AnomalySummaryRowDto[];
  grandTotal: AnomalySummaryRowDto;
}
