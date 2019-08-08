package net.opentechnology.triki.mtd.pages;

import static net.opentechnology.triki.mtd.security.HmrcIdentityProvider.HMRC_TOKEN;

import net.opentechnology.triki.auth.resources.SessionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class MtdVatParent extends WebPage {

  @SpringBean
  private SessionUtils sessionUtils;

  private final AuthHeaderStatus authenticateHeaderStatus;
  private final AuthHeaderStatus authoriseHeaderStatus;

  public MtdVatParent() {
    authenticateHeaderStatus = new AuthHeaderStatus("authenticateStatus");
    add(authenticateHeaderStatus);
    authoriseHeaderStatus = new AuthHeaderStatus("authoriseStatus");
    add(authoriseHeaderStatus);
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();

    if(sessionUtils.hasAuthenticatedEmail()) {
      authenticateHeaderStatus.setAuthText("Authenticated as " + sessionUtils.getProfile().getEmail());
      authenticateHeaderStatus.add(new AttributeAppender("class", Model.of(" green")));
      authenticateHeaderStatus.setIcon("lock");
      if(sessionUtils.hasModuleToken(HMRC_TOKEN)){
        authoriseHeaderStatus.setAuthText("Authorised with HMRC");
        authoriseHeaderStatus.add(new AttributeAppender("class", Model.of(" green")));
        authoriseHeaderStatus.setIcon("lock");
      }
      else {
        authoriseHeaderStatus.setAuthText("Unauthorised");
        authoriseHeaderStatus.add(new AttributeAppender("class", Model.of(" grey")));
        authoriseHeaderStatus.setIcon("lock open");
      }
    }
    else {
      authenticateHeaderStatus.setAuthText("Unauthenticated");
      authenticateHeaderStatus.add(new AttributeAppender("class", Model.of(" grey")));
      authenticateHeaderStatus.setIcon("lock open");
      authoriseHeaderStatus.setAuthText("Unauthorised");
      authoriseHeaderStatus.add(new AttributeAppender("class", Model.of(" grey")));
      authoriseHeaderStatus.setIcon("lock open");
    }
  }

}
