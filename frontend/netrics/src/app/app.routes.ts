import {Routes} from '@angular/router';
import {PulseComponent} from './layout/content/pages/pulse/pulse.component';
import {CellAnalysis} from './layout/content/pages/pulse/cell-analysis/cell-analysis';
import {LayoutComponent} from './layout/layout.component';
import {ContentComponent} from './layout/content/content.component';
import {authGuard} from './auth/guards/auth-guard';
import {roleGuard} from './auth/guards/role-guard';
import {UnauthorizedComponent} from './layout/content/pages/unauthorized/unauthorized-component/unauthorized-component';
import {BeamComponent} from './layout/content/pages/beam/beam-component/beam-component';
import {HomeComponent} from './layout/content/pages/home/home-component/home-component';
import {DashboardComponent} from './layout/content/pages/pulse/dashboard/dashboard-component';
import {KpiReports} from './layout/content/pages/pulse/kpi-reports/kpi-reports';
import {PulseSettings} from './layout/content/pages/pulse/pulse-settings/pulse-settings';
import {KpiMap} from './layout/content/pages/pulse/kpi-map/kpi-map';

export const routes: Routes = [
  // {path: '', redirectTo: 'pulse', pathMatch: 'full'},
  // {path: 'cell', component: CellAnalysis, title: 'Pulse'},
  // {path: 'pulse', component: PulseComponent, title: 'Pulse'},
  // {path: '**', component: PulseComponent},

  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      // {path: '', redirectTo: 'pulse', pathMatch: 'full'},
      {
        path: '',
        component: ContentComponent,
        children: [
          {
            path: '',
            component: HomeComponent,
          },
          {path: 'unauthorized', component: UnauthorizedComponent}
        ]
      },
      {
        path: 'pulse',
        component: ContentComponent,
        children: [
          // {path: 'cell/:rat/:cell-name', component: CellAnalysis},
          {
            path: 'cell',
            canActivate: [authGuard, roleGuard(['PULSE_READ'])],
            component: CellAnalysis
          },
          {
            path: 'dashboard',
            canActivate: [authGuard, roleGuard(['PULSE_READ'])],
            component: DashboardComponent
          },
          {
            path: 'kpi-reports',
            canActivate: [authGuard, roleGuard(['PULSE_UPDATE'])],
            component: KpiReports
          },
          {
            path: 'map',
            canActivate: [authGuard, roleGuard(['PULSE_DELETE'])],
            component: KpiMap
          },
          {
            path: 'settings',
            canActivate: [authGuard, roleGuard(['PULSE_DELETE'])],
            component: PulseSettings
          },
          {
            path: '',
            canActivate: [authGuard, roleGuard(['PULSE_READ'])],
            component: PulseComponent
          },
          {path: 'unauthorized', component: UnauthorizedComponent},
        ]
      },
      {path: 'surge', component: BeamComponent, canActivate: [authGuard, roleGuard(['SURGE_READ'])]},
      {path: 'beam', component: BeamComponent, canActivate: [authGuard, roleGuard(['BEAM_READ'])]},
      {path: 'mark', component: BeamComponent, canActivate: [authGuard, roleGuard(['MARK_READ'])]},
      {path: 'cortex', component: BeamComponent, canActivate: [authGuard, roleGuard(['CORTEX_READ'])]},
    ]
  },
  {path: 'unauthorized', component: UnauthorizedComponent},
  {path: '**', redirectTo: 'unauthorized', pathMatch: 'full'},
];
