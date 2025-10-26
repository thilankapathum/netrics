package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.service.DateService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DateServiceImpl implements DateService {
    private final KpiDayRepository kpiDayRepository;
    private final RatService ratService;

    @Override
    public LocalDateTime getLatestDate(Rat rat) {
        return kpiDayRepository.getLatestDate(rat.getId());
    }

    @Override
    public LocalDateTime getLatestDate(String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        return kpiDayRepository.getLatestDate(rat.getId());
    }

    @Override
    public LocalDateTime getLatestPreviousDate(String period, Rat rat) {
        switch (period) {
            case "day" -> {
                return getLatestDate(rat).minusDays(1);
            }
            case "week" -> {
                return getLatestDate(rat).minusDays(7);
            }
            case "month" -> {
                return getLatestDate(rat).minusDays(30);
            }
            case "quarter" -> {
                return getLatestDate(rat).minusDays(90);
            }
            case "half-year" -> {
                return getLatestDate(rat).minusDays(180);
            }
            case "year" -> {
                return getLatestDate(rat).minusDays(365);
            }
        }
        return null;
    }

    @Override
    public LocalDateTime getLatestPreviousDate(String period, String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        return getLatestPreviousDate(period, rat);
    }

    @Override
    public Long getPeriod(String period) {
        switch (period) {
            case "day" -> {
                return 0L;
            }
            case "week" -> {
                return 6L;
            }
            case "month" -> {
                return 29L;
            }
            case "quarter" -> {
                return 89L;
            }
            case "half-year" -> {
                return 179L;
            }
            case "year" -> {
                return 364L;
            }
        }
        return null;
    }
}
