import {AuditDto} from './AuditDto';

export interface ElectricalTiltDto {
  id?: number;
  electricalTilt?: number;
  antennaId: number;
  bandName: string;
  audit?: AuditDto;
}
