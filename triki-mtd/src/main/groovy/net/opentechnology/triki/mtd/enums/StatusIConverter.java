package net.opentechnology.triki.mtd.enums;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.util.Locale;

public class StatusIConverter implements IConverter<VatObligationStatus> {
    @Override
    public VatObligationStatus convertToObject(String value, Locale locale) throws ConversionException {
        try {
            return VatObligationStatus.getVatObligationStatus(value);
        } catch (EnumValueException e) {
            throw new ConversionException(e);
        }
    }

    @Override
    public String convertToString(VatObligationStatus value, Locale locale) {
        return value.getDescription();
    }
}
