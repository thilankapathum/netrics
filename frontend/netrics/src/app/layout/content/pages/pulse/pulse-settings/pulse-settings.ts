import {Component, ElementRef, ViewChild} from '@angular/core';
import {PulseSettingUserAreaMapping} from './pulse-setting-user-area-mapping/pulse-setting-user-area-mapping';
import {RouterLink} from '@angular/router';
import {PsStandardKpi} from './ps-standard-kpi/ps-standard-kpi';
import {PsCells} from './ps-cells/ps-cells';
import {PsCreateSite} from './ps-create-site/ps-create-site';

@Component({
  selector: 'app-pulse-settings',
  imports: [
    PulseSettingUserAreaMapping,
    RouterLink,
    PsStandardKpi,
    PsCells,
    PsCreateSite
  ],
  templateUrl: './pulse-settings.html',
  styleUrl: './pulse-settings.css'
})
export class PulseSettings {

  showUserAreaMappingModal:boolean = false;
  showStandardKpiModal:boolean = false;
  showMissingCellInfoModal:boolean = false;
  showCreateSiteModal:boolean = false;

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

}
