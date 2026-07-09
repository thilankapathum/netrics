package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.DateRangeDto;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public interface DateService {

    LocalDateTime getLatestDate();
    LocalDateTime getLatestDate(Rat rat, Granularity granularity);
    LocalDateTime getLatestDateStart(Rat rat, String period, Granularity granularity);

    LocalDateTime getLatestDate(String ratName, String granularityName);

    DateRangeDto getLatestDateRange(String period, String ratName, String granularityName);

    LocalDateTime getLatestPreviousDate(String period, Rat rat, Granularity granularity);
    LocalDateTime getLatestPreviousDateStart(Rat rat, String period);

    LocalDateTime getLatestPreviousDate(String period, String ratName, String granularityName);

    Long getPeriod(String period);

    boolean isDateIsDay(LocalDateTime timestamp, DayOfWeek day);

    DayOfWeek extractDayOfWeek(String day);

    LocalDateTime extractDate(String date);

    LocalDateTime getPreviousDate(LocalDateTime currentDate, String period);

    LocalDateTime getStartDate(LocalDateTime currentDate, String period);
}
