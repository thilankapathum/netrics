export interface OperationLogDto {
  id: number;
  entityName: 'Site' | 'Sector' | 'Antenna' | 'ElectricalTilt';
  entityId: string;
  operation: 'CREATE' | 'UPDATE' | 'DELETE';
  performedAt: string;
  /** Keycloak user id (sub), or SYSTEM */
  performedBy: string;
  /** field -> {old, new}; foreign keys hold the related row's id */
  changes: Record<string, { old?: string | null; new?: string | null }>;
  /** Null when the row or its parent has been deleted */
  siteCode: string | null;
  sectorName: string | null;
  antennaIndex: number | null;
}
