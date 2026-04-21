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

@Component({
  selector: 'app-pulse-settings',
  imports: [
    PulseSettingUserAreaMapping,
    RouterLink,
    PsStandardKpi,
    PsCells,
    PsCreateSite,
    PsEvictCache,
    PsSiteInfo
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
  missingCellInfoCount = signal(0);

  constructor(private cellService:CellService,
              private alertService: AlertService,) {
    this.getCellCountWithMissingInfo();
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

}
