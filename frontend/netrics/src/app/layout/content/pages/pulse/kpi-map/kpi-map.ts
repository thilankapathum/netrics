import {Component, computed, CUSTOM_ELEMENTS_SCHEMA, OnInit, signal} from '@angular/core';
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
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {UserAreaService} from '../../../../../service/pulse/user-area-service';
import {StandardkpiService} from '../../../../../service/pulse/ltefdd/standardkpi.service';
import {DateService} from '../../../../../service/pulse/date-service';
import {MapCellThresholdService} from '../../../../../service/pulse/map-cell/map-cell-threshold-service';
import {MapCellThrSetAndThresholds} from '../../../../../models/pulse/map-cell/MapCellThrSetAndThresholds';
import {CellMapLegend} from './cell-map-legend/cell-map-legend';
import {CellMapLegendEdit} from './cell-map-legend-edit/cell-map-legend-edit';
import {CellMapCellAnalysis} from './cell-map-cell-analysis/cell-map-cell-analysis';
import {SharedService} from '../../../../../service/pulse/shared-service';
import {SiteDto} from '../../../../../models/pulse/SiteDto';
import {SiteService} from '../../../../../service/pulse/site-service';
import {BandDto} from '../../../../../models/pulse/BandDto';
import {BandService} from '../../../../../service/pulse/band-service';
import {CellMapEngParaModify} from './cell-map-eng-para-modify/cell-map-eng-para-modify';

