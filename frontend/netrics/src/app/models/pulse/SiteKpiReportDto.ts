export interface SiteKpiReportDto {
  timestamp: Date,
  siteCode: string,
  kpiName: string,
  label: string,
  kpiValue: number,
  concatBands: string,
}
