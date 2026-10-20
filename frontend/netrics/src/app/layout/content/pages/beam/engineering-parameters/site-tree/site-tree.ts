import {Component, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {SiteService} from '../../../../../../service/beam/site-service';
import {SectorService} from '../../../../../../service/beam/sector-service';
import {AlertService} from '../../../../../../components/alert/alert.service';
import {AuthService} from '../../../../../../auth/service/auth-service';
import {SiteDto} from '../../../../../../models/beam/SiteDto';
import {NodeType, sectorToNode, TreeNodeVm} from '../tree-node.model';
import {TreeNode} from '../tree-node/tree-node';
import {NodeEditModal} from '../node-edit-modal/node-edit-modal';

@Component({
  selector: 'app-site-tree',
  imports: [CommonModule, RouterLink, TreeNode, NodeEditModal],
  templateUrl: './site-tree.html',
  styleUrl: './site-tree.css'
})
export class SiteTree implements OnInit {
  siteCode = signal<string>('');
  site = signal<SiteDto | undefined>(undefined);
  sectorNodes = signal<TreeNodeVm[]>([]);
  loading = signal(false);

  modalOpen = signal(false);
  modalMode: 'create' | 'edit' = 'edit';
  modalNodeType: NodeType = 'site';
  editNode?: TreeNodeVm;
  parentNode?: TreeNodeVm;

  constructor(private route: ActivatedRoute,
              private siteService: SiteService,
              private sectorService: SectorService,
              private alertService: AlertService,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    const siteCode = this.route.snapshot.paramMap.get('siteCode') ?? '';
    this.siteCode.set(siteCode);
    this.loadSite(siteCode);
  }

  canAdd(): boolean {
    return this.authService.hasAnyRole(['BEAM_CREATE']);
  }

  canEdit(): boolean {
    return this.authService.hasAnyRole(['BEAM_UPDATE']);
  }

  private loadSite(siteCode: string): void {
    this.loading.set(true);
    this.siteService.getBySiteCode(siteCode).subscribe({
      next: site => {
        this.site.set(site);
        this.loadSectors(siteCode);
      },
      error: err => {
        this.loading.set(false);
        this.alertService.httpError(`Failed to load site ${siteCode}`, err);
      }
    });
  }

  private loadSectors(siteCode: string): void {
    this.sectorService.getBySiteCode(siteCode).subscribe({
      next: sectors => {
        this.sectorNodes.set(sectors.map(sectorToNode));
        this.loading.set(false);
      },
      error: err => {
        this.loading.set(false);
        this.alertService.httpError('Failed to load sectors', err);
      }
    });
  }

  onSelectSite(): void {
    this.editNode = {type: 'site', label: this.site()!.siteCode, data: this.site()!};
    this.parentNode = undefined;
    this.modalMode = 'edit';
    this.modalNodeType = 'site';
    this.modalOpen.set(true);
  }

  onSelect(node: TreeNodeVm): void {
    this.editNode = node;
    this.parentNode = undefined;
    this.modalMode = 'edit';
    this.modalNodeType = node.type;
    this.modalOpen.set(true);
  }

  onAddSector(): void {
    this.openAdd('sector', {type: 'site', label: this.site()!.siteCode, data: this.site()!});
  }

  onAddChild(parent: TreeNodeVm): void {
    const childType: NodeType = parent.type === 'sector' ? 'antenna' : 'tilt';
    this.openAdd(childType, parent);
  }

  private openAdd(nodeType: NodeType, parent: TreeNodeVm): void {
    this.editNode = undefined;
    this.parentNode = parent;
    this.modalMode = 'create';
    this.modalNodeType = nodeType;
    this.modalOpen.set(true);
  }

  onModalClosed(): void {
    this.modalOpen.set(false);
  }

  onModalSaved(node: TreeNodeVm): void {
    this.modalOpen.set(false);

    if (this.modalMode === 'edit') {
      if (this.modalNodeType === 'site') {
        this.site.set(node.data as SiteDto);
      } else if (this.editNode) {
        this.editNode.data = node.data;
        this.editNode.label = node.label;
      }
      return;
    }

    // create
    if (this.modalNodeType === 'sector') {
      this.sectorNodes.update(nodes => [...nodes, node]);
    } else if (this.parentNode?.children) {
      // Unloaded (undefined) children stay lazy, so the next expand fetches the full list incl. the new node
      this.parentNode.children = [...this.parentNode.children, node];
    }
  }
}
