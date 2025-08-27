import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import {provideCharts, withDefaultRegisterables} from 'ng2-charts';
import { provideHttpClient } from '@angular/common/http';


bootstrapApplication(AppComponent, {
  providers: [provideHttpClient(),
    provideCharts(withDefaultRegisterables())],
}).catch((err) => console.error(err));
