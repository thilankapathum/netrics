import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { AreaTypeDto } from '../../../../../../models/pulse/AreaTypeDto';
import { AreaDto } from '../../../../../../models/pulse/AreaDto';
import { AreaTypeService } from '../../../../../../service/pulse/area-type-service';
import { AlertService } from '../../../../../../components/alert/alert.service';
import { AreaService } from '../../../../../../service/pulse/area-service';
import { PulseSettingService } from '../../../../../../service/pulse/pulse-setting-service';
import { UserAreaMappingDto } from '../../../../../../models/pulse/UserAreaMappingDto';
import { UserService } from '../../../../../../service/pulse/user-service';
import { UserDto } from '../../../../../../models/pulse/UserDto';

@Component({
  selector: 'app-pulse-setting-user-area-mapping',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './pulse-setting-user-area-mapping.html',
  styleUrl: './pulse-setting-user-area-mapping.css'
})
export class PulseSettingUserAreaMapping {
  @Input() open: boolean = false;
  @Output() closed = new EventEmitter<void>();

  areaType = signal<string | undefined>('');
  areaTypes: AreaTypeDto[] = [];

  area = signal<string | undefined>('');
  areas: AreaDto[] = [];

  userId = signal('');
  users: UserDto[] = [];

  loadingUserAreaMapping = false;

  mappings: UserAreaMappingDto[] = [];
  loadingMappings = false;

  editingMappingId: number | null = null;
  editAreaType = signal<string | undefined>('');
  editAreas: AreaDto[] = [];
  editArea = signal<string | undefined>('');
  savingEdit = false;

  mappingPendingDelete: UserAreaMappingDto | null = null;

  constructor(private formBuilder: FormBuilder,
              private areaTypeService: AreaTypeService,
              private alertService: AlertService,
              private areaService: AreaService,
              private settingService: PulseSettingService,
              private userService: UserService) {
    this.getAllAreaTypes();
    this.getAllUsers();
    this.loadMappings();
  }

  loadMappings() {
    this.loadingMappings = true;
    this.settingService.getAllUserAreaMappings().subscribe({
      next: data => {
        this.mappings = data;
        this.loadingMappings = false;
      }, error: error => {
        console.log(error);
        this.alertService.error('Error getting User-Area Mappings', 'Error', `${error.status}:${error.statusText}`);
        this.loadingMappings = false;
      }
    });
  }

  startEditMapping(mapping: UserAreaMappingDto) {
    this.editingMappingId = mapping.id!;
    this.editAreaType.set(mapping.areaTypeName);
    this.editArea.set(mapping.areaName);
    this.editAreas = [];
    this.areaService.getAreasByAreaTypes(mapping.areaTypeName!).subscribe({
      next: data => this.editAreas = data,
      error: error => {
        console.log(error);
        this.alertService.error('Error getting Areas', 'Error', `${error.status}:${error.statusText}`);
      }
    });
  }

  selectEditAreaType(areaTypeName: string) {
    this.editAreaType.set(areaTypeName);
    this.editArea.set('');
    this.areaService.getAreasByAreaTypes(areaTypeName).subscribe({
      next: data => {
        this.editAreas = data;
        this.editArea.set(data.length > 0 ? data[0].name : '');
      }, error: error => {
        console.log(error);
        this.alertService.error('Error getting Areas', 'Error', `${error.status}:${error.statusText}`);
      }
    });
  }

  selectEditArea(areaName: string) {
    this.editArea.set(areaName);
  }

  cancelEditMapping() {
    this.editingMappingId = null;
  }

  saveEditMapping(mapping: UserAreaMappingDto) {
    if (!this.editArea()) {
      this.alertService.error('Select an area before saving');
      return;
    }
    this.savingEdit = true;
    this.settingService.updateUserAreaMapping(mapping.id!, this.editArea()!).subscribe({
      next: updated => {
        const idx = this.mappings.findIndex(m => m.id === mapping.id);
        if (idx > -1) {
          this.mappings[idx] = updated;
        }
        this.alertService.success(`Updated mapping for ${updated.userFullName}`);
        this.editingMappingId = null;
        this.savingEdit = false;
      }, error: error => {
        console.log(error);
        this.alertService.error('Error updating User-Area Mapping');
        this.savingEdit = false;
      }
    });
  }

  requestDeleteMapping(mapping: UserAreaMappingDto) {
    this.mappingPendingDelete = mapping;
  }

  cancelDeleteMapping() {
    this.mappingPendingDelete = null;
  }

  confirmDeleteMapping() {
    const mapping = this.mappingPendingDelete;
    if (!mapping?.id) {
      return;
    }
    this.settingService.deleteUserAreaMapping(mapping.id).subscribe({
      next: () => {
        this.mappings = this.mappings.filter(m => m.id !== mapping.id);
        this.alertService.success(`Removed mapping for ${mapping.userFullName}`);
        this.mappingPendingDelete = null;
      }, error: error => {
        console.log(error);
        this.alertService.error('Error deleting User-Area Mapping');
        this.mappingPendingDelete = null;
      }
    });
  }

  getAllUsers() {
    this.userService.getAllUsers().subscribe({
      next: data => {
        this.users = data;
        if (this.users.length > 0) {
          this.userId.set(this.users[0].userId);
        }
      }, error: error => {
        console.log(error);
        this.alertService.error('Error getting Users', 'Error', `${error.status}:${error.statusText}`);
      }
    });
  }

  onCancel(): void {
    this.closed.emit();
    this.userId.set('');
  }

  getAllAreaTypes() {
    this.areaTypeService.getAllAreaTypes().subscribe({
      next: data => {
        this.areaTypes = data;
        if (this.areaTypes.length > 0) {
          this.areaType.set(this.areaTypes[0].name!);
          this.getAreasByAreaType(this.areaType()!);
        }
      }, error: error => {
        console.log(error);
        this.alertService.error('Error getting Area-types', 'Error', `${error.status}:${error.statusText}`);
      }
    });
  }

  getAreasByAreaType(areaTypeName: string) {
    this.areaService.getAreasByAreaTypes(areaTypeName).subscribe({
      next: data => {
        this.areas = data;
        if (this.areas.length > 0) {
          this.area.set(this.areas.at(0)?.name);
        } else {
          this.area.set('');
        }
      }, error: error => {
        console.log(error);
        this.alertService.error('Error getting Areas', 'Error', `${error.status}:${error.statusText}`);
      }
    });
  }

  selectAreaType(areaType: string) {
    this.areaType.set(areaType);
    this.getAreasByAreaType(this.areaType()!);
    console.log(this.areaType());
  }

  selectArea(areaName: string) {
    this.area.set(areaName);
    console.log(this.area());
  }

  createUserAreaMapping() {
    this.loadingUserAreaMapping = true;
    console.log(this.userId());
    console.log(this.area());

    if (this.userId() != '' && this.area() != '') {
      const userAreaMapping: UserAreaMappingDto = { userId: this.userId(), areaName: this.area()! };
      this.settingService.createUserAreaMapping(userAreaMapping).subscribe({
        next: data => {
          this.alertService.success(`Successfully created mapping for ${data.areaName} - ${data.userFullName}`);
          console.log(data);
          this.mappings = [data, ...this.mappings];
          this.loadingUserAreaMapping = false;
          this.onCancel();
        }, error: error => {
          console.log(error);
          this.alertService.error(`Error creating User-Area Mapping`);
          this.loadingUserAreaMapping = false;
        }
      });
    } else {
      this.alertService.error(`Error creating User-Area Mapping`);
      this.loadingUserAreaMapping = false;
    }
  }
}
