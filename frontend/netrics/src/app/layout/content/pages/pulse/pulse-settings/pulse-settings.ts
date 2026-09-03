import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PulseSettingUserAreaMapping } from './pulse-setting-user-area-mapping/pulse-setting-user-area-mapping';
import { PsStandardKpi } from './ps-standard-kpi/ps-standard-kpi';
import { PsCells } from './ps-cells/ps-cells';
import { PsCreateSite } from './ps-create-site/ps-create-site';
import { PsEvictCache } from './ps-evict-cache/ps-evict-cache';
import { PsSiteInfo } from './ps-site-info/ps-site-info';
import { PsSectorInfo } from './ps-sector-info/ps-sector-info';
import { CellService } from '../../../../../service/pulse/cell-service';
import { AlertService } from '../../../../../components/alert/alert.service';
import { SectorService } from '../../../../../service/pulse/sector-service';
import { SiteService } from '../../../../../service/pulse/site-service';
import {PsCellSettings} from './ps-cell-settings/ps-cell-settings';
import {PsSiteManagement} from './ps-site-management/ps-site-management';
import {PsKpiSourceMapping} from './ps-kpi-source-mapping/ps-kpi-source-mapping';
import {PsStandardRawKpiMapping} from './ps-standard-raw-kpi-mapping/ps-standard-raw-kpi-mapping';
import {PsCellMapping} from './ps-cell-mapping/ps-cell-mapping';

export type SettingsView =
  | 'CACHE'
  | 'CELL_INFO'
  | 'CELL_MAPPING'
  | 'SECTOR_INFO'
  | 'CREATE_SITE'
  | 'SITE_INFO'
  | 'STANDARD_KPI'
  | 'KPI_SOURCE_MAPPING'
  | 'STANDARD_RAW_KPI_MAPPING'
  | 'USER_AREA_MAPPING'
  | null;

@Component({
  selector: 'app-pulse-settings',
  imports: [
    PulseSettingUserAreaMapping,
    RouterLink,
    PsStandardKpi,
    PsKpiSourceMapping,
    PsCellSettings,
    PsCreateSite,
    PsEvictCache,
    PsSectorInfo,
    PsSiteManagement,
    PsKpiSourceMapping,
    PsStandardRawKpiMapping,
    PsCellMapping
  ],
  templateUrl: './pulse-settings.html',
  styleUrl: './pulse-settings.css'
})
export class PulseSettings {

  // Track the currently active view instead of multiple modal states
  activeView = signal<SettingsView>(null);

  missingCellInfoCount = signal(0);
  missingSectorInfoCount = signal(0);
  missingSiteInfoCount = signal(0);

  constructor(
    private cellService: CellService,
    private alertService: AlertService,
    private sectorService: SectorService,
    private siteService: SiteService,
  ) {
    this.getCellCountWithMissingInfo();
    this.getSectorCountWithMissingInfo();
    this.getSiteCountWithMissingInfo();
  }

  setActiveView(view: SettingsView) {
    this.activeView.set(view);
  }

  getCellCountWithMissingInfo() {
    this.cellService.getCellCountWithMissingInfo().subscribe({
      next: data => this.missingCellInfoCount.set(data),
      error: error => {
        console.error("Error getCellCountWithMissingInfo:", error);
        this.alertService.error('Error retrieving Cell count with missing information', 'Error', `${error.status} ${error.statusText}`);
      }
    });
  }

  getSiteCountWithMissingInfo() {
    this.siteService.getSiteCountWithMissingInfo().subscribe({
      next: data => this.missingSiteInfoCount.set(data),
      error: error => {
        console.error("Error getSiteCountWithMissingInfo:", error);
        this.alertService.error('Error retrieving Site count with missing information', 'Error', `${error.status} ${error.statusText}`);
      }
    });
  }

  getSectorCountWithMissingInfo() {
    this.sectorService.getSectorCountWithMissingInfo().subscribe({
      next: data => this.missingSectorInfoCount.set(data),
      error: error => {
        console.error("Error getSectorCountWithMissingInfo:", error);
        this.alertService.error('Error retrieving Sector count with missing information', 'Error', `${error.status} ${error.statusText}`);
      }
    });
  }
}
