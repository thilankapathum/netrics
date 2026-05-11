import {Component, ElementRef, signal, ViewChild} from '@angular/core';
import {PulseSettingUserAreaMapping} from './pulse-setting-user-area-mapping/pulse-setting-user-area-mapping';
import {Router, RouterLink} from '@angular/router';
import {PsStandardKpi} from './ps-standard-kpi/ps-standard-kpi';
import {PsCells} from './ps-cells/ps-cells';
import {PsCreateSite} from './ps-create-site/ps-create-site';
import {PsEvictCache} from './ps-evict-cache/ps-evict-cache';
import {PsSiteInfo} from './ps-site-info/ps-site-info';
import {CellService} from '../../../../../service/pulse/cell-service';
import {AlertService} from '../../../../../components/alert/alert.service';
import {PsSectorInfo} from './ps-sector-info/ps-sector-info';
import {SectorService} from '../../../../../service/pulse/sector-service';
import {SiteService} from '../../../../../service/pulse/site-service';

@Component({
  selector: 'app-pulse-settings',
  imports: [
    PulseSettingUserAreaMapping,
    RouterLink,
    PsStandardKpi,
    PsCells,
    PsCreateSite,
    PsEvictCache,
    PsSiteInfo,
    PsSectorInfo
  ],
  templateUrl: './pulse-settings.html',
  styleUrl: './pulse-settings.css'
})
export class PulseSettings {

  showUserAreaMappingModal:boolean = false;
  showStandardKpiModal:boolean = false;
  showMissingCellInfoModal:boolean = false;
  showCreateSiteModal:boolean = false;
  showEvictCacheModal:boolean = false;
  showSiteInformationModal:boolean = false;
  showMissingSectorInfoModal:boolean = false;
  missingCellInfoCount = signal(0);
  missingSectorInfoCount = signal(0);
  missingSiteInfoCount = signal(0);

  constructor(private cellService:CellService,
              private alertService: AlertService,
              private sectorService: SectorService,
              private siteService: SiteService,) {
    this.getCellCountWithMissingInfo();
    this.getSectorCountWithMissingInfo();
    this.getSiteCountWithMissingInfo();
    //TODO: include missing sector and site count
  }

  //---------- OPEN MODALS ------------------------

  openUserAreaMappingModal() {
    this.showUserAreaMappingModal = true;
  }

  openStandardKpiModal() {
    this.showStandardKpiModal = true;
  }

  openMissingCellInfoModal(){
    this.showMissingCellInfoModal = true;
  }

  openCreateSiteModal() {
    this.showCreateSiteModal = true;
  }

  openSiteInformationModal() {
    this.showSiteInformationModal = true;
  }

  openEvictCacheModal() {
    this.showEvictCacheModal = true;
  }

  openMissingSectorInfoModal(){
    this.showMissingSectorInfoModal = true;
  }

  //---------- CLOSE MODALS

  closeUserAreaMappingModal() {
    this.showUserAreaMappingModal = false;
  }

  closeStandardKpiModal() {
    this.showStandardKpiModal = false;
  }

  closeMissingCellInfoModal() {
    this.showMissingCellInfoModal = false;
  }

  closeCreateSiteModal() {
    this.showCreateSiteModal = false;
  }

  closeSiteInformationModal() {
    this.showSiteInformationModal = false;
  }

  closeEvictCacheModal() {
    this.showEvictCacheModal = false;
  }

  closeMissingSectorInfoModal(){
    this.showMissingSectorInfoModal = false;
  }

  //----------------------------------------------------


  getCellCountWithMissingInfo() {
    this.cellService.getCellCountWithMissingInfo().subscribe({
      next: data => {
        this.missingCellInfoCount.set(data);
      },
      error: error => {
        console.log("Error getCellCountWithMissingInfo:");
        console.error(error);
        this.alertService.error("Error retrieving Cell count with missing information");
      }
    })
  }

  getSiteCountWithMissingInfo() {
    this.siteService.getSiteCountWithMissingInfo().subscribe({
      next: data => {
        this.missingSiteInfoCount.set(data);
      },
      error: error => {
        console.log("Error getSiteCountWithMissingInfo:");
        console.error(error);
        this.alertService.error("Error retrieving Site count with missing information");
      }
    })
  }

  getSectorCountWithMissingInfo() {
    this.sectorService.getSectorCountWithMissingInfo().subscribe({
      next: data => {
        this.missingSectorInfoCount.set(data);
      },
      error: error => {
        console.log("Error getSectorCountWithMissingInfo:");
        console.error(error);
        this.alertService.error("Error retrieving Sector count with missing information");
      }
    })
  }

}
