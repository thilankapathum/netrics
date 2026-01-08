import {WorstCellsWithLatestDto} from './WorstCellsWithLatestDto';
import {WorstCellCommentDto} from './WorstCellCommentDto';

export  interface WorstCellsAndCommentsDto {
  worstCell: WorstCellsWithLatestDto;
  comments: Array<WorstCellCommentDto>;
}
