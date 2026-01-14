import {Component, ElementRef, ViewChild} from '@angular/core';
import {PulseSettingUserAreaMapping} from './pulse-setting-user-area-mapping/pulse-setting-user-area-mapping';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-pulse-settings',
  imports: [
    PulseSettingUserAreaMapping,
    RouterLink
  ],
  templateUrl: './pulse-settings.html',
  styleUrl: './pulse-settings.css'
})
export class PulseSettings {


  showUserAreaMappingModal:boolean = false;

  openUserAreaMappingModal() {
    this.showUserAreaMappingModal = true;
  }

  closeUserAreaMappingModal() {
    this.showUserAreaMappingModal = false;
  }

}
