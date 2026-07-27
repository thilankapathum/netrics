export interface WorstCells {
  timestamp?: Date;
  cellName?: string;
  kpiName?: string;
  kpiLabel?: string;
  unit?: string;
  value?: number;
  previousValue?: number;
  difference?: number;
  improved?: number;
  consecutiveBadDays?: number;
  severity?: string;
  hasAlarmCorrelation?: boolean;
  distinctAlarmDefCount?: number;
  totalAlarmOccurrences?: number;
  bestMatchLevel?: string
}
