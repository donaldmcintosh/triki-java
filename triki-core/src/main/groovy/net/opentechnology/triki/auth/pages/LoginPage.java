package net.opentechnology.triki.auth.pages;

import groovy.util.logging.Log4j;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.opentechnology.triki.auth.AuthenticationException;
import net.opentechnology.triki.auth.AuthenticationManager;
import net.opentechnology.triki.auth.components.FeedbackListContainer;
import net.opentechnology.triki.auth.components.FeedbackStringContainer;
import net.opentechnology.triki.auth.resources.Profile;
import net.opentechnology.triki.auth.resources.SessionUtils;
import net.opentechnology.triki.schema.Dcterms;
import net.opentechnology.triki.schema.Foaf;
import org.apache.jena.rdf.model.Resource;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

@Log4j
public class LoginPage extends ParentPage {

  @SpringBean
  private AuthenticationManager authMgr;

  @SpringBean
  private SessionUtils sessionUtils;

  public LoginPage(final PageParameters parameters) {
    super(parameters);
    add(new LoginForm("loginForm", authMgr, sessionUtils));
  }

  class LoginForm extends Form {

    public static final String PLEASE_TRY_AGAIN = "Wrong username or password.  Please try again.";
    private AuthenticationManager authMgr;
    private SessionUtils sessionUtils;
    private String username;
    private String password;
//    private String loginStatus;
    private FeedbackStringContainer loginFeedback;

    public LoginForm(String id, AuthenticationManager authMgr, SessionUtils sessionUtils) {
      super(id);
      this.authMgr = authMgr;
      this.sessionUtils = sessionUtils;

      setDefaultModel(new CompoundPropertyModel(this));

      TextField usernameTextfield = new TextField("username");
      usernameTextfield.setRequired(true);
      add(usernameTextfield);
      FeedbackListContainer usernameFeedbackPanel = new FeedbackListContainer("usernameFeedback");
      usernameFeedbackPanel.setFilter(new ComponentFeedbackMessageFilter(usernameTextfield));
      add(usernameFeedbackPanel);

      PasswordTextField passwordTextField = new PasswordTextField("password");
      passwordTextField.setRequired(true);
      add(passwordTextField);
      FeedbackListContainer passwordFeedbackPanel = new FeedbackListContainer("passwordFeedback");
      passwordFeedbackPanel.setFilter(new ComponentFeedbackMessageFilter(passwordTextField));
      add(passwordFeedbackPanel);

      loginFeedback = new FeedbackStringContainer("loginFeedback");
      add(loginFeedback);
    }

    @Override
    public final void onSubmit() {
      try {
        Optional<Resource> signedInPerson = authMgr.authenticate(username, password);
        if (signedInPerson.isPresent()) {
          RedirectPage redir =new RedirectPage("/");
          HttpSession session =  ((HttpServletRequest) redir.getPage().getRequest().getContainerRequest()).getSession();
          setSession(signedInPerson, session);
          setResponsePage(redir);
        } else {
          loginFeedback.setMsg(PLEASE_TRY_AGAIN);
        }
      } catch (AuthenticationException e) {
        loginFeedback.setMsg(PLEASE_TRY_AGAIN);
      }
    }

    @Override
    protected void onValidate() {
      super.onValidate();
      loginFeedback.setMsg(null);
    }

    private void setSessionAndForward(HttpServletResponse resp, HttpServletRequest req,
                                      Optional<Resource> signedInPerson) {
      HttpSession session = req.getSession();
      setSession(signedInPerson, session);
      sessionUtils.forwardCorrectly(resp, session, null);
    }

    private void setSession(Optional<Resource> signedInPerson, HttpSession session) {
      Profile profile = Profile.getProfile(session);
      sessionUtils.ifKnownSave(signedInPerson, session);
      if (signedInPerson.get().getProperty(Dcterms.title) != null) {
        profile.setName(signedInPerson.get().getProperty(Dcterms.title).getString());
      }
      if (signedInPerson.get().getProperty(Foaf.mbox) != null) {
        profile.setEmail(signedInPerson.get().getProperty(Foaf.mbox).getString());
      }
      sessionUtils.setIfAdmin(signedInPerson, profile);
      sessionUtils.setProfile(session, profile);
    }
  }
}
