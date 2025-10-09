import { Component } from '@angular/core';
import {PulseComponent} from './pages/pulse/pulse.component';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-content',
  imports: [
    PulseComponent,
    RouterOutlet
  ],
  templateUrl: './content.component.html',
  styleUrl: './content.component.css'
})
export class ContentComponent {

}
