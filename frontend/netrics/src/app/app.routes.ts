import {Routes} from '@angular/router';
import {PulseComponent} from './layout/content/pages/pulse/pulse.component';
import {CellAnalysis} from './layout/content/pages/pulse/cell-analysis/cell-analysis';
import {LayoutComponent} from './layout/layout.component';
import {ContentComponent} from './layout/content/content.component';

export const routes: Routes = [
  // {path: '', redirectTo: 'pulse', pathMatch: 'full'},
  // {path: 'cell', component: CellAnalysis, title: 'Pulse'},
  // {path: 'pulse', component: PulseComponent, title: 'Pulse'},
  // {path: '**', component: PulseComponent},

  {
    path: '',
    component: LayoutComponent,
    children: [
      {path: '', redirectTo: 'pulse', pathMatch: 'full'},
      {
        path: 'pulse',
        component: ContentComponent,
        children: [
          {path: 'cell/:rat/:cell-name', component: CellAnalysis},
          {path: 'cell', component: CellAnalysis},
          {path: '', component: PulseComponent},
        ]
      },
      {path: 'surge', component: PulseComponent},
      {path: 'beam', component: CellAnalysis},
    ]
  },
  {path: '**', redirectTo: ''},
];
