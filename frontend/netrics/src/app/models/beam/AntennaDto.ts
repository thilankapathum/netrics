import {AuditDto} from './AuditDto';

export interface AntennaDto {
  id?: number;
  antennaIndex?: number;
  azimuth?: number;
  mechanicalTilt?: number;
  antennaHeight?: number;
  /** electrical tilt is set remotely (RET) and read from OSS; no manual tilts */
  retAvailable?: boolean;
  antennaTypeName?: string;
  sectorName: string;
  manufacturerName?: string;
  audit?: AuditDto;
}
