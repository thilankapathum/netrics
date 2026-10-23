export interface NameCount {
  name: string;
  count: number;
}

export interface EngineeringOverviewDto {
  counts: { sites: number; sectors: number; antennas: number; tilts: number };
  /** Largest first; unassigned rows are grouped as "Not set" */
  breakdowns: { operator: NameCount[]; infraType: NameCount[]; band: NameCount[]; manufacturer: NameCount[] };
  gaps: GapDto[];
}

export interface GapDto {
  key: 'sitesWithoutSectors' | 'sectorsWithoutAntennas' | 'antennasWithoutTilts' | 'sectorAzimuthMissing' | 'towerHeightMissing';
  label: string;
  /** Offending rows */
  count: number;
  /** Up to 20 of the sites they sit on */
  siteCodes: string[];
}
