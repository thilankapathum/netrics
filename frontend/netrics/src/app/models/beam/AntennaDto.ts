import {AuditDto} from './AuditDto';

export interface AntennaDto {
  id?: number;
  antennaIndex?: number;
  azimuth?: number;
  mechanicalTilt?: number;
  antennaHeight?: number;
  antennaTypeName?: string;
  sectorName: string;
  manufacturerName?: string;
  audit?: AuditDto;
}
