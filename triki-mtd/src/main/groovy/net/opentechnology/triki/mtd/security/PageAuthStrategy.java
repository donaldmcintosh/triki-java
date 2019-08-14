package net.opentechnology.triki.mtd.security;

import net.opentechnology.triki.auth.resources.SessionUtils;
import org.apache.wicket.Page;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;

import javax.servlet.http.HttpSession;

public class PageAuthStrategy extends SimplePageAuthorizationStrategy {

    public <S extends Page> PageAuthStrategy(Class<?> securePageSuperType, Class<S> signInPageClass) {
        super(securePageSuperType, signInPageClass);
    }

    @Override()
    protected boolean isAuthorized() {
        HttpSession session = ((ServletWebRequest) RequestCycle.get().getRequest()).getContainerRequest().getSession();
        SessionUtils sessionUtils = new SessionUtils(session);

        if(!sessionUtils.hasAuthenticatedEmail() || (sessionUtils.getModuleToken(HmrcIdentityProvider.HMRC_TOKEN) == null)){
            return false;
        }
        else {
            return true;
        }
    }
}
