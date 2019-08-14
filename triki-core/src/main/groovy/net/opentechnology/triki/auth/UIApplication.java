package net.opentechnology.triki.auth;

import javax.inject.Inject;
import net.opentechnology.triki.auth.pages.LoginPage;
import net.opentechnology.triki.auth.resources.SessionUtils;
import net.opentechnology.triki.modules.Module;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.SecuritySettings;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class UIApplication extends WebApplication {

  @Inject
  private ApplicationContext ctx;

  @Override
  protected void init() {
    super.init();
    getComponentInstantiationListeners().add(new SpringComponentInjector(this, ctx));
    mountPage("/login", LoginPage.class);
    mountModulePages();
    setAuthorisationStrategy(getSecuritySettings());
  }

  private void mountModulePages(){
    String[] beanNames = getCtx().getParent().getBeanNamesForType(Module.class);
    for(String beanName: beanNames){
      Module module = getCtx().getBean(beanName, Module.class);
      module.mountPages(this);
    }
  }

  private void setAuthorisationStrategy(SecuritySettings securitySettings){
    String[] beanNames = getCtx().getParent().getBeanNamesForType(Module.class);
    for(String beanName: beanNames){
      Module module = getCtx().getBean(beanName, Module.class);
      module.setAuthorisationStrategy(securitySettings);
    }
  }

  @Override
  protected IConverterLocator newConverterLocator() {
    ConverterLocator defaultLocator = new ConverterLocator();

    String[] beanNames = getCtx().getParent().getBeanNamesForType(Module.class);
    for(String beanName: beanNames){
      Module module = getCtx().getBean(beanName, Module.class);
      module.addConverters(defaultLocator);
    }

    return defaultLocator;
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
