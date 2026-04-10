package com.studentexpensetracker.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class DateUtil {
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private DateUtil() {
    }

    public static String formatYearMonth(YearMonth aYearMonth) {
        return YEAR_MONTH_FORMATTER.format(aYearMonth);
    }

    public static YearMonth parseYearMonth(String aText) {
        return YearMonth.parse(aText, YEAR_MONTH_FORMATTER);
    }

    public static List<YearMonth> recentMonths(int aCount) {
        YearMonth aCurrent = YearMonth.now();
        List<YearMonth> months = new ArrayList<>();
        for (int anIndex = 0; anIndex < aCount; anIndex += 1) {
            months.add(aCurrent.minusMonths(anIndex));
        }
        return months;
    }

    public static LocalDate safeDate(LocalDate aDate, LocalDate aFallback) {
        return aDate == null ? aFallback : aDate;
    }
}