@Component({
  selector: 'app-kpi-map',
  imports: [
    SectorMap,
    ReactiveFormsModule,
    RouterLink,
    FormsModule,
    CellMapLegend,
    CellMapLegendEdit,
    CellMapCellAnalysis,
    CellMapEngParaModify
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

  band = signal<string | undefined>(undefined);
  bands : BandDto[] = [];
  // filterByBands:boolean = false;
  activeBand = signal<string>('');

  date = signal<string | undefined>(undefined);

  mapCellThrSetAndThresholds = signal<MapCellThrSetAndThresholds | undefined>(undefined);

  isAdmin = signal<boolean>(true);
  isThresholdAvailable = signal<boolean>(true);

  selectedCellName = signal<string>('');

  searchSite = signal('');
  selectedSite = signal<SiteDto | undefined>(undefined);
  filteredSites = signal<SiteDto[]>([]);

  isThresholdsEditable = computed(() =>
    (this.isAdmin() && this.isUserAdmin()) || (!this.isAdmin() && this.isUserAuthorized()));

  loadingRats: boolean = false;
  loadingGranularity: boolean = false;
  loadingStandardKpis: boolean = false;
  loadingAreaTypes: boolean = false;
  loadingAreas: boolean = false;
  savingThresholds: boolean = false;
  showSiteLabels:boolean = true;

  showCellMapLegendEditModal = signal<boolean>(false);
  showCellMapCellAnalysisModal = signal<boolean>(false);
  showCellMapEngParaModifyModal = signal<boolean>(false);


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
              private mapCellThresholdService: MapCellThresholdService,
              private sharedService: SharedService,
              private siteService:SiteService,
              private bandService: BandService) {
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
    this.selectKpi(this.standardKpi()!, this.rat()!, this.granularity()!);
  }

  setGranularity(granularity: 'day-average' | 'busy-hour') {
    this.granularity.set(granularity);
    this.getThrSetAndThresholdsByThrSetId(this.standardKpi()!,this.rat()!,this.granularity()!,this.isAdmin());
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
          if (this.sharedService.areaType() != '') {
            this.areaType.set(this.sharedService.areaType());
          } else if (this.userArea() != null) {
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
        if (this.sharedService.area() != '') {
          this.area.set(this.sharedService.area());
        } else if (this.userArea() != null) {
          this.area.set(this.userArea()?.name);
          this.userArea.set(undefined);   // Clear userArea details after initial loading
        } else {
          this.area.set(this.areas.at(0)?.name);
        }

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
        if (this.sharedService.selectedGranularity() != '') {
          this.granularity.set(this.sharedService.selectedGranularity());
        } else {
          this.granularity.set(this.granularities[0]?.name);
        }
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

        if (this.sharedService.selectedRat() !== '') {
          this.rat.set(this.sharedService.selectedRat());

        } else {
          this.rat.set(this.rats[0]?.name);
        }

        this.getAllStandardKpi(this.rat()!);
        this.loadingRats = false;
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error retrieving RATs ${error.status}:${error.statusText}`);
        this.loadingRats = false;
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
          if (this.sharedService.selectedStandardKpi() != '' && this.rat() === this.sharedService.selectedRat()) {
            this.standardKpi.set(this.sharedService.selectedStandardKpi());
            // this.sharedService.selectedRat.set('');
          } else {
            this.standardKpi.set(this.standardKpis[0].kpiName);
          }
          this.getLatestDate(this.rat()!, this.granularity()!);
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

  getLatestDate(ratName: string, granularityName: string) {
    this.dateService.getLatestDate(ratName, granularityName).subscribe({
      next: data => {
        const formattedDate = data.split('T')[0];
        if (this.sharedService.cellMapDate() != '') {
          this.date.set(this.sharedService.cellMapDate());
        } else {
          this.date.set(formattedDate);
        }
        this.getBandsByRat(this.rat()!);
        this.sharedService.clearAll();
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error retrieving latest date. ${error.status}:${error.statusText}`);
      }
    })
  }

  getBandsByRat(ratName:string){
    this.bandService.getBandsByRatName(ratName).subscribe({
      next: data => {
        this.bands = data;
        console.log(this.bands);
      }, error: error => {
        console.log(error);
        this.alertService.error(`Error retrieving Bands. ${error.status}:${error.statusText}`);
      }
    })
  }

  getThrSetAndThresholdsByThrSetId(standardKpiName: string, ratName: string, granularityName: string, isAdmin: boolean) {
    this.mapCellThrSetAndThresholds.set(undefined);
    this.mapCellThresholdService.getThrSetAndThresholds(standardKpiName, ratName, granularityName, isAdmin).subscribe({
      next: data => {
        this.mapCellThrSetAndThresholds.set(data);
        this.isThresholdAvailable.set(true);
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

  onActiveBandChange(band: string): void {
    this.activeBand.set(band);
  }

  // selectBand(bandName:string):void{
  //   this.band.set(bandName);
  //   console.log('Selected Band-kpimap', this.band());
  // }

  onLegendTypeChange(type: boolean) {
    this.isAdmin.set(type);
    this.getThrSetAndThresholdsByThrSetId(this.standardKpi()!, this.rat()!, this.granularity()!, this.isAdmin());
  }

  onDateChange(event: any) {
    // Cally emits event.target.value
    this.date.set(event.target.value);
  }

  onSearchSite(value:string){
    this.searchSite.set(value);

    if (value.length > 2){
      this.siteService.searchSites(value).subscribe({
        next: data=>{
          this.filteredSites.set(data);
        }, error: error => {
          console.log(error);
          this.alertService.error(`Error searching site. ${error.status} ${error.statusText}`);
        }
      });
    } else{
      this.filteredSites.set([]);
    }
  }

  selectSite(site:SiteDto){
    // console.log('selected site', site)
    this.selectedSite.set(site);
  }

  extractStandardKpiLabel(kpiName: string): string | undefined {
    return this.standardKpis.find(kpi => kpi.kpiName === kpiName)?.label;
  }

  isUserAdmin() {
    return this.authService.hasRole('PULSE_DELETE')
  }

  isUserAuthorized() {
    return this.userProfile.id === this.mapCellThrSetAndThresholds()?.thrSet?.userId;
  }

  isThresholdsCustomizable(): boolean {
    return this.authService.hasRole('PULSE_UPDATE')
  }

  //-------------------- OPEN MODAL -----------------------------
  openCellMapLegendEditModal(): void {
    this.showCellMapLegendEditModal.set(true);
  }

  openCellMapCellAnalysisModal(): void {
    this.showCellMapCellAnalysisModal.set(true);
    this.sharedService.areaType.set(this.areaType()!);
    this.sharedService.area.set(this.area()!);
    this.sharedService.cellMapDate.set(this.date()!);
    this.sharedService.selectedRat.set(this.rat()!);
    this.sharedService.selectedGranularity.set(this.granularity()!);
    this.sharedService.selectedStandardKpi.set(this.standardKpi()!);
    this.sharedService.cellMapDate.set(this.date()!);
  }

  openCellMapEngParaModifyModal(): void {
    this.showCellMapEngParaModifyModal.set(true);
  }

  //------------------- CLOSE MODAL ----------------------------
  closeCellMapLegendEditModal(): void {
    this.showCellMapLegendEditModal.set(false);
  }

  closeCellMapCellAnalysisModal(): void {
    this.showCellMapCellAnalysisModal.set(false);
    this.sharedService.clearAll();
  }

  closeCellMapEngParaModifyModal(): void {
    this.showCellMapEngParaModifyModal.set(false);
  }

  saveThrSetAndThresholds(thrSetAndThresholds: MapCellThrSetAndThresholds) {
    this.savingThresholds = true;
    if (thrSetAndThresholds.thrSet?.id == 0) {
      this.mapCellThresholdService.createThrSetAndThresholds(thrSetAndThresholds).subscribe({
        next: data => {
          this.mapCellThrSetAndThresholds.set(data);
          this.alertService.success(`Threshold Template Successfully saved!`);
          this.savingThresholds = false;
        }, error: error => {
          console.log(error);
          this.alertService.error(`Error creating Threshold Set ${error.status}:${error.statusText}`);
          this.savingThresholds = false;
        }
      })
    } else {
      this.mapCellThresholdService.updateThresholds(thrSetAndThresholds).subscribe({
        next: data => {
          this.mapCellThrSetAndThresholds.set(data);
          this.alertService.success(`Threshold Template Successfully updated!`);
          this.savingThresholds = false;
        }, error: error => {
          console.log(error);
          this.alertService.error(`Error updating thresholds ${error.status}:${error.statusText}`);
          this.savingThresholds = false;
        }
      })
    }

  }

  onCellSelection(cellName: string) {
    this.selectedCellName.set(cellName);
  }

  onShowSiteLabelsChange(showSiteLabels: boolean): void {
    this.showSiteLabels = showSiteLabels;
  }

  isEngineeringParaEditable():boolean {
    return this.authService.hasRole('PULSE_UPDATE');
  }

  // onFilterByBandsChange(filter: boolean): void {
  //   this.filterByBands = filter;
  //   if (!filter) {
  //     this.band.set('');    // clear the band signal when filter is disabled
  //   }
  // }
}
