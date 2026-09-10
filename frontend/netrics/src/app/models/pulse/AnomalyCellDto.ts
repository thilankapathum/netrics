export interface AnomalyCellDto {
  cellName: string;
  kpiName: string;
  kpiLabel: string;
  unit: string;
  value: number;
  previousValue: number;
  difference: number;
  improved: number;
  consecutiveBadDays: number;
  severity: string;
  hasAlarmCorrelation: boolean | null;
  distinctAlarmDefCount: number | null;
  totalAlarmOccurrences: number | null;
  bestMatchLevel: 'CELL' | 'NODE' | null;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  pageSize: number;
}

export type SortField = 'cellName' | 'value' | 'difference' | 'severity' | 'alarmCorrelation';
export type SortDir = 'asc' | 'desc';

export type AlarmCorrelationFilter = 'all' | 'correlated' | 'uncorrelated';
