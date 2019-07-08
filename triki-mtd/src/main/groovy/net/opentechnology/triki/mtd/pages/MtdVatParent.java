package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class MtdVatParent extends WebPage {

  private final AuthHeaderStatus authenticateHeaderStatus;
  private final AuthHeaderStatus authoriseHeaderStatus;

  public MtdVatParent() {
    authenticateHeaderStatus = new AuthHeaderStatus("authenticateStatus");
    add(authenticateHeaderStatus);
    authoriseHeaderStatus = new AuthHeaderStatus("authoriseStatus");
    add(authoriseHeaderStatus);
  }

  public void setColourAuthenticateHeaderStatus(String colour){
    authenticateHeaderStatus.add(new AttributeAppender("class", Model.of(" " + colour)));
  }

  public void setColourAuthoriseHeaderStatus(String colour){
    authoriseHeaderStatus.add(new AttributeAppender("class", Model.of(" " + colour)));
  }

}
