import {WorstCellsWithLatestDto} from './WorstCellsWithLatestDto';
import {WorstCellCommentDto} from './WorstCellCommentDto';

export  interface WorstCellAndCommentsDto{
  worstCell: WorstCellsWithLatestDto;
  comments: Array<WorstCellCommentDto>;
}
