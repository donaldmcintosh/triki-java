package net.opentechnology.triki.mtd.enums;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

public enum DateRange {

    NEXT_6_MONTHS("Next 6 months"),
    NEXT_2_YEARS("Next 2 years"),
    LAST_2_YEARS("Last 2 years");

    private String label;
    private LocalDate start;
    private LocalDate end;

    private DateRange(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static DateRange getRange(DateRange rangeName) throws EnumValueException {

        switch (rangeName) {
            case NEXT_6_MONTHS:
                DateRange range = DateRange.NEXT_6_MONTHS;
                LocalDate now = LocalDate.now();
                range.start = now;
                range.end = now.plus(6, ChronoUnit.MONTHS);
                return range;
            case NEXT_2_YEARS:
                range = DateRange.NEXT_2_YEARS;
                range.start = LocalDate.now();
                range.end = range.start.plus(2, ChronoUnit.YEARS);
                return range;
            case LAST_2_YEARS:
                range = DateRange.LAST_2_YEARS;
                range.end = LocalDate.now();
                range.start = range.end.minus(2, ChronoUnit.YEARS);
                return range;
            default:
                throw new EnumValueException("Unknown date range");
        }
    }

    public static DateRange getDateRange(String rangeName) throws EnumValueException {
        try {
            return getRange(DateRange.valueOf(rangeName));
        } catch (IllegalArgumentException iae) {
            throw new EnumValueException("Problems converting date range", iae);
        }
    }

    public static DateRange getDateRangeFromDesc(String desc) throws EnumValueException {
        Optional<DateRange> optionalDateRanges = Arrays.stream(DateRange.values()).filter(dr -> dr.getLabel().equals(desc)).findFirst();

        if (optionalDateRanges.isPresent()) {
            return DateRange.getDateRange(optionalDateRanges.get().name());
        } else {
            throw new EnumValueException("Could not convert date range " + desc);
        }
    }

    public static String format(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }
}
