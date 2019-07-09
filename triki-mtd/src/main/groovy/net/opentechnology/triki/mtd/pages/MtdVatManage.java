package net.opentechnology.triki.mtd.pages;

import net.opentechnology.triki.auth.resources.SessionUtils;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class MtdVatManage extends MtdVatParent {

  @SpringBean
  private SessionUtils sessionUtils;

  private final NavigationMenu navigationMenu;

  public MtdVatManage() {
    navigationMenu = new NavigationMenu("navigationMenu");
    add(navigationMenu);
  }

  public void setActiveMenu(String name){
    switch (name) {
      case "obligations":
        navigationMenu.obligationsLinkActive();
      case "returns":
        navigationMenu.returnsLinkActive();
      case "liabilities":
        navigationMenu.liabilitiesLinkActive();
      case "payments":
        navigationMenu.paymentsLinkActive();
    }
  }
}
