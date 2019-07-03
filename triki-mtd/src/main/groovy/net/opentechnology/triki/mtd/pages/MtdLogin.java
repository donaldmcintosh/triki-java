package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class MtdLogin extends MtdVatParent {

  public MtdLogin() {
    WebMarkupContainer authenticateStep = new WebMarkupContainer("authenticateStep");
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();


  }
}
