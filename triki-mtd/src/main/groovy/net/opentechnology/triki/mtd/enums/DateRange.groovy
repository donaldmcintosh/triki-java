package net.opentechnology.triki.mtd.enums

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DateRange {

    private LocalDate start
    private LocalDate end

    public enum DateRanges {

        THIS_YEAR_AND_LAST("This year and last"),
        NEXT_2_YEARS("Next 2 years"),
        LAST_2_YEARS("Last 2 years"),
        LAST_5_YEARS("Last 5 years"),
        LAST_10_YEARS("Last 10 years");

        private final String label;

        private DateRanges(String label){
            this.label = label;
        }

        String getLabel() {
            return label
        }
    }

    public static DateRange getRange(DateRanges rangeName){
        DateRange range = new DateRange();

        switch(rangeName){
            case DateRanges.THIS_YEAR_AND_LAST:
                LocalDate now = LocalDate.now();
                range.start = now.minus(1, ChronoUnit.YEARS)
                range.end = range.start.plus(1, ChronoUnit.YEARS)
                return range;
            case DateRanges.NEXT_2_YEARS:
                range.start = LocalDate.now();
                range.end = range.start.plus(2, ChronoUnit.YEARS)
                return range;
            case DateRanges.LAST_2_YEARS:
                range.end = LocalDate.now()
                range.start = range.end.minus(2, ChronoUnit.YEARS)
                return range;
            case DateRanges.LAST_5_YEARS:
                range.end = LocalDate.now()
                range.start = range.end.minus(5, ChronoUnit.YEARS)
                return range;
            case DateRanges.LAST_10_YEARS:
                range.end = LocalDate.now()
                range.start = range.end.minus(10, ChronoUnit.YEARS)
                return range;
            default:
                throw new DateRangeException("Unknown date range")
        }
    }

    public static DateRange getDateRange(String rangeName){
        try {
            getRange(DateRange.DateRanges.valueOf(rangeName))
        } catch (IllegalArgumentException iae){
            return null;
        }
    }

    static String format(LocalDate date){
        date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    LocalDate getStart() {
        return start
    }

    LocalDate getEnd() {
        return end
    }
}
