import {Component, EventEmitter, Input, Output, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AntennaService} from '../../../../../../service/beam/antenna-service';
import {ElectricalTiltService} from '../../../../../../service/beam/electrical-tilt-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {AntennaDto} from '../../../../../../models/beam/AntennaDto';
import {antennaToNode, tiltToNode, TreeNodeVm} from '../tree-node.model';

@Component({
  selector: 'app-tree-node',
  imports: [CommonModule, TreeNode],
  templateUrl: './tree-node.html',
  styleUrl: './tree-node.css'
})
export class TreeNode {
  @Input() node!: TreeNodeVm;
  @Input() canAdd = false;

  @Output() select = new EventEmitter<TreeNodeVm>();
  @Output() addChild = new EventEmitter<TreeNodeVm>();

  expanded = signal(false);
  loadingChildren = signal(false);

  constructor(private antennaService: AntennaService,
              private electricalTiltService: ElectricalTiltService,
              private alertService: AlertService) {
  }

  get isLeaf(): boolean {
    return this.node.type === 'tilt';
  }

  toggle(): void {
    if (this.isLeaf) return;

    this.expanded.update(v => !v);
    if (this.expanded() && this.node.children === undefined) {
      this.loadChildren();
    }
  }

  private loadChildren(): void {
    this.loadingChildren.set(true);

    if (this.node.type === 'sector') {
      this.antennaService.getBySectorName(this.node.label).subscribe({
        next: antennas => {
          this.node.children = antennas.map(antennaToNode);
          this.loadingChildren.set(false);
        },
        error: err => {
          this.alertService.httpError('Failed to load antennas', err);
          this.loadingChildren.set(false);
        }
      });
    } else if (this.node.type === 'antenna') {
      const antenna = this.node.data as AntennaDto;
      this.electricalTiltService.getByAntennaId(antenna.id!).subscribe({
        next: tilts => {
          this.node.children = tilts.map(tiltToNode);
          this.loadingChildren.set(false);
        },
        error: err => {
          this.alertService.httpError('Failed to load electrical tilts', err);
          this.loadingChildren.set(false);
        }
      });
    }
  }

  onSelect(node: TreeNodeVm): void {
    this.select.emit(node);
  }

  onAddChild(node: TreeNodeVm): void {
    this.addChild.emit(node);
  }
}
