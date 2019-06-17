package net.opentechnology.triki.auth;

import net.opentechnology.triki.auth.pages.LoginPage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class WicketApplication extends WebApplication {

  @Override
  public Class<? extends Page> getHomePage() {
    getComponentInstantiationListeners().add(new SpringComponentInjector(this,
        WebApplicationContextUtils.getRequiredWebApplicationContext(
            getServletContext())));
    return LoginPage.class;
  }

}
