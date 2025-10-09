import {Component, OnInit, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-cell-analysis',
  imports: [],
  templateUrl: './cell-analysis.html',
  styleUrl: './cell-analysis.css'
})
export class CellAnalysis  {

  rat = signal('');
  cellName = signal('');

  constructor(private activatedRoute: ActivatedRoute) {
    this.rat.set(this.activatedRoute.snapshot.params['rat']);
    this.cellName.set(this.activatedRoute.snapshot.params['cell-name']);
  }

}
