package net.opentechnology.triki.auth;

import net.opentechnology.triki.auth.pages.HelloWorld;
import net.opentechnology.triki.auth.pages.LoginPage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

public class WicketApplication extends WebApplication {

  @Override
  public Class<? extends Page> getHomePage() {
    return LoginPage.class;
  }

}
