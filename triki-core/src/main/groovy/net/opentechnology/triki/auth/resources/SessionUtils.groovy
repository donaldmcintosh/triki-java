package net.opentechnology.triki.auth.resources;

import groovy.util.logging.Log4j
import org.apache.wicket.Session;

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

  @Inject HttpSession session2;

  @Inject
  private AuthorisationManager authorisationManager;

  @Inject
  private Utilities utils;

  @Inject
  private SettingDto settingDto;

  @Inject
  private final IdentityProviders identityProviders

  private setKnownPersonSession(HttpSession session, Resource person) {
    logger.info("Setting session known person for " + person.getProperty(Dcterms.title)?.getString())
    session.setAttribute(SESSION_PERSON, person)
  }

  public setProfile(HttpSession session, Profile profile) {
    logger.info("Setting session profile to be " + profile)
    session2.setAttribute(SESSION_PROFILE, profile)
    if (Session.exists())
    {
      Session wicketSession = Session.get();
      wicketSession.setAttribute(SESSION_PROFILE, profile)
    }
  }

  public boolean ifKnownSave(Optional<Resource> signedInPerson, HttpSession session){
    if(signedInPerson.isPresent()){
      // If known to me
      logger.info("${signedInPerson.get().getProperty(Dcterms.title)?.getString()} successfully authenticated");
      setKnownPersonSession(session, signedInPerson.get());
    }
  }

  public setIfAdmin(Optional<Resource> signedInPerson, Profile profile){
    if(signedInPerson.isPresent()){
      if(authorisationManager.isAdmin(signedInPerson.get())){
        profile.setIsAdmin(true)
      }
    }
  }

  public forwardCorrectly(HttpServletResponse resp, HttpSession session, String referer){
    // Forward correctly
    String redirectUrl = session.getAttribute("redirectUrl")
    // Check if redirected via filter first
    if(redirectUrl){
      logger.info("Redirecting to ${redirectUrl}")
      session.removeAttribute("redirectUrl")
      resp.sendRedirect(redirectUrl);
    } else if (referer && referer != NOREFERRER) {
      logger.info("Redirecting to ${referer}")
      resp.sendRedirect(referer);
    }
    else {
      logger.info("Redirecting to home")
      resp.sendRedirect("/");
    }
  }

}
