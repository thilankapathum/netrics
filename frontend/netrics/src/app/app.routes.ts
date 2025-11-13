import {Routes} from '@angular/router';
import {PulseComponent} from './layout/content/pages/pulse/pulse.component';
import {CellAnalysis} from './layout/content/pages/pulse/cell-analysis/cell-analysis';
import {LayoutComponent} from './layout/layout.component';
import {ContentComponent} from './layout/content/content.component';
import {authGuard} from './auth/guards/auth-guard';
import {roleGuard} from './auth/guards/role-guard';
import {UnauthorizedComponent} from './layout/content/pages/unauthorized/unauthorized-component/unauthorized-component';

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
      {path: '', redirectTo: 'pulse', pathMatch: 'full'},
      {
        path: 'pulse',
        component: ContentComponent,
        children: [
          // {path: 'cell/:rat/:cell-name', component: CellAnalysis},
          {
            path: 'cell',
            canActivate: [authGuard, roleGuard(['ADMIN'])],
            component: CellAnalysis
          },
          {
            path: '',
            canActivate: [authGuard, roleGuard(['ADMIN'])],
            component: PulseComponent
          },
          {path: 'unauthorized', component: UnauthorizedComponent},
        ]
      },
      {path: 'surge', component: PulseComponent, canActivate: [authGuard]},
      {path: 'beam', component: CellAnalysis, canActivate: [roleGuard(['ADMIN'])]},
    ]
  },
  {path: 'unauthorized', component: UnauthorizedComponent},
  {path: '**', redirectTo: ''},
];
