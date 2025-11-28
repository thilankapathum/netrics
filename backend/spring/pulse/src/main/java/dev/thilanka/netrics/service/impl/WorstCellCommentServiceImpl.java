package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.WorstCellCommentDto;
import dev.thilanka.netrics.entity.WorstCell;
import dev.thilanka.netrics.entity.WorstCellComment;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.WorstCellCommentRepository;
import dev.thilanka.netrics.repository.WorstCellRepository;
import dev.thilanka.netrics.service.WorstCellCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorstCellCommentServiceImpl implements WorstCellCommentService {
    private final WorstCellCommentRepository worstCellCommentRepository;
    private final WorstCellRepository worstCellRepository;
    private final Mapper mapper;

    @Override
    public WorstCellCommentDto createWorstCellComment(WorstCellCommentDto dto) {
        WorstCellComment worstCellComment = mapper.dtoToWorstCellComment(dto);
        WorstCellComment savedWorstCellComment = worstCellCommentRepository.save(worstCellComment);
        return mapper.worstCellCommentToDto(savedWorstCellComment);
    }

    @Override
    public WorstCellCommentDto createWorstCellComment(String comment, Long worstCellId) {

        WorstCell worstCell = worstCellRepository.findById(worstCellId)
                .orElseThrow(()-> new RuntimeException("Worst cell not found by ID: " + worstCellId));

        WorstCellComment worstCellComment = WorstCellComment
                .builder()
                .worstCell(worstCell)
                .comment(comment)
                .build();

        WorstCellComment savedWorstCellComment = worstCellCommentRepository.save(worstCellComment);
        return mapper.worstCellCommentToDto(savedWorstCellComment);
    }

    @Override
    public List<WorstCellCommentDto> getCommentsByWorstCell(Long worstCellId) {
        WorstCell worstCell = worstCellRepository.findById(worstCellId)
                .orElseThrow(()-> new RuntimeException("Worst cell not found by ID: " + worstCellId));

        List<WorstCellComment> worstCellComments = worstCellCommentRepository.findByWorstCell(worstCell);

        return worstCellComments.stream().map(wcc -> mapper.worstCellCommentToDto(wcc)).toList();
    }

    @Override
    public WorstCellCommentDto updateCommentByWorstCell(String comment, Long commentId) {

        WorstCellComment worstCellComment = worstCellCommentRepository.findById(commentId)
                .orElseThrow(()-> new RuntimeException("Comment not found by ID: " + commentId));

        worstCellComment.setComment(comment);

        WorstCellComment savedComment = worstCellCommentRepository.save(worstCellComment);

        return mapper.worstCellCommentToDto(savedComment);
    }

    @Override
    public WorstCellCommentDto UpdateWorstCellComment(WorstCellCommentDto dto) {
        return null;
    }
}
