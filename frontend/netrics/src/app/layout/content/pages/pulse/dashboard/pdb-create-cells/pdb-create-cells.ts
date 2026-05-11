import {Component, CUSTOM_ELEMENTS_SCHEMA, EventEmitter, Input, OnDestroy, OnInit, Output, signal} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {DatePipe} from '@angular/common';
import {RatDto} from '../../../../../../models/pulse/RatDto';
import {AreaTypeDto} from '../../../../../../models/pulse/AreaTypeDto';
import {GranularityDto} from '../../../../../../models/pulse/GranularityDto';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {RatService} from '../../../../../../service/pulse/rat-service';
import {AreaTypeService} from '../../../../../../service/pulse/area-type-service';
import {GranularityService} from '../../../../../../service/pulse/granularity-service';
import {DashboardService} from '../../../../../../service/pulse/dashboard/dashboard-service';
import {interval, Subscription, switchMap} from 'rxjs';
import {KeycloakProfile} from 'keycloak-js';
import {AuthService} from '../../../../../../auth/service/auth-service';

@Component({
  selector: 'app-pdb-create-cells',
  imports: [
    FormsModule,
    DatePipe
  ],
  templateUrl: './pdb-create-cells.html',
  styleUrl: './pdb-create-cells.css',
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class PdbCreateCells implements OnInit, OnDestroy {

  // Pulse Dashboard Create Cells
  rat = signal('');
  rats: RatDto[] = [];

  areaType = signal<string | undefined>('');
  areaTypes: AreaTypeDto[] = [];

  granularity = signal('');
  granularities: GranularityDto[] = [];

  selectedDate = signal<string>('');

  @Input() open: boolean = false;
  @Output() closed = new EventEmitter<void>();

  creatingWorstCells = signal(false);
  // worstCellCreationStatus: boolean = false;
  private statusPollingSub?: Subscription;
  totalItems = signal(1);
  executedItems = signal(0);

  userProfile: KeycloakProfile = {};

  constructor(private alertService: AlertService,
              private ratService: RatService,
              private areaTypeService: AreaTypeService,
              private granularityService: GranularityService,
              private dashboardService: DashboardService,
              private authService: AuthService) {
    // this.getWorstCellCreationStatus();
    this.getAllGranularities();
    this.getAllRats();
    this.getAllAreaTypes();
  }

  ngOnInit(): void {
    this.getUserProfile();
  }

  ngOnDestroy(): void {
    this.statusPollingSub?.unsubscribe();
  }



  onCancel(): void {
    this.closed.emit();
    this.rat.set(this.rats[0].name!);
    this.areaType.set(this.areaTypes[0].name);
    this.granularity.set(this.granularities[0].name!);
    this.selectedDate.set('');
  }

  createWorstCells() {
    this.creatingWorstCells.set(true);
    this.dashboardService.createWorstCellsByRatAndAreaType('week', this.areaType()!, this.selectedDate(), this.rat(), this.granularity())    //TODO: Modify week for better arguments
      .subscribe({
        next: (result) => {
          this.creatingWorstCells.set(false);
          this.alertService.success(`Worst Cells created!`);
          this.onCancel();
        }, error: (err) => {
          console.log(err);
          this.alertService.error(`Error creating Worst Cells (${err.status})`);
          this.creatingWorstCells.set(false);
        }
      });
  }

  //================== GETTERS =============================

  getAllRats(): void {
    this.ratService.getAllRats().subscribe({
      next: data => {
        this.rats = data;
        this.rat.set(this.rats[0].name!);
      }, error: err => {
        console.error(err);
        this.alertService.error('Error retrieving RATs');
      }
    })
  }

  getAllGranularities() {
    this.granularityService.getAllGranularities().subscribe({
      next: data => {
        this.granularities = data;
        this.granularity.set(this.granularities[0].name!)
      }
    })
  }

  getAllAreaTypes() {
    this.areaTypeService.getAllAreaTypes().subscribe({
        next: data => {
          this.areaTypes = data;
          this.areaType.set(this.areaTypes[0].name!);
        }, error: error => {
          console.log(error);
          this.alertService.error(`Error getting Area-types! (${error.status}:${error.statusText})`);
        }
      }
    )
  }

  //================== FILTERS =========================

  selectRat(ratName: string) {
    this.rat.set(ratName);
    const _rat = this.rats.find(r => r.name! === ratName);
    if (_rat) {
    } else {
      this.alertService.error(`Error retrieving Standard KPI for ${ratName}`);
    }
  }

  selectAreaType(areaType: string) {
    this.areaType.set(areaType);
  }

  selectGranularity(granularity: string) {
    this.granularity.set(granularity);
  }

  onSelectedDateChange(event: any) {
    // Cally emits event.target.value
    this.selectedDate.set(event.target.value);
  }

  //============= UTILITY ================================

  startWorstCellCreationStatusPolling() {
    this.statusPollingSub = interval(3000)
      .pipe(
        switchMap(() => this.dashboardService.getWorstCellCreationStatus())
      ).subscribe({
        next: (result) => {
          this.creatingWorstCells.set(result.status);
          this.totalItems.set(result.totalItems);
          this.executedItems.set(result.executedItems);
        }, error: (err) => {
          console.log(err);
          this.alertService.error(`Error retrieving Worst Cells creation status (${err.status})`);
        }
      })
  }

  isInputsValid() {
    return !(this.rat() != '' && this.areaType() != '' && this.granularity() != '' && this.selectedDate() != '');
  }

  //============== USER VALIDATION =============================

  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
    if (this.authService.hasRole('PULSE_CREATE')) {
      this.startWorstCellCreationStatusPolling();
    }
  }

  hasAnyRole(roles:string[]){
    return this.authService.hasAnyRole(roles);
  }
}
