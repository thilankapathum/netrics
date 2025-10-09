import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import {provideCharts, withDefaultRegisterables} from 'ng2-charts';
import { provideHttpClient } from '@angular/common/http';
import {routes} from './app/app.routes';
import {provideRouter} from '@angular/router';


bootstrapApplication(AppComponent, {
  providers: [provideHttpClient(), provideRouter(routes),
    provideCharts(withDefaultRegisterables())],
}).catch((err) => console.error(err));
