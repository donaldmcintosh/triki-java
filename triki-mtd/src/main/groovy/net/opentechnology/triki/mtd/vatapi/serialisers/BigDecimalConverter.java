package net.opentechnology.triki.mtd.vatapi.serialisers;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class BigDecimalConverter implements IConverter<BigDecimal> {

  @Override
  public BigDecimal convertToObject(String value, Locale locale) throws ConversionException {
    return null;
  }

  @Override
  public String convertToString(BigDecimal bigDecimal, Locale locale) {
    return new DecimalFormat("#.00").format(bigDecimal);
  }
}
