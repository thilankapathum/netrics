package dev.thilanka.beam.common.exception;

import org.springframework.http.HttpStatus;

public class DependencyConflictException extends BaseException {

    public DependencyConflictException(String resourceName, Object resourceIdentifier, int dependentCount) {
        super(
                String.format("%s '%s' has %d dependent record(s); pass force=true to delete anyway",
                        resourceName, resourceIdentifier, dependentCount),
                "DEPENDENCY_CONFLICT",
                HttpStatus.CONFLICT,
                new Object[]{resourceName, resourceIdentifier, dependentCount}
        );
    }
}
