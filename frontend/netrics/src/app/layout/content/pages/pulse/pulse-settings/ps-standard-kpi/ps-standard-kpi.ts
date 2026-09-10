import {Component, effect, EventEmitter, Input, Output, signal} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {KeycloakProfile} from 'keycloak-js';
import {RatDto} from '../../../../../../models/pulse/RatDto';
import {StandardKpiDto} from '../../../../../../models/pulse/StandardKpiDto';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {AuthService} from '../../../../../../auth/service/auth-service';
import {StandardkpiService} from '../../../../../../service/pulse/ltefdd/standardkpi.service';
import {RatService} from '../../../../../../service/pulse/rat-service';
import {BasickpiService} from '../../../../../../service/pulse/ltefdd/basickpi.service';
import {BasicKpiDto} from '../../../../../../models/pulse/BasicKpiDto';

export type StandardKpiType = 'standard' | 'numerator' | 'denominator';

@Component({
  selector: 'app-ps-standard-kpi',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './ps-standard-kpi.html',
  styleUrl: './ps-standard-kpi.css'
})
export class PsStandardKpi {
  userProfile: KeycloakProfile = {};

  activeTab = signal<'CREATE' | 'LIST'>('CREATE');

  readonly kpiTypes: StandardKpiType[] = ['standard', 'numerator', 'denominator'];

  rats: RatDto[] = [];

  // ---- Create form state ----
  creatingKpi = signal<boolean>(false);
  createRat = signal<string>('');

  kpiName = signal<string>('');
  label = signal<string>('');
  unit = signal<string>('');
  type = signal<StandardKpiType>('standard');
  worstOrder = signal<string>('ASC');
  threshold = signal<number | undefined>(undefined);
  aggregation = signal<string>('AVG');
  basicKpi = signal<string>('');

  // Basic KPI options, scoped to the selected RAT in the Create form
  basicKpis = signal<BasicKpiDto[]>([]);
  loadingBasicKpis = signal<boolean>(false);

  // ---- List state ----
  listRat = signal<string>('');
  loadingList = signal<boolean>(false);
  standardKpis = signal<StandardKpiDto[]>([]);

  constructor(private alertService: AlertService,
              private authService: AuthService,
              private standardKpiService: StandardkpiService,
              private basicKpiService: BasickpiService,
              private ratService: RatService) {
    this.getAllRats();

    // Whenever the create-form RAT changes, reload its Basic KPI options
    effect(() => {
      const rat = this.createRat();
      if (rat) {
        this.loadBasicKpis(rat);
      }
    });
  }

  setActiveTab(tab: 'CREATE' | 'LIST') {
    this.activeTab.set(tab);
    if (tab === 'LIST' && this.standardKpis().length === 0 && this.listRat()) {
      this.loadStandardKpis();
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
          this.loadStandardKpis();
        }
      }, error: err => {
        console.error(err);
        this.alertService.error('Error retrieving RATs');
      }
    });
  }

  loadStandardKpis() {
    if (!this.listRat()) {
      return;
    }
    this.loadingList.set(true);
    this.standardKpiService.getAllStandardKpiWithOperands(this.listRat()).subscribe({
      next: data => {
        this.standardKpis.set(data);
        this.loadingList.set(false);
      }, error: error => {
        console.error(error);
        this.alertService.error(`Error retrieving Standard KPIs for ${this.listRat()}`, 'Error', `${error.statusText}`);
        this.loadingList.set(false);
      }
    });
  }

  loadBasicKpis(ratName: string) {
    this.loadingBasicKpis.set(true);
    // Selected Basic KPI may not belong to the new RAT; clear it out
    this.basicKpi.set('');
    this.basicKpiService.getAllBasicKpi(ratName).subscribe({
      next: data => {
        this.basicKpis.set(data);
        this.loadingBasicKpis.set(false);
      }, error: error => {
        console.error(error);
        this.alertService.error(`Error retrieving Basic KPIs for ${ratName}`, 'Error', `${error.statusText}`);
        this.loadingBasicKpis.set(false);
      }
    });
  }

  //================== FILTERS =========================

  selectCreateRat(ratName: string) {
    this.createRat.set(ratName);
  }

  selectListRat(ratName: string) {
    this.listRat.set(ratName);
    this.loadStandardKpis();
  }

  //================== TYPE HANDLING =========================

  isStandardType() {
    return this.type() === 'standard';
  }

  onTypeChange(newType: StandardKpiType) {
    this.type.set(newType);
    // Clear fields that don't apply outside 'standard' type
    if (newType !== 'standard') {
      this.worstOrder.set('');
      this.threshold.set(undefined);
      this.aggregation.set('');
    } else {
      // Restore sensible defaults when switching back to 'standard'
      if (!this.worstOrder()) this.worstOrder.set('ASC');
      if (!this.aggregation()) this.aggregation.set('AVG');
    }
  }

  //================== CREATE =========================

  createStandardKpi(): void {
    this.creatingKpi.set(true);

    const isStandard = this.isStandardType();

    const dto: StandardKpiDto = {
      kpiName: this.kpiName(),
      label: this.label(),
      unit: this.unit(),
      type: this.type(),
      worstOrder: isStandard ? this.worstOrder() : '',
      threshold: isStandard ? this.threshold() : undefined,
      aggregation: isStandard ? this.aggregation() : '',
      basicKpi: this.basicKpi(),
      ratName: this.createRat()
    };

    this.standardKpiService.createStandardKpi(dto).subscribe({
      next: value => {
        this.alertService.success(`Standard KPI ${value.kpiName} created successfully.`);
        this.resetForm();
        this.creatingKpi.set(false);
        // Refresh the list if we're looking at the same RAT
        if (this.listRat() === dto.ratName) {
          this.loadStandardKpis();
        }
      }, error: err => {
        console.error(err);
        this.alertService.error(`Error creating Standard KPI ${dto.kpiName}`, 'Error', `${err.statusText}`);
        this.creatingKpi.set(false);
      }
    });
  }

  resetForm(): void {
    this.kpiName.set('');
    this.label.set('');
    this.unit.set('');
    this.type.set('standard');
    this.worstOrder.set('ASC');
    this.threshold.set(undefined);
    this.aggregation.set('AVG');
    this.basicKpi.set('');
  }

  isInputsValid() {
    const baseValid = this.kpiName() != '' && this.kpiName().length > 2
      && this.label() != '' && this.label().length > 2
      && this.createRat() != '';

    if (!this.isStandardType()) {
      return baseValid;
    }

    // 'standard' type additionally requires worstOrder & aggregation to be set
    return baseValid && this.worstOrder() != '' && this.aggregation() != '';
  }

  //============== USER VALIDATION =============================

  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
  }

  hasAnyRole(roles: string[]) {
    return this.authService.hasAnyRole(roles);
  }
}
