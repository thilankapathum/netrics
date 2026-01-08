export interface WorstCellsWithLatestDto {
  id: number;
  cellName:string;
  kpiName: string;
  kpiLabel:string;
  unit:string;
  value:number;
  previousValue:number;
  difference:number;
  improved:boolean;
  latestValue:number;
  latestDifference:number;
  latestImproved:boolean;
}
