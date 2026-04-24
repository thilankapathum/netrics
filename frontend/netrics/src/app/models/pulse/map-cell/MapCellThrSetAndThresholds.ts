import {MapCellThrSetResponseDto} from './MapCellThrSetResponseDto';
import {MapCellThresholdDto} from './MapCellThresholdDto';

export interface MapCellThrSetAndThresholds {
  thrSet?: MapCellThrSetResponseDto;
  thresholds?: Array<MapCellThresholdDto>;
}
