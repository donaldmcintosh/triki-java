package net.opentechnology.triki.mtd.pages;

import net.opentechnology.triki.auth.resources.SessionUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class MtdVatManage extends MtdVatParent {

  @SpringBean
  private SessionUtils sessionUtils;

  private final NavigationMenu navigationMenu;

  public MtdVatManage(PageParameters parameters) {
    navigationMenu = new NavigationMenu("navigationMenu");
    add(navigationMenu);
  }

  public void setActiveMenu(String name){
    switch (name) {
      case "obligations":
        navigationMenu.obligationsLinkActive();
        break;
      case "returns":
        navigationMenu.returnsLinkActive();
        break;
      case "liabilities":
        navigationMenu.liabilitiesLinkActive();
        break;
      case "payments":
        navigationMenu.paymentsLinkActive();
        break;
    }
  }
}
