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
  severity: 'critical' | 'high' | 'moderate' | null;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  pageSize: number;
}

export type SortField = 'cellName' | 'severity' | 'difference' | 'value';
export type SortDir = 'asc' | 'desc';
