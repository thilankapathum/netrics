import {Component, CUSTOM_ELEMENTS_SCHEMA, OnInit, signal} from '@angular/core';
import {SectorMap} from './sector-map/sector-map';
import {RatDto} from '../../../../../models/pulse/RatDto';
import {GranularityDto} from '../../../../../models/pulse/GranularityDto';
import {AreaTypeDto} from '../../../../../models/pulse/AreaTypeDto';
import {AreaDto} from '../../../../../models/pulse/AreaDto';
import {KeycloakProfile} from 'keycloak-js';
import {StandardKpiDto} from '../../../../../models/pulse/StandardKpiDto';
import {RatService} from '../../../../../service/pulse/rat-service';
import {GranularityService} from '../../../../../service/pulse/granularity-service';
import {AreaTypeService} from '../../../../../service/pulse/area-type-service';
import {AreaService} from '../../../../../service/pulse/area-service';
import {AlertService} from '../../../../../components/alert/alert.service';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../../../../../auth/service/auth-service';
import {DatePipe} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {UserAreaService} from '../../../../../service/pulse/user-area-service';
import {StandardkpiService} from '../../../../../service/pulse/ltefdd/standardkpi.service';
import {DateService} from '../../../../../service/pulse/date-service';
import {MapCellThresholdService} from '../../../../../service/pulse/map-cell/map-cell-threshold-service';
import {MapCellThrSetAndThresholds} from '../../../../../models/pulse/map-cell/MapCellThrSetAndThresholds';
import {CellMapLegend} from './cell-map-legend/cell-map-legend';
import {CellMapLegendEdit} from './cell-map-legend-edit/cell-map-legend-edit';

