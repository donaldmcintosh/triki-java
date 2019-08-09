package net.opentechnology.triki.mtd.vatapi.serialisers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

public class LocalDateConverter implements IConverter<LocalDate> {

  @Override
  public LocalDate convertToObject(String value, Locale locale) throws ConversionException {
    return null;
  }

  @Override
  public String convertToString(LocalDate date, Locale locale) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMM yyyy");
    return date.format(dtf);
  }
}
