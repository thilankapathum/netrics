export interface CellKpiSeries {
  name: string;                 // for chart legend
  data: { x: Date; y: number }[];
  cellName: string;             // custom property
  kpiLabel: string;             // custom property
  type?: "line" | "area" | "bar"; // optional
  color?: string;
}
