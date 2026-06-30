export interface WorstCells{
  cellName?:string;
  kpiName?:string;
  kpiLabel?:string;
  unit?:string;
  value?:number;
  previousValue?:number;
  difference?:number;
  improved?:number;
  consecutiveBadDays?: number;
}
