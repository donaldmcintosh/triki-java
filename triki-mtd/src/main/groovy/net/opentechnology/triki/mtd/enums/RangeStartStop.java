package net.opentechnology.triki.mtd.enums;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class RangeStartStop {
    private LocalDate start;
    private LocalDate end;

    public RangeStartStop(DateRange range) throws EnumValueException {
        switch (range) {
            case NEXT_6_MONTHS:
                LocalDate now = LocalDate.now();
                start = now;
                end = now.plus(6, ChronoUnit.MONTHS);
                return;
            case NEXT_2_YEARS:
                start = LocalDate.now();
                end = start.plus(2, ChronoUnit.YEARS);
                return;
            case LAST_2_YEARS:
                end = LocalDate.now();
                start = end.minus(2, ChronoUnit.YEARS);
                return;
            default:
                throw new EnumValueException("Unknown date range");
        }
    }

    public static String format(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getStartFormatted() {
        return format(start);
    }

    public String getEndFormatted() {
        return format(end);
    }
}
