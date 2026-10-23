import {SiteDto} from './SiteDto';
import {CellDto} from './CellDto';
import {SiteDto as BeamSiteDto} from '../beam/SiteDto';
import {SectorDto} from '../beam/SectorDto';
import {AntennaDto} from '../beam/AntennaDto';
import {ElectricalTiltDto} from '../beam/ElectricalTiltDto';

export type AssistantStatus = 'ANSWER' | 'CLARIFY' | 'REFUSED';

export type AssistantTool =
  'get_worst_cells'
  | 'get_anomalies'
  | 'get_kpi_trend'
  | 'get_area_kpi_snapshot'
  | 'get_cell_kpi'
  | 'get_alarms'
  | 'get_site_info'
  | 'get_cell_info';

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

/** get_site_info row: pulse site + cells, plus beam engineering parameters when the user may read them. */
export interface AssistantSiteInfo {
  site: SiteDto;
  cells: CellDto[];
  engineering: {
    site: BeamSiteDto;
    sectors: {
      sector: SectorDto;
      antennas: { antenna: AntennaDto; electricalTilts: ElectricalTiltDto[] }[];
    }[];
  } | null;
}
