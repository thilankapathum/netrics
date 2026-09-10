import { Component, signal } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AreaTypeDto } from '../../../../../../models/pulse/AreaTypeDto';
import { AreaDto } from '../../../../../../models/pulse/AreaDto';
import { DistrictCodeDto } from '../../../../../../models/pulse/DistrictCodeDto';
import { AreaDistrictCodeMappingDto } from '../../../../../../models/pulse/AreaDistrictCodeMappingDto';
import { AreaTypeService } from '../../../../../../service/pulse/area-type-service';
import { AreaService } from '../../../../../../service/pulse/area-service';
import { DistrictCodeService } from '../../../../../../service/pulse/district-code-service';
import { AreaDistrictCodeMappingService } from '../../../../../../service/pulse/area-district-code-mapping-service';
import { AlertService } from '../../../../../../components/alert/alert.service';

@Component({
  selector: 'app-ps-area-management',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './ps-area-management.html',
  styleUrl: './ps-area-management.css'
})
export class PsAreaManagement {

  activeTab = signal<'AREA_TYPE' | 'AREA' | 'DISTRICT_ASSIGNMENT'>('AREA_TYPE');

  areaTypeSubTab = signal<'CREATE' | 'LIST'>('CREATE');
  areaSubTab = signal<'CREATE' | 'LIST'>('CREATE');
  assignmentSubTab = signal<'CREATE' | 'LIST'>('CREATE');

  areaTypes: AreaTypeDto[] = [];

  // ---- Area Type tab ----
  newAreaTypeName = signal('');
  creatingAreaType = false;

  // ---- Area tab ----
  areaFormAreaType = signal('');
  newAreaName = signal('');
  newAreaEnabled = signal(true);
  creatingArea = false;
  areasForAreaFormType: AreaDto[] = [];

  // ---- District Assignment tab ----
  assignAreaType = signal('');
  assignAreas: AreaDto[] = [];
  assignArea = signal('');
  districtCodes: DistrictCodeDto[] = [];
  assigningCode: string | null = null;
  unassigningId: number | null = null;

  // ---- Lists ----
  loadingMappings = false;
  mappings: AreaDistrictCodeMappingDto[] = [];

  constructor(private areaTypeService: AreaTypeService,
              private areaService: AreaService,
              private districtCodeService: DistrictCodeService,
              private areaDistrictCodeMappingService: AreaDistrictCodeMappingService,
              private alertService: AlertService) {
    this.getAllAreaTypes();
    this.getAllDistrictCodes();
    this.loadMappings();
  }

  setActiveTab(tab: 'AREA_TYPE' | 'AREA' | 'DISTRICT_ASSIGNMENT') {
    this.activeTab.set(tab);
  }

  setAreaTypeSubTab(tab: 'CREATE' | 'LIST') {
    this.areaTypeSubTab.set(tab);
  }

  setAreaSubTab(tab: 'CREATE' | 'LIST') {
    this.areaSubTab.set(tab);
  }

  setAssignmentSubTab(tab: 'CREATE' | 'LIST') {
    this.assignmentSubTab.set(tab);
  }

  //================== LOADERS =========================

  getAllAreaTypes() {
    this.areaTypeService.getAllAreaTypes().subscribe({
      next: data => {
        this.areaTypes = data;
        if (this.areaTypes.length > 0) {
          this.areaFormAreaType.set(this.areaTypes[0].name!);
          this.assignAreaType.set(this.areaTypes[0].name!);
          this.getAreasForAreaForm(this.areaFormAreaType());
          this.getAreasForAssignment(this.assignAreaType());
        }
      }, error: error => {
        console.error(error);
        this.alertService.error('Error getting Area Types', 'Error', `${error.status}:${error.statusText}`);
      }
    });
  }

  getAllDistrictCodes() {
    this.districtCodeService.getAll().subscribe({
      next: data => this.districtCodes = data,
      error: error => {
        console.error(error);
        this.alertService.error('Error getting District Codes', 'Error', `${error.status}:${error.statusText}`);
      }
    });
  }

  loadMappings() {
    this.loadingMappings = true;
    this.areaDistrictCodeMappingService.getAll().subscribe({
      next: data => {
        this.mappings = data;
        this.loadingMappings = false;
      }, error: error => {
        console.error(error);
        this.alertService.error('Error getting Area-District Code Mappings', 'Error', `${error.status}:${error.statusText}`);
        this.loadingMappings = false;
      }
    });
  }

  //================== AREA TYPE =========================