@Component({
  selector: 'app-kpi-map',
  imports: [
    SectorMap,
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    FormsModule,
    CellMapLegend,
    CellMapLegendEdit
  ],
  templateUrl: './kpi-map.html',
  styleUrl: './kpi-map.css',
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class KpiMap implements OnInit {

  rat = signal<string | undefined>(undefined);
  rats: RatDto[] = [];

  granularity = signal<string | undefined>(undefined);
  granularities: GranularityDto[] = [];

  areaType = signal<string | undefined>('');
  areaTypes: AreaTypeDto[] = [];

  area = signal<string | undefined>('')
  areas: AreaDto[] = [];

  userArea = signal<AreaDto | undefined>(undefined);
  userProfile: KeycloakProfile = {};

  standardKpi = signal<string | undefined>(undefined);
  standardKpis: StandardKpiDto[] = [];

  date = signal<string | undefined>(undefined);

  mapCellThrSetAndThresholds = signal<MapCellThrSetAndThresholds | undefined>(undefined);

  isAdmin = signal<boolean>(true);
  isThresholdAvailable = signal<boolean>(true);

  loadingRats: boolean = false;
  loadingGranularity: boolean = false;
  loadingStandardKpis: boolean = false;
  loadingAreaTypes: boolean = false;
  loadingAreas: boolean = false;

  showCellMapLegendEditModal: boolean = false;

  constructor(private ratService: RatService,
              private granularityService: GranularityService,
              private areaTypeService: AreaTypeService,
              private areaService: AreaService,
              private alertService: AlertService,
              private router: Router,
              private authService: AuthService,
              private userAreaService: UserAreaService,
              private standardKpiService: StandardkpiService,
              private dateService: DateService,
              private mapCellThresholdService: MapCellThresholdService,) {
  }

  ngOnInit() {
    this.getUserProfile();
    // this.getRats();
    this.getGranularities();
  }

  loadingAll() {
    return this.loadingAreaTypes || this.loadingAreas || this.loadingRats || this.loadingGranularity || this.loadingStandardKpis;
  }


  //------------ FILTERS ---------------------------

  selectAreaType(areaType: string) {
    this.areaType.set(areaType);
    this.getAreasByAreaType(this.areaType()!);
  }

  selectArea(areaName: string) {
    this.area.set(areaName);
    // this.cdr.detectChanges(); // Force change detection
    // this.getBasicKpi(this.selectedRat());
    this.selectKpi(this.standardKpi()!, this.rat()!, this.granularity()!);
  }

  setGranularity(granularity: 'day-average' | 'busy-hour') {
    this.granularity.set(granularity);
    // this.getWorstCellsAndKpiTrends(this.bandWise, this.selectedBand(), this.selectedStandardKpi(), this.selectedKpiTrendPeriod(), this.selectedRat(), this.excludeZeroes, this.selectedGranularity(), this.area()!, this.aggregation());
  }

  setRat(rat: string) {
    this.rat.set(rat);
    this.getAllStandardKpi(this.rat()!);
  }

  //----------- GETTERS ----------------------------------


  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
    // if (this.authService.hasRole('PULSE_CREATE')) {
    //   this.getCellCountWithMissingInfo();
    // }
    this.getAreaByUserId(this.userProfile.id!);
  }

  getAreaByUserId(userId: string) {
    this.userAreaService.findByUserId(userId).subscribe({
      next: data => {
        this.userArea.set(data);
        // this.ngOnInitRemaining();
        this.getAreaTypes();

      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting AreaByUserId)`);
      }
    })
  }

  getAreaTypes() {
    this.loadingAreaTypes = true;
    this.areaTypeService.getAllAreaTypes().subscribe({
        next: data => {
          this.areaTypes = data;
          const districtsAreaType = this.areaTypes.find(at => at.name === 'District');
          // if (this.sharedService.areaType() != '') {
          //   this.areaType.set(this.sharedService.areaType());
          // } else
          if (this.userArea() != null) {
            this.areaType.set(this.userArea()?.areaTypeName);
          } else {
            this.areaType.set(districtsAreaType?.name);
          }
          this.getAreasByAreaType(this.areaType()!);
          this.loadingAreaTypes = false;
        }, error: error => {
          console.log(error);
          this.alertService.error(`Error getting Area-types! (${error.status}:${error.statusText})`);
          this.loadingAreaTypes = false;
        }
      }
    )
  }


  getAreasByAreaType(areaTypeName: string) {
    this.loadingAreas = true;
    this.areaService.getAreasByAreaTypes(areaTypeName).subscribe({
      next: data => {
        this.areas = data;
        // if (this.sharedService.area() != '') {
        //   this.area.set(this.sharedService.area());
        //   this.sharedService.area.set('');
        //   this.sharedService.areaType.set('');
        // } else
        if (this.userArea() != null) {
          this.area.set(this.userArea()?.name);
          this.userArea.set(undefined);   // Clear userArea details after initial loading
        } else {
          this.area.set(this.areas.at(0)?.name);
        }
        // this.getAllStandardKpi(this.rat()!);

        this.loadingAreas = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error getting Areas! (${error.status}:${error.statusText})`);
        this.loadingAreas = false;
      }
    })
  }

  getGranularities() {
    this.loadingGranularity = true;
    this.granularityService.getAllGranularities().subscribe({
      next: data => {
        this.granularities = data;
        this.granularity.set(this.granularities[0]?.name);
        this.getRats();
        this.loadingGranularity = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error retrieving granularities ${error.status}:${error.statusText}`);
        this.loadingGranularity = false;
      }
    })
  }

  getRats() {
    this.loadingRats = true;
    this.ratService.getAllRats().subscribe({
      next: data => {
        this.rats = data;
        this.rat.set(this.rats[0]?.name);
        // this.setRat(this.rats[0].name);
        this.getLatestDate(this.rat()!, this.granularity()!);
        this.getAllStandardKpi(this.rat()!);
        this.loadingRats = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error retrieving RATs ${error.status}:${error.statusText}`);
        this.loadingRats = false;
      }
    })
  }

  getLatestDate(ratName: string, granularityName: string) {
    this.dateService.getLatestDate(ratName, granularityName).subscribe({
      next: data => {
        const formattedDate = data.split('T')[0];
        this.date.set(formattedDate);
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error retrieving latest date`);
      }
    })
  }

  getAllStandardKpi(ratName: string) {
    this.loadingStandardKpis = true;
    this.standardKpis = [];
    this.standardKpiService.getAllStandardKpi(ratName).subscribe({
      next: data => {
        this.standardKpis = data;
        if (this.standardKpis.length > 0) {
          // if (this.sharedService.selectedStandardKpi() != '' && this.selectedRat() === this.sharedService.selectedRat()) {
          //   this.selectedStandardKpi.set(this.sharedService.selectedStandardKpi());
          // } else {
          //   this.selectedStandardKpi.set(this.standardKpis[0].kpiName!);
          // }
          this.standardKpi.set(this.standardKpis[0].kpiName)
          this.selectKpi(this.standardKpi()!, ratName, this.granularity()!);    // Getting Worst-cells and Trend-data
          this.getThrSetAndThresholdsByThrSetId(this.standardKpi()!, this.rat()!, this.granularity()!, this.isAdmin())
        } else {
          this.alertService.error("KPI are unavailable for the RAT");
        }
        this.loadingStandardKpis = false;
      }, error: error => {
        console.log("Error getAllStandardKpi:");
        console.error(error);
        this.alertService.error("Standard KPI retrieval failed");
        this.loadingStandardKpis = false;
      }
    })
  }

  getThrSetAndThresholdsByThrSetId(standardKpiName: string, ratName: string, granularityName: string, isAdmin: boolean) {
    this.mapCellThrSetAndThresholds.set(undefined);
    this.mapCellThresholdService.getThrSetAndThresholds(standardKpiName, ratName, granularityName, isAdmin).subscribe({
      next: data => {
        this.mapCellThrSetAndThresholds.set(data);
        this.isThresholdAvailable.set(true);
        // console.log(data);
      }, error: error => {
        console.log(error);
        if (error.status === 404) {
          this.isThresholdAvailable.set(false);
        } else {
          this.alertService.error(`Error retrieving thresholds ${error.status}:${error.statusText}`);
        }
      }
    })
  }

  async selectKpi(kpi: string, ratName: string, granularityName: string) {
    // await this.getWorstCellsAndKpiTrends(this.bandWise, this.selectedBand(), kpi, this.selectedKpiTrendPeriod(), ratName, this.excludeZeroes, granularityName, this.area()!, this.aggregation());
    this.standardKpi.set(kpi);
    this.getThrSetAndThresholdsByThrSetId(kpi, ratName, this.granularity()!, this.isAdmin());
  }

  onLegendTypeChange(type: boolean) {
    this.isAdmin.set(type);
    this.getThrSetAndThresholdsByThrSetId(this.standardKpi()!, this.rat()!, this.granularity()!, this.isAdmin());
  }

  onDateChange(event: any) {
    // Cally emits event.target.value
    this.date.set(event.target.value);
  }

  //-------------------- OPEN MODAL -----------------------------
  openCellMapLegendEditModal(): void {
    this.showCellMapLegendEditModal = true;
  }

  //------------------- CLOSE MODAL ----------------------------
  closeCellMapLegendEditModal(): void {
    this.showCellMapLegendEditModal = false;
  }
}
