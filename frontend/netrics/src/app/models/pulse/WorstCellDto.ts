export interface WorstCellDto{
  timestamps: string;
  cellName:string;
  standardKpiId: number;
  unit: string;
  value:number;
  previousValue:number;
  difference:number;
  improved:number;
  ratId:number;
}
