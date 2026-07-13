package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.BusinessValidationException;
import dev.thilanka.netrics.dto.DateRangeDto;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.repository.KpiHourRepository;
import dev.thilanka.netrics.service.DateService;
import dev.thilanka.netrics.service.GranularityService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class DateServiceImpl implements DateService {
    private final KpiDayRepository kpiDayRepository;
    private final KpiHourRepository kpiHourRepository;
    private final RatService ratService;
    private final GranularityService granularityService;

    @Override
    public LocalDateTime getLatestDate() {
        //TODO: implement for KPI-HOUR
        return kpiDayRepository.getLatestDate();
    }

    @Override
    public LocalDateTime getLatestDate(Rat rat, Granularity granularity) {

        if (granularity.getName().equals("hour")) {     //-- Hourly granularity should be taken from hourly KPI table
            return kpiHourRepository.getLatestDate(rat.getId(), granularity.getId());
        } else {
            return kpiDayRepository.getLatestDate(rat.getId(), granularity.getId());
        }
    }

    @Override
    public LocalDateTime getLatestDateStart(Rat rat, String period, Granularity granularity) {
        return getLatestPreviousDate(period, rat, granularity).plusDays(1L);
    }

    @Override
    public LocalDateTime getLatestDate(String ratName, String granularityName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        return kpiDayRepository.getLatestDate(rat.getId(), granularity.getId());
    }

    @Override
    @Cacheable(value = "latestDateRange", key = "#period + '_' + #granularityName + '_' + #ratName")
    public DateRangeDto getLatestDateRange(String period, String ratName, String granularityName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        LocalDateTime latestDate = getLatestDate(rat, granularity).toLocalDate().atStartOfDay();
        LocalDateTime latestPrevDate = getLatestPreviousDate(period, rat, granularity).plusDays(1);
        return new DateRangeDto(Timestamp.valueOf(latestDate), Timestamp.valueOf(latestPrevDate));
    }

    @Override
    public LocalDateTime getLatestPreviousDate(String period, Rat rat, Granularity granularity) {
        switch (period) {
            case "day" -> {
                return getLatestDate(rat, granularity).minusDays(1);
            }
            case "week" -> {
                return getLatestDate(rat, granularity).minusDays(7);
            }
            case "month" -> {
                return getLatestDate(rat, granularity).minusDays(30);
            }
            case "quarter" -> {
                return getLatestDate(rat, granularity).minusDays(90);
            }
            case "half-year" -> {
                return getLatestDate(rat, granularity).minusDays(180);
            }
            case "year" -> {
                return getLatestDate(rat, granularity).minusDays(365);
            }
        }
        return null;
    }

    @Override
    public LocalDateTime getLatestPreviousDateStart(Rat rat, String period) {
        return null;
    }

    @Override
    public LocalDateTime getLatestPreviousDate(String period, String ratName, String granularityName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        return getLatestPreviousDate(period, rat, granularity);
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

    @Override
    public boolean isDateIsDay(LocalDateTime timestamp, DayOfWeek day) {
        return timestamp.getDayOfWeek() == day;
    }

    @Override
    public DayOfWeek extractDayOfWeek(String day) {
        try {
            return DayOfWeek.valueOf(day.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessValidationException("Incorrect refresh day: " + day);
        }
    }

    @Override
    public LocalDateTime extractDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter).atStartOfDay();
    }

    @Override
    public LocalDateTime getPreviousDate(LocalDateTime currentDate, String period) {
        switch (period) {
            case "day" -> {
                return currentDate.minusDays(1);
            }
            case "week" -> {
                return currentDate.minusDays(7);
            }
            case "month" -> {
                return currentDate.minusDays(30);
            }
            case "quarter" -> {
                return currentDate.minusDays(90);
            }
            case "half-year" -> {
                return currentDate.minusDays(180);
            }
            case "year" -> {
                return currentDate.minusDays(365);
            }
        }
        return null;
    }

    @Override
    public LocalDateTime getStartDate(LocalDateTime currentDate, String period) {
        return currentDate.minusDays(getPeriod(period));
    }
}
