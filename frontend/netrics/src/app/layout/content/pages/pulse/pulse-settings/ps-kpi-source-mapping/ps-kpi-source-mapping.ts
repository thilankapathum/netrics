import {Component, effect, signal} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {KeycloakProfile} from 'keycloak-js';
import {RatDto} from '../../../../../../models/pulse/RatDto';
import {OssDto} from '../../../../../../models/pulse/OssDto';
import {StandardKpiDto} from '../../../../../../models/pulse/StandardKpiDto';
import {KpiMappingToOssDto} from '../../../../../../models/pulse/KpiMappingToOssDto';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {AuthService} from '../../../../../../auth/service/auth-service';
import {KpimappingtoossService} from '../../../../../../service/pulse/kpimappingtooss-service';
import {StandardkpiService} from '../../../../../../service/pulse/ltefdd/standardkpi.service';
import {OssService} from '../../../../../../service/pulse/oss-service';
import {RatService} from '../../../../../../service/pulse/rat-service';

@Component({
  selector: 'app-ps-kpi-source-mapping',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './ps-kpi-source-mapping.html',
  styleUrl: './ps-kpi-source-mapping.css'
})
export class PsKpiSourceMapping {
  userProfile: KeycloakProfile = {};

  activeTab = signal<'CREATE' | 'LIST'>('CREATE');

  rats: RatDto[] = [];
  ossList = signal<OssDto[]>([]);

  // ---- Create form state ----
  creatingMapping = signal<boolean>(false);
  createRat = signal<string>('');

  ossKpiName = signal<string>('');
  ossIdentifier = signal<string>('');
  standardKpi = signal<string>('');
  multiplicationFactor = signal<number>(1.0);

  // Standard KPI options, scoped to the selected RAT in the Create form
  standardKpis = signal<StandardKpiDto[]>([]);
  loadingStandardKpis = signal<boolean>(false);

  // ---- List state ----
  listRat = signal<string>('');
  loadingList = signal<boolean>(false);
  kpiMappings = signal<KpiMappingToOssDto[]>([]);

  constructor(private alertService: AlertService,
              private authService: AuthService,
              private kpiMappingToOssService: KpimappingtoossService,
              private standardKpiService: StandardkpiService,
              private ossService: OssService,
              private ratService: RatService) {
    this.getAllRats();
    this.getAllOss();

    // Whenever the create-form RAT changes, reload its Standard KPI options
    effect(() => {
      const rat = this.createRat();
      if (rat) {
        this.loadStandardKpis(rat);
      }
    });
  }

  setActiveTab(tab: 'CREATE' | 'LIST') {
    this.activeTab.set(tab);
    if (tab === 'LIST' && this.kpiMappings().length === 0 && this.listRat()) {
      this.loadKpiMappings();
    }
  }

  //================== GETTERS =============================

  getAllRats(): void {
    this.ratService.getAllRats().subscribe({
      next: data => {
        this.rats = data;
        if (this.rats.length > 0) {
          this.createRat.set(this.rats[0].name!);
          this.listRat.set(this.rats[0].name!);
          this.loadKpiMappings();
        }
      }, error: err => {
        console.error(err);
        this.alertService.error('Error retrieving RATs');
      }
    });
  }

  getAllOss(): void {
    this.ossService.getAllOss().subscribe({
      next: data => {
        this.ossList.set(data);
      }, error: err => {
        console.error(err);
        this.alertService.error('Error retrieving OSS list');
      }
    });
  }

  loadStandardKpis(ratName: string) {
    this.loadingStandardKpis.set(true);
    // Selected Standard KPI may not belong to the new RAT; clear it out
    this.standardKpi.set('');
    this.standardKpiService.getAllStandardKpi(ratName).subscribe({
      next: data => {
        this.standardKpis.set(data);
        this.loadingStandardKpis.set(false);
      }, error: error => {
        console.error(error);
        this.alertService.error(`Error retrieving Standard KPIs for ${ratName} - ${error.statusText}`);
        this.loadingStandardKpis.set(false);
      }
    });
  }

  loadKpiMappings() {
    if (!this.listRat()) {
      return;
    }
    this.loadingList.set(true);
    this.kpiMappingToOssService.getAllKpiMappings(this.listRat()).subscribe({
      next: data => {
        this.kpiMappings.set(data);
        this.loadingList.set(false);
      }, error: error => {
        console.error(error);
        this.alertService.error(`Error retrieving KPI Mappings for ${this.listRat()} - ${error.statusText}`);
        this.loadingList.set(false);
      }
    });
  }

  //================== FILTERS =========================

  selectCreateRat(ratName: string) {
    this.createRat.set(ratName);
  }

  selectListRat(ratName: string) {
    this.listRat.set(ratName);
    this.loadKpiMappings();
  }

  //================== CREATE =========================

  createKpiMapping(): void {
    this.creatingMapping.set(true);

    const dto: KpiMappingToOssDto = {
      ossKpiName: this.ossKpiName(),
      multiplicationFactor: this.multiplicationFactor(),
      ossIdentifier: this.ossIdentifier(),
      standardKpi: this.standardKpi(),
      ratName: this.createRat()
    };

    this.kpiMappingToOssService.createKpiMapping(dto).subscribe({
      next: value => {
        this.alertService.success(`Mapping for ${value.ossKpiName} created successfully.`);
        this.resetForm();
        this.creatingMapping.set(false);
        // Refresh the list if we're looking at the same RAT
        if (this.listRat() === dto.ratName) {
          this.loadKpiMappings();
        }
      }, error: err => {
        console.error(err);
        this.alertService.error(`Error creating KPI Mapping for ${dto.ossKpiName} - ${err.statusText}`);
        this.creatingMapping.set(false);
      }
    });
  }

  resetForm(): void {
    this.ossKpiName.set('');
    this.ossIdentifier.set('');
    this.standardKpi.set('');
    this.multiplicationFactor.set(1.0);
  }

  isInputsValid() {
    return this.ossKpiName() != '' && this.ossKpiName().length > 1
      && this.ossIdentifier() != ''
      && this.standardKpi() != ''
      && this.createRat() != ''
      && this.multiplicationFactor() != null
      && !isNaN(this.multiplicationFactor());
  }

  //============== USER VALIDATION =============================

  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
  }

  hasAnyRole(roles: string[]) {
    return this.authService.hasAnyRole(roles);
  }
}
