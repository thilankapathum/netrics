export interface AuditDto {
  createdAt?: string;
  /** Keycloak user id (sub), or SYSTEM */
  createdBy?: string;
  lastModifiedAt?: string;
  lastModifiedBy?: string;
}
