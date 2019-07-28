package net.opentechnology.triki.mtd.enums;

public enum DateRange {

    NEXT_6_MONTHS("Next 6 months"),
    NEXT_2_YEARS("Next 2 years"),
    LAST_2_YEARS("Last 2 years");

    private final String label;

    private DateRange(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }


    @Override
    public String toString() {
        return label;
    }
}
