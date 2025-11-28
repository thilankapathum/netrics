package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.WorstCellCommentDto;
import dev.thilanka.netrics.entity.WorstCellComment;

import java.util.List;

public interface WorstCellCommentService {

    WorstCellCommentDto createWorstCellComment(WorstCellCommentDto dto);
    WorstCellCommentDto createWorstCellComment(String comment, Long worstCellId);

    List<WorstCellCommentDto> getCommentsByWorstCell(Long worstCellId);

    WorstCellCommentDto modifyCommentByWorstCell(WorstCellCommentDto dto);     //TODO: Add function to have separate comments for different users

    WorstCellCommentDto UpdateWorstCellComment(WorstCellCommentDto dto);
}
