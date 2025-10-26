package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.DateRangeDto;
import dev.thilanka.netrics.entity.Rat;

import java.time.LocalDateTime;

public interface DateService {

    LocalDateTime getLatestDate(Rat rat);

    LocalDateTime getLatestDate(String ratName);

//    DateRangeDto getDateRange(String period, String ratName);

    LocalDateTime getLatestPreviousDate(String period, Rat rat);

    LocalDateTime getLatestPreviousDate(String period, String ratName);

    Long getPeriod(String period);
}
