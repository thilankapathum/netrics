package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.BusinessValidationException;
import dev.thilanka.netrics.service.DateService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportJobServiceImplTest {

    private final ReportJobServiceImpl service;

    ReportJobServiceImplTest() {
        DateService dateService = mock(DateService.class);
        when(dateService.extractDate(anyString())).thenAnswer(inv -> LocalDate.parse(inv.<String>getArgument(0)).atStartOfDay());
        service = new ReportJobServiceImpl(null, null, null, null, null, null, null, null, dateService, null, null);
    }

    @Test
    void cellWiseRangeAllowsUpTo31DaysInclusive() {
        assertDoesNotThrow(() -> service.validateCellWiseRange("2026-09-01", "2026-09-01"));
        assertDoesNotThrow(() -> service.validateCellWiseRange("2026-08-01", "2026-08-31"));
    }

    @Test
    void cellWiseRangeRejects32DaysAndInvertedRanges() {
        assertThrows(BusinessValidationException.class, () -> service.validateCellWiseRange("2026-08-01", "2026-09-01"));
        assertThrows(BusinessValidationException.class, () -> service.validateCellWiseRange("2026-09-02", "2026-09-01"));
    }
}
