export interface BandKpiSeries {
  name: string;                 // for chart legend
  data: { x: Date; y: number }[];
  bandName: string;             // custom property
  kpiLabel: string;             // custom property
  type?: "line" | "area" | "bar"; // optional
  color?: string;
}
