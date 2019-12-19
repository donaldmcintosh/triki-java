package net.opentechnology.triki.auth.resources;

import groovy.util.logging.Log4j;
import java.util.Optional;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.opentechnology.triki.auth.AuthenticationManager;
import net.opentechnology.triki.auth.AuthorisationManager;
import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.Utilities;
import net.opentechnology.triki.core.dto.SettingDto;
import net.opentechnology.triki.schema.Dcterms;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.rdf.model.Resource;
import org.apache.log4j.Logger;

@Log4j
public class SessionUtils {

  public static final String NOREFERRER = "NOREFERRER"
  public static final String SESSION_PERSON = "person";
  public static final String SESSION_PROFILE = "profile";

  private final Logger logger = Logger.getLogger(this.getClass());

  @Inject HttpSession session;

  @Inject
  private AuthorisationManager authorisationManager;

  @Inject
  private Utilities utils;

  @Inject
  private SettingDto settingDto;

  @Inject
  private final IdentityProviders identityProviders

  public SessionUtils(){}

  public SessionUtils(HttpSession session){
    this.session = session;
  }

  private setKnownPersonSession(  Resource person) {
    logger.info("Setting session known person for " + person.getProperty(Dcterms.title)?.getString())
    session.setAttribute(SESSION_PERSON, person)
  }

  public setProfile(Profile profile) {
    logger.info("Setting session profile to be " + profile)
    session.setAttribute(SESSION_PROFILE, profile)
  }

  public boolean hasAuthenticatedEmail(){
    getProfile().getEmail()
  }

  public boolean hasModuleToken(String tokenName){
    getProfile().getModuleParams().get(tokenName)
  }

  public String getModuleToken(String tokenName){
    return getProfile().getModuleParams().get(tokenName)
  }

  public String setModuleToken(String tokenName, String value){
    return getProfile().getModuleParams().put(tokenName, value);
  }

  public boolean ifKnownSave(Optional<Resource> signedInPerson){
    if(signedInPerson.isPresent()){
      // If known to me
      logger.info("${signedInPerson.get().getProperty(Dcterms.title)?.getString()} successfully authenticated");
      setKnownPersonSession(signedInPerson.get());
    }
  }

  public setIfAdmin(Optional<Resource> signedInPerson, Profile profile){
    if(signedInPerson.isPresent()){
      if(authorisationManager.isAdmin(signedInPerson.get())){
        profile.setIsAdmin(true)
      }
    }
  }

  public Profile getProfile(){
    if(session.getAttribute(AuthenticateResource.SESSION_PROFILE)){
      return session.getAttribute(AuthenticateResource.SESSION_PROFILE)
    }
    else {
      def profile = new Profile();
      setProfile(profile);
      return profile;
    }
  }

  public forwardCorrectly(HttpServletResponse resp, String referer){
    // Forward correctly
    String redirectUrl = session.getAttribute("redirectUrl")
    // Check if redirected via filter first
    if(redirectUrl){
      logger.info("Redirecting to ${redirectUrl} using redirectUrl from filter")
      session.removeAttribute("redirectUrl")
      resp.sendRedirect(redirectUrl);
    } else if (referer && referer != NOREFERRER) {
      logger.info("Redirecting to ${referer} using referer from OAuth")
      resp.sendRedirect(referer);
    }
    else {
      logger.info("Redirecting to home")
      resp.sendRedirect("/");
    }
  }

}