  createAreaType() {
    if (!this.newAreaTypeName()) {
      this.alertService.error('Enter an Area Type name');
      return;
    }
    this.creatingAreaType = true;
    this.areaTypeService.createAreaType({ name: this.newAreaTypeName() }).subscribe({
      next: data => {
        this.alertService.success(`Area Type '${data.name}' created successfully`);
        this.areaTypes = [data, ...this.areaTypes];
        this.newAreaTypeName.set('');
        this.creatingAreaType = false;
      }, error: error => {
        console.error(error);
        this.alertService.error('Error creating Area Type');
        this.creatingAreaType = false;
      }
    });
  }

  //================== AREA =========================

  selectAreaFormAreaType(areaTypeName: string) {
    this.areaFormAreaType.set(areaTypeName);
    this.getAreasForAreaForm(areaTypeName);
  }

  getAreasForAreaForm(areaTypeName: string) {
    if (!areaTypeName) {
      return;
    }
    this.areaService.getAreasByAreaTypes(areaTypeName).subscribe({
      next: data => this.areasForAreaFormType = data,
      error: error => {
        console.error(error);
        this.alertService.error('Error getting Areas', 'Error', `${error.status}:${error.statusText}`);
      }
    });
  }

  createArea() {
    if (!this.newAreaName() || !this.areaFormAreaType()) {
      this.alertService.error('Enter an Area name and select an Area Type');
      return;
    }
    this.creatingArea = true;
    const area: AreaDto = {
      name: this.newAreaName(),
      enabled: this.newAreaEnabled(),
      areaTypeName: this.areaFormAreaType()
    };
    this.areaService.createArea(area).subscribe({
      next: data => {
        this.alertService.success(`Area '${data.name}' created successfully`);
        this.areasForAreaFormType = [data, ...this.areasForAreaFormType];
        this.newAreaName.set('');
        this.creatingArea = false;
      }, error: error => {
        console.error(error);
        this.alertService.error('Error creating Area');
        this.creatingArea = false;
      }
    });
  }

  //================== DISTRICT ASSIGNMENT =========================

  selectAssignAreaType(areaTypeName: string) {
    this.assignAreaType.set(areaTypeName);
    this.getAreasForAssignment(areaTypeName);
  }

  getAreasForAssignment(areaTypeName: string) {
    if (!areaTypeName) {
      return;
    }
    this.areaService.getAreasByAreaTypes(areaTypeName).subscribe({
      next: data => {
        this.assignAreas = data;
        this.assignArea.set(data.length > 0 ? data[0].name! : '');
      }, error: error => {
        console.error(error);
        this.alertService.error('Error getting Areas', 'Error', `${error.status}:${error.statusText}`);
      }
    });
  }

  // District codes not yet assigned to the currently selected Area, sorted ascending.
  // (a district code can be assigned to more than one Area, so this only excludes
  // codes already assigned to *this* area, not every assigned code globally.)
  getAvailableDistrictCodes(): DistrictCodeDto[] {
    const assignedToThisArea = new Set(
      this.mappings.filter(m => m.areaName === this.assignArea()).map(m => m.districtCode)
    );
    return this.districtCodes
      .filter(dc => !assignedToThisArea.has(dc.code))
      .sort((a, b) => (a.code ?? '').localeCompare(b.code ?? ''));
  }

  // District codes assigned to the currently selected Area, sorted ascending.
  getAssignedDistrictCodesForArea(): AreaDistrictCodeMappingDto[] {
    return this.mappings
      .filter(m => m.areaName === this.assignArea())
      .sort((a, b) => (a.districtCode ?? '').localeCompare(b.districtCode ?? ''));
  }

  assignCode(code: string) {
    if (!this.assignArea()) {
      this.alertService.error('Select an Area first');
      return;
    }
    this.assigningCode = code;
    const mapping: AreaDistrictCodeMappingDto = {
      areaName: this.assignArea(),
      districtCode: code
    };
    this.areaDistrictCodeMappingService.create(mapping).subscribe({
      next: data => {
        this.mappings = [data, ...this.mappings];
        this.assigningCode = null;
      }, error: error => {
        console.error(error);
        this.alertService.error('Error assigning District Code');
        this.assigningCode = null;
      }
    });
  }

  unassignCode(mapping: AreaDistrictCodeMappingDto) {
    if (!mapping.id) {
      return;
    }
    this.unassigningId = mapping.id;
    this.areaDistrictCodeMappingService.delete(mapping.id).subscribe({
      next: () => {
        this.mappings = this.mappings.filter(m => m.id !== mapping.id);
        this.unassigningId = null;
      }, error: error => {
        console.error(error);
        this.alertService.error('Error unassigning District Code');
        this.unassigningId = null;
      }
    });
  }
}
