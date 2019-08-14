package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

public class AuthHeaderStatus extends WebMarkupContainer {

  private final AuthStatusHeaderIcon authStatusHeaderIcon;
  private final Label authStatusText;
  private String authTextMsg;

  public AuthHeaderStatus(String id) {
    super(id);

    setDefaultModel(new CompoundPropertyModel(this));

    authStatusHeaderIcon = new AuthStatusHeaderIcon("statusIcon");
    add(authStatusHeaderIcon);
    authStatusText = new Label( "authTextMsg");
    add(authStatusText);

  }

  public void setAuthText(String authTextMsg){
    this.authTextMsg = authTextMsg;
  }

  public void setIcon(String iconName){
    authStatusHeaderIcon.add(new AttributeAppender("class", Model.of(" " + iconName)));
  }
}
