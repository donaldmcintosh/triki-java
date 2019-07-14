package net.opentechnology.triki.mtd.validators;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;

public class FormFieldRequiredValidator implements INullAcceptingValidator<String> {

  private final String fieldName;

  public FormFieldRequiredValidator(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public void validate(IValidatable<String> validatable) {
    String fieldValue = validatable.getValue();

    if (StringUtils.isBlank(fieldValue)) {
      ValidationError error = new ValidationError(this);
      error.addKey(this.getClass().getSimpleName() + ".required");
      error.setVariable("fieldName", fieldName);
      validatable.error(error);
    }
  }
}