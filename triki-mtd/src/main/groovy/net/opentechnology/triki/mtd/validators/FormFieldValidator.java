package net.opentechnology.triki.mtd.validators;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class FormFieldValidator implements INullAcceptingValidator<String> {

  private final String fieldName;

  public FormFieldValidator(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public void validate(IValidatable<String> validatable) {
    String fieldValue = validatable.getValue();

    if(StringUtils.isBlank(fieldValue)){
      ValidationError error = new ValidationError(this);
      error.setVariable("fieldName", fieldName);
      validatable.error(error);
    }
  }

}
