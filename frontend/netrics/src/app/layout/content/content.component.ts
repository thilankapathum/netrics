import { Component } from '@angular/core';
import {PulseComponent} from './pages/pulse/pulse.component';

@Component({
  selector: 'app-content',
  imports: [
    PulseComponent
  ],
  templateUrl: './content.component.html',
  styleUrl: './content.component.css'
})
export class ContentComponent {

}
