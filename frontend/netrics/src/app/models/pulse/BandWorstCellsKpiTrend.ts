import {BandDto} from './BandDto';
import {WorstCells} from './WorstCells';
import {KpiTrendDto} from './KpiTrendDto';

export interface BandWorstCellsKpiTrend{
  band?: BandDto;
  worstCells?: WorstCells[];
  kpiTrend?: KpiTrendDto[];
}
