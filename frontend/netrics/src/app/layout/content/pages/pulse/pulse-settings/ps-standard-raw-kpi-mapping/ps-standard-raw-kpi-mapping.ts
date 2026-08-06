import {Component, computed, effect, signal} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {KeycloakProfile} from 'keycloak-js';
import {RatDto} from '../../../../../../models/pulse/RatDto';
import {StandardKpiDto} from '../../../../../../models/pulse/StandardKpiDto';
import {StandardRawKpiMappingDto} from '../../../../../../models/pulse/StandardRawKpiMappingDto';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {AuthService} from '../../../../../../auth/service/auth-service';
import {StandardRawKpiMappingService} from '../../../../../../service/pulse/standard-raw-kpi-mapping-service';
import {StandardkpiService} from '../../../../../../service/pulse/ltefdd/standardkpi.service';
import {RatService} from '../../../../../../service/pulse/rat-service';

@Component({
  selector: 'app-ps-standard-raw-kpi-mapping',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './ps-standard-raw-kpi-mapping.html',
  styleUrl: './ps-standard-raw-kpi-mapping.css'
})
export class PsStandardRawKpiMapping {
  userProfile: KeycloakProfile = {};

  activeTab = signal<'CREATE' | 'LIST'>('CREATE');

  rats: RatDto[] = [];

  // ---- Create form state ----
  creatingMapping = signal<boolean>(false);
  createRat = signal<string>('');

  standardKpi = signal<string>('');
  numerator = signal<string>('');
  denominator = signal<string>('');

  // All Standard KPIs for the selected RAT, split by type
  allStandardKpisForRat = signal<StandardKpiDto[]>([]);
  loadingStandardKpis = signal<boolean>(false);

  standardTypeKpis = computed(() =>
    this.allStandardKpisForRat().filter(k => k.type === 'standard'));
  numeratorTypeKpis = computed(() =>
    this.allStandardKpisForRat().filter(k => k.type === 'numerator'));
  denominatorTypeKpis = computed(() =>
    this.allStandardKpisForRat().filter(k => k.type === 'denominator'));

  // Whether the selected 'standard' KPI already has a mapping (info only, not blocking)
  checkingAvailability = signal<boolean>(false);
  mappingAlreadyExists = signal<boolean>(false);

  // ---- List state ----
  listRat = signal<string>('');
  loadingList = signal<boolean>(false);
  mappings = signal<StandardRawKpiMappingDto[]>([]);

  constructor(private alertService: AlertService,
              private authService: AuthService,
              private standardRawKpiMappingService: StandardRawKpiMappingService,
              private standardKpiService: StandardkpiService,
              private ratService: RatService) {
    this.getAllRats();

    // Whenever the create-form RAT changes, reload Standard KPIs for it
    effect(() => {
      const rat = this.createRat();
      if (rat) {
        this.loadStandardKpisForRat(rat);
      }
    });

    // Whenever the selected 'standard' KPI changes, check if it's already mapped
    effect(() => {
      const rat = this.createRat();
      const kpi = this.standardKpi();
      if (rat && kpi) {
        this.checkMappingAvailability(rat, kpi);
      } else {
        this.mappingAlreadyExists.set(false);
      }
    });
  }

  setActiveTab(tab: 'CREATE' | 'LIST') {
    this.activeTab.set(tab);
    if (tab === 'LIST' && this.mappings().length === 0 && this.listRat()) {
      this.loadMappings();
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
          this.loadMappings();
        }
      }, error: err => {
        console.error(err);
        this.alertService.error('Error retrieving RATs');
      }
    });
  }

  loadStandardKpisForRat(ratName: string) {
    this.loadingStandardKpis.set(true);
    // Selections may not belong to the new RAT; clear them out
    this.standardKpi.set('');
    this.numerator.set('');
    this.denominator.set('');
    this.standardKpiService.getAllStandardKpiWithOperands(ratName).subscribe({
      next: data => {
        this.allStandardKpisForRat.set(data);
        this.loadingStandardKpis.set(false);
      }, error: error => {
        console.error(error);
        this.alertService.error(`Error retrieving Standard KPIs for ${ratName} - ${error.statusText}`);
        this.loadingStandardKpis.set(false);
      }
    });
  }

  checkMappingAvailability(ratName: string, standardKpiName: string) {
    this.checkingAvailability.set(true);
    this.standardRawKpiMappingService.isMappingAvailable(ratName, standardKpiName).subscribe({
      next: exists => {
        this.mappingAlreadyExists.set(exists);
        this.checkingAvailability.set(false);
      }, error: error => {
        console.error(error);
        // Non-critical check; fail silently rather than blocking the form
        this.mappingAlreadyExists.set(false);
        this.checkingAvailability.set(false);
      }
    });
  }

  loadMappings() {
    if (!this.listRat()) {
      return;
    }
    this.loadingList.set(true);
    this.standardRawKpiMappingService.getAll(this.listRat()).subscribe({
      next: data => {
        this.mappings.set(data);
        this.loadingList.set(false);
      }, error: error => {
        console.error(error);
        this.alertService.error(`Error retrieving Standard Raw KPI Mappings for ${this.listRat()} - ${error.statusText}`);
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
    this.loadMappings();
  }

  //================== CREATE =========================

  createMapping(): void {
    this.creatingMapping.set(true);

    const dto: StandardRawKpiMappingDto = {
      standardKpi: this.standardKpi(),
      numerator: this.numerator(),
      denominator: this.denominator(),
      ratName: this.createRat()
    };

    this.standardRawKpiMappingService.createMapping(dto).subscribe({
      next: value => {
        this.alertService.success(`Mapping for ${value.standardKpi} created successfully.`);
        this.resetForm();
        this.creatingMapping.set(false);
        // Refresh the list if we're looking at the same RAT
        if (this.listRat() === dto.ratName) {
          this.loadMappings();
        }
      }, error: err => {
        console.error(err);
        this.alertService.error(`Error creating mapping for ${dto.standardKpi} - ${err.statusText}`);
        this.creatingMapping.set(false);
      }
    });
  }

  resetForm(): void {
    this.standardKpi.set('');
    this.numerator.set('');
    this.denominator.set('');
    this.mappingAlreadyExists.set(false);
  }

  isInputsValid() {
    return this.standardKpi() != ''
      && this.numerator() != ''
      && this.denominator() != ''
      && this.createRat() != '';
  }

  //============== USER VALIDATION =============================

  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
  }

  hasAnyRole(roles: string[]) {
    return this.authService.hasAnyRole(roles);
  }
}
