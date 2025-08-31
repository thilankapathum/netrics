import { Component } from '@angular/core';
import {SidebarComponent} from './sidebar/sidebar.component';
import {ContentComponent} from './content/content.component';
import {Navbar} from './navbar/navbar';

@Component({
  selector: 'app-layout',
  imports: [
    SidebarComponent,
    ContentComponent,
    Navbar
  ],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent {

}
