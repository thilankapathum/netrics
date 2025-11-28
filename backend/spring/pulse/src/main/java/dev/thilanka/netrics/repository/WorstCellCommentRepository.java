package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.WorstCell;
import dev.thilanka.netrics.entity.WorstCellComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorstCellCommentRepository extends JpaRepository<WorstCellComment,Long> {

    List<WorstCellComment> findByWorstCell(WorstCell worstCell);
}
