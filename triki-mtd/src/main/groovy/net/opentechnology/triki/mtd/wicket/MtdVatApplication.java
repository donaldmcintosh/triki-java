package net.opentechnology.triki.share.wicket;

import javax.inject.Inject;
import net.opentechnology.triki.mtd.vat.pages.HomePage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class MtdVatApplication extends WebApplication {

  @Inject
  private ApplicationContext ctx;

  @Override
  protected void init() {
    super.init();
    getComponentInstantiationListeners().add(new SpringComponentInjector(this, ctx));
  }

  @Override
  public Class<? extends Page> getHomePage() {
    getComponentInstantiationListeners().add(new SpringComponentInjector(this,
        WebApplicationContextUtils.getRequiredWebApplicationContext(
            getServletContext())));
    return HomePage.class;
  }

  public ApplicationContext getCtx() {
    return ctx;
  }

}
