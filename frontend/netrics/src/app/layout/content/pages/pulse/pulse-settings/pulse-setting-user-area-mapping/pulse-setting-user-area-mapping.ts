import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { AreaTypeDto } from '../../../../../../models/pulse/AreaTypeDto';
import { AreaDto } from '../../../../../../models/pulse/AreaDto';
import { AreaTypeService } from '../../../../../../service/pulse/area-type-service';
import { AlertService } from '../../../../../../components/alert/alert.service';
import { AreaService } from '../../../../../../service/pulse/area-service';
import { PulseSettingService } from '../../../../../../service/pulse/pulse-setting-service';
import { UserAreaMappingDto } from '../../../../../../models/pulse/UserAreaMappingDto';

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

  loadingUserAreaMapping = false;

  constructor(private formBuilder: FormBuilder,
              private areaTypeService: AreaTypeService,
              private alertService: AlertService,
              private areaService: AreaService,
              private settingService: PulseSettingService) {
    this.getAllAreaTypes();
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
        this.alertService.error(`Error getting Area-types! (${error.status}:${error.statusText})`);
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
        this.alertService.error(`Error getting Areas! (${error.status}:${error.statusText})`);
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
          this.alertService.success(`Successfully created mapping for ${data.areaName} - ${data.userId}`);
          console.log(data);
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
