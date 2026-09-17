import {Component, computed, ElementRef, signal, ViewChild} from '@angular/core';
import {DatePipe, DecimalPipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {AssistantService} from '../../../../../service/pulse/assistant-service';
import {AssistantResponseDto} from '../../../../../models/pulse/AssistantDto';
import {AnomalyCellDto} from '../../../../../models/pulse/AnomalyCellDto';
import {KpiTrendDto} from '../../../../../models/pulse/KpiTrendDto';
import {KpiDataDto} from '../../../../../models/pulse/KpiDataDto';
import {BasicKpiSnapshot} from '../../../../../models/pulse/BasicKpiSnapshot';
import {CellAnalysisModal} from '../anomaly-cells/cell-analysis-modal/cell-analysis-modal';

interface ChatTurn {
  question: string;
  loading: boolean;
  response?: AssistantResponseDto;
  error?: string;
}

@Component({
  selector: 'app-assistant',
  imports: [DatePipe, DecimalPipe, FormsModule, RouterLink, CellAnalysisModal],
  templateUrl: './assistant.html'
})
export class Assistant {
  readonly maxLength = 500;
  readonly examples = [
    'Top E-RAB drop rate worst cells of yesterday for Kandy district',
    'Abnormal RRC setup success rates in Region 1',
    'E-RAB drop rate trend for Colombo over the last month',
    'Critical anomalies in Galle',
  ];

  turns = signal<ChatTurn[]>([]);
  question = signal('');
  busy = computed(() => this.turns().some(t => t.loading));

  // Cell analysis modal state (same inputs as the anomaly page)
  showCellAnalysisModal = signal(false);
  selectedCellName = signal('');
  standardKpi = signal<string | undefined>(undefined);
  rat = signal<string | undefined>(undefined);
  granularity = signal<string | undefined>(undefined);

  @ViewChild('conversation') conversationRef?: ElementRef<HTMLDivElement>;

  constructor(private assistantService: AssistantService) {
  }

  ask(text: string = this.question()): void {
    const question = text.trim().slice(0, this.maxLength);
    if (!question || this.busy()) return;

    const index = this.turns().length;
    this.turns.update(ts => [...ts, {question, loading: true}]);
    this.question.set('');
    this.scrollToBottom();

    this.assistantService.ask({question}).subscribe({
      next: response => this.updateTurn(index, {loading: false, response}),
      error: err => this.updateTurn(index, {
        loading: false,
        error: err?.error?.message ?? 'The assistant is unavailable right now. Please try again later.'
      })
    });
  }

  onEnter(event: Event): void {
    const e = event as KeyboardEvent;
    if (e.shiftKey) return;   // Shift+Enter = newline
    e.preventDefault();
    this.ask();
  }

  chooseOption(turn: ChatTurn, option: string): void {
    this.ask(`${turn.question} (${option})`);
  }

  interpretedAs(r: AssistantResponseDto): string[] {
    const p = r.params ?? {};
    return [p['kpiLabel'], p['cell'], p['area'], p['rat'], p['period'], p['granularity'], p['severity'],
      p['limit'] ? `top ${p['limit']}` : undefined]
      .filter((v): v is string => !!v);
  }

  cellRows(r: AssistantResponseDto): AnomalyCellDto[] {
    return r.rows as AnomalyCellDto[];
  }

  trendRows(r: AssistantResponseDto): KpiTrendDto[] {
    return r.rows as KpiTrendDto[];
  }

  cellKpiRows(r: AssistantResponseDto): KpiDataDto[] {
    return r.rows as KpiDataDto[];
  }

  snapshot(r: AssistantResponseDto): BasicKpiSnapshot {
    return r.rows[0] as BasicKpiSnapshot;
  }

  openCell(r: AssistantResponseDto, cell: AnomalyCellDto): void {
    this.selectedCellName.set(cell.cellName);
    this.standardKpi.set(cell.kpiName);
    this.rat.set(r.params['rat']);
    this.granularity.set(r.params['granularity']);
    this.showCellAnalysisModal.set(true);
  }

  severityDotClass(severity: string | null): string {
    switch (severity) {
      case 'critical':
        return 'bg-error';
      case 'high':
        return 'bg-warning';
      case 'moderate':
        return 'bg-warning opacity-50';
      default:
        return 'bg-base-300';
    }
  }

  private updateTurn(index: number, patch: Partial<ChatTurn>): void {
    this.turns.update(ts => ts.map((t, i) => i === index ? {...t, ...patch} : t));
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const el = this.conversationRef?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    });
  }
}
