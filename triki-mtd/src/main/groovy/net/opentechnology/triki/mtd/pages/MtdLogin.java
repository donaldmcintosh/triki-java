package net.opentechnology.triki.mtd.pages;

import net.opentechnology.triki.auth.resources.SessionUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class MtdLogin extends MtdVatParent {

  @SpringBean
  private SessionUtils sessionUtils;

  private final LoginStep authenticateStep;

  public MtdLogin() {
    authenticateStep = new LoginStep("authenticateStep");
    add(authenticateStep);
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();

    if(sessionUtils.hasAuthenticatedEmail()){
      authenticateStep.setEnabledMode(false);
      authenticateStep.add(new AttributeAppender("class", Model.of(" disabled")));
    }
    else {
      authenticateStep.setEnabledMode(true);
      authenticateStep.add(new AttributeAppender("class", Model.of(" active")));
    }

  }


}
