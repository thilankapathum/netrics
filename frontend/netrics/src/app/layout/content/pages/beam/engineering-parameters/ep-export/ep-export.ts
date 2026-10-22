import {Component, ElementRef, Input, signal, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  EngineeringExportLevel,
  EngineeringParameterService
} from '../../../../../../service/beam/engineering-parameter-service';
import {AlertService} from '../../../../../../components/alert/alert.service';

const LEVEL_KEY = 'beam.ep-export.level';

@Component({
  selector: 'app-ep-export',
  imports: [CommonModule],
  templateUrl: './ep-export.html'
})
export class EpExport {
  /** Limit the export to one site; leave unset to export every site */
  @Input() siteCode?: string;
  @ViewChild('menu') menu?: ElementRef<HTMLDetailsElement>;

  readonly levels: { value: EngineeringExportLevel, label: string, hint: string }[] = [
    {value: 'SITE', label: 'Site', hint: 'One row per site'},
    {value: 'SECTOR', label: 'Sector', hint: 'One row per sector, with its site'},
    {value: 'ANTENNA', label: 'Antenna', hint: 'One row per antenna, with its sector and site'},
    {value: 'TILT', label: 'Electrical tilt', hint: 'One row per tilt, with its antenna, sector and site'},
  ];

  level = signal<EngineeringExportLevel>(this.savedLevel());
  downloading = signal(false);

  constructor(private engineeringParameterService: EngineeringParameterService,
              private alertService: AlertService) {
  }

  select(level: EngineeringExportLevel): void {
    this.level.set(level);
    try { localStorage.setItem(LEVEL_KEY, level); } catch { /* storage blocked: just don't remember */ }
  }

  download(): void {
    const level = this.level();
    this.downloading.set(true);
    this.engineeringParameterService.export(level, this.siteCode).subscribe({
      next: blob => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = `engineering_parameters_${level.toLowerCase()}${this.siteCode ? '_' + this.siteCode : ''}.csv`;
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
        this.downloading.set(false);
        this.menu?.nativeElement.removeAttribute('open');
      },
      error: err => {
        this.downloading.set(false);
        this.alertService.httpError('Engineering parameter export failed', err);
      }
    });
  }

  private savedLevel(): EngineeringExportLevel {
    try {
      const saved = localStorage.getItem(LEVEL_KEY);
      if (this.levels.some(l => l.value === saved)) return saved as EngineeringExportLevel;
    } catch { /* storage blocked */ }
    return 'TILT';
  }
}
