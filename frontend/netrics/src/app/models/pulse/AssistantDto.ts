export type AssistantStatus = 'ANSWER' | 'CLARIFY' | 'REFUSED';

export type AssistantTool =
  'get_worst_cells'
  | 'get_anomalies'
  | 'get_kpi_trend'
  | 'get_area_kpi_snapshot'
  | 'get_cell_kpi';

export interface AssistantRequestDto {
  question: string;
}

export interface AssistantResponseDto {
  status: AssistantStatus;
  message: string | null;
  summary: string | null;         // AI-generated, may be null
  tool: AssistantTool | null;
  params: Record<string, string>; // resolved canonical values: kpi, kpiLabel, area, rat, period, granularity, ...
  rows: unknown[];                // shape depends on tool; authoritative data
  options: string[];              // choices for CLARIFY
}
