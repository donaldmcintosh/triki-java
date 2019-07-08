package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.markup.html.WebMarkupContainer;

public class AuthHeaderStatus extends WebMarkupContainer {

  private final AuthStatusHeaderIcon authenticateStatusHeaderIcon;

  public AuthHeaderStatus(String id) {
    super(id);

    authenticateStatusHeaderIcon = new AuthStatusHeaderIcon(id + "Icon");
    add(authenticateStatusHeaderIcon);
  }
}
