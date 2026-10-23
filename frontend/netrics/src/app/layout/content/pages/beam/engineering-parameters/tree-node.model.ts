import {SiteDto} from '../../../../../models/beam/SiteDto';
import {SectorDto} from '../../../../../models/beam/SectorDto';
import {AntennaDto} from '../../../../../models/beam/AntennaDto';
import {ElectricalTiltDto} from '../../../../../models/beam/ElectricalTiltDto';

export type NodeType = 'site' | 'sector' | 'antenna' | 'tilt';

export interface TreeNodeVm {
  type: NodeType;
  label: string;
  data: SiteDto | SectorDto | AntennaDto | ElectricalTiltDto;
  /** undefined = children not loaded yet (lazy), [] = loaded and empty */
  children?: TreeNodeVm[];
}

export function sectorToNode(sector: SectorDto): TreeNodeVm {
  return {type: 'sector', label: sector.name, data: sector};
}

export function antennaToNode(antenna: AntennaDto): TreeNodeVm {
  return {type: 'antenna', label: `Antenna ${antenna.antennaIndex}`, data: antenna};
}

export function tiltToNode(tilt: ElectricalTiltDto): TreeNodeVm {
  return {type: 'tilt', label: `${tilt.bandName} E-Tilt: ${tilt.electricalTilt}°`, data: tilt, children: []};
}
