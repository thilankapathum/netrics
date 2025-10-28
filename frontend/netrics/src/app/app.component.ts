import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {LayoutComponent} from './layout/layout.component';
import {AlertComponent} from './components/alert/alert-component/alert-component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, LayoutComponent, AlertComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'netrics';
}
