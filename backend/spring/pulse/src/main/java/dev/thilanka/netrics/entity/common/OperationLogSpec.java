package dev.thilanka.netrics.entity.common;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OperationLogSpec {
    public static Specification<OperationLog> from(OperationLogFilter f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (f.getEntityName() != null && !f.getEntityName().isBlank())
                predicates.add(cb.equal(root.get("entityName"), f.getEntityName()));

            if (f.getEntityId() != null && !f.getEntityId().isBlank())
                predicates.add(cb.equal(root.get("entityId"), f.getEntityId()));

            if (f.getOperation() != null)
                predicates.add(cb.equal(root.get("operation"), f.getOperation()));

            if (f.getPerformedBy() != null && !f.getPerformedBy().isBlank())
                predicates.add(cb.equal(root.get("performedBy"), f.getPerformedBy()));

            if (f.getFrom() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("performedAt"), f.getFrom()));

            if (f.getTo() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("performedAt"), f.getTo()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
