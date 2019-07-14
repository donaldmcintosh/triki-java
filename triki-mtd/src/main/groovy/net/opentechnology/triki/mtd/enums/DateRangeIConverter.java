package net.opentechnology.triki.mtd.enums;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.util.Locale;

public class DateRangeIConverter implements IConverter<DateRange> {
    @Override
    public DateRange convertToObject(String value, Locale locale) throws ConversionException {
        try {
            return DateRange.getDateRangeFromDesc(value);
        } catch (EnumValueException e) {
            throw new ConversionException(e);
        }
    }

    @Override
    public String convertToString(DateRange value, Locale locale) {
        return value.getLabel();
    }
}
