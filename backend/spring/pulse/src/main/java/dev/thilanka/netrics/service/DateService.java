package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.DateRangeDto;
import dev.thilanka.netrics.entity.Rat;

import java.time.LocalDateTime;

public interface DateService {

    LocalDateTime getLatestDate(Rat rat);
    LocalDateTime getLatestDateStart(Rat rat, String period);

    LocalDateTime getLatestDate(String ratName);

    DateRangeDto getLatestDateRange(String period, String ratName);

    LocalDateTime getLatestPreviousDate(String period, Rat rat);
    LocalDateTime getLatestPreviousDateStart(Rat rat, String period);

    LocalDateTime getLatestPreviousDate(String period, String ratName);

    Long getPeriod(String period);

}
