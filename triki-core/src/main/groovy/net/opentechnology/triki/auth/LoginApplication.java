package net.opentechnology.triki.auth;

import javax.inject.Inject;
import net.opentechnology.triki.auth.pages.LoginPage;
import net.opentechnology.triki.auth.resources.SessionUtils;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class LoginApplication extends WebApplication {

  @Inject
  private ApplicationContext ctx;

  @Override
  protected void init() {
    super.init();
//    setName("LoginApplication");
    getComponentInstantiationListeners().add(new SpringComponentInjector(this, ctx));
  }

  @Override
  public Class<? extends Page> getHomePage() {
    getComponentInstantiationListeners().add(new SpringComponentInjector(this,
        WebApplicationContextUtils.getRequiredWebApplicationContext(
            getServletContext())));
    return LoginPage.class;
  }

  public ApplicationContext getCtx() {
    return ctx;
  }

}
