import { Component, signal } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { KeycloakProfile } from 'keycloak-js';
import { AlertService } from '../../../../../../components/alert/alert.service';
import { AuthService } from '../../../../../../auth/service/auth-service';
import { CacheService } from '../../../../../../service/pulse/cache-service';
import { RatDto } from '../../../../../../models/pulse/RatDto';
import { GranularityDto } from '../../../../../../models/pulse/GranularityDto';
import { RatService } from '../../../../../../service/pulse/rat-service';
import { GranularityService } from '../../../../../../service/pulse/granularity-service';

@Component({
  selector: 'app-ps-evict-cache',
  imports: [
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './ps-evict-cache.html',
  styleUrl: './ps-evict-cache.css'
})
export class PsEvictCache {

  userProfile: KeycloakProfile = {};

  rat = signal('');
  rats: RatDto[] = [];

  granularity = signal('');
  granularities: GranularityDto[] = [];

  isEvictByGranularity = signal<boolean>(false);
  evictingCache: boolean = false;
  evictingAllCaches: boolean = false;

  constructor(private alertService: AlertService,
              private authService: AuthService,
              private cacheService: CacheService,
              private ratService: RatService,
              private granularityService: GranularityService) {
    this.getAllRats();
    this.getAllGranularities();
  }

  evictCaches() {
    this.evictingCache = true;
    if (this.isEvictByGranularity()) {
      this.cacheService.evictByRatAndGranularity(this.rat(), this.granularity()).subscribe({
        next: result => {
          this.alertService.success(`${result}`);
          this.evictingCache = false;
        }, error: error => {
          console.error(error);
          this.alertService.error(`Error evicting cache for ${this.rat()}-${this.granularity()} - ${error.statusText}`);
          this.evictingCache = false;
        }
      });
    } else {
      this.cacheService.evictByRatName(this.rat()).subscribe({
        next: result => {
          this.alertService.success(`${result}`);
          this.evictingCache = false;
        }, error: error => {
          console.error(error);
          this.alertService.error(`Error evicting cache for ${this.rat()} - ${error.statusText}`);
          this.evictingCache = false;
        }
      });
    }
  }

  evictAllCaches() {
    this.evictingAllCaches = true;
    this.cacheService.evictAllCaches().subscribe({
      next: result => {
        this.alertService.success(`Evicted ${result} caches successfully!`);
        this.evictingAllCaches = false;
      }, error: error => {
        console.error(error);
        this.alertService.error(`Error evicting caches - ${error.statusText}`);
        this.evictingAllCaches = false;
      }
    });
  }

  //================== GETTERS =============================

  getAllRats(): void {
    this.ratService.getAllRats().subscribe({
      next: data => {
        this.rats = data;
        if (this.rats.length > 0) {
          this.rat.set(this.rats[0].name!);
        }
      }, error: err => {
        console.error(err);
        this.alertService.error('Error retrieving RATs');
      }
    });
  }

  getAllGranularities() {
    this.granularityService.getAllGranularities().subscribe({
      next: data => {
        this.granularities = data;
        if (this.granularities.length > 0) {
          this.granularity.set(this.granularities[0].name!);
        }
      }
    });
  }

  //================== FILTERS =========================

  selectRat(ratName: string) {
    this.rat.set(ratName);
  }

  selectGranularity(granularity: string) {
    this.granularity.set(granularity);
  }

  //============== USER VALIDATION =============================

  async getUserProfile() {
    this.userProfile = await this.authService.getUserProfile();
  }

  hasAnyRole(roles: string[]) {
    return this.authService.hasAnyRole(roles);
  }
}
