package net.opentechnology.triki.mtd.pages;

import java.util.HashMap;
import net.opentechnology.triki.auth.resources.SessionUtils;
import org.apache.wicket.Session;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class MtdVatManage extends MtdVatParent {

  public static final String VRN_SESSION_KEY = "vrnKey";
  public static final String HMRC_HEADERS_SESSION_KEY = "hmrcHeaders";

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

  public void setVrn(String vrn){
    Session.get().setAttribute(VRN_SESSION_KEY, vrn);
  }

  public String getVrn(){
    return (String) Session.get().getAttribute(VRN_SESSION_KEY);
  }

  public void setHmrcHeaders(HashMap<String, String> headers){
    if(Session.get().getAttribute(HMRC_HEADERS_SESSION_KEY) == null) {
      headers.put("Gov-Client-Connection-Method", "WEB_APP_VIA_SERVER");
      Session.get().setAttribute(HMRC_HEADERS_SESSION_KEY, headers);
    }
  }

  public HashMap<String, String> getHeaders(){
    return (HashMap<String, String>) Session.get().getAttribute(HMRC_HEADERS_SESSION_KEY);
  }

  public String getResourceBundleMsg(String key){
    return getString(key);
  }

}
