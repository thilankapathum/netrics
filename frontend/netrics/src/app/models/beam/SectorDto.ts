import {AuditDto} from './AuditDto';

export interface SectorDto {
  sectorIndex?: number;
  name: string;
  azimuth?: number;
  siteCode?: string;
  audit?: AuditDto;
}
