export interface WorstCellsDashboardDto {
  id: number;
  cellName:string;
  kpiName: string;
  kpiLabel:string;
  unit:string;
  value:number;
  previousValue:number;
  difference:number;
  improved:boolean;
}
