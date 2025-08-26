import { Component } from '@angular/core';
import {SidebarComponent} from './sidebar/sidebar.component';
import {ContentComponent} from './content/content.component';

@Component({
  selector: 'app-layout',
  imports: [
    SidebarComponent,
    ContentComponent
  ],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent {

}
