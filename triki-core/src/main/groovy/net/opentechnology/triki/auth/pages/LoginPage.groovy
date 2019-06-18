package net.opentechnology.triki.auth.pages

import groovy.util.logging.Log4j
import net.opentechnology.triki.auth.AuthenticationException
import net.opentechnology.triki.auth.AuthenticationManager
import net.opentechnology.triki.auth.LoginApplication
import net.opentechnology.triki.auth.resources.Profile
import net.opentechnology.triki.auth.resources.SessionUtils
import net.opentechnology.triki.schema.Dcterms
import net.opentechnology.triki.schema.Foaf
import org.apache.jena.rdf.model.Resource
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.form.Form
import org.apache.wicket.markup.html.form.PasswordTextField
import org.apache.wicket.markup.html.form.TextField
import org.apache.wicket.markup.html.pages.RedirectPage
import org.apache.wicket.markup.html.panel.FeedbackPanel
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.protocol.http.servlet.ServletWebRequest
import org.apache.wicket.protocol.http.servlet.ServletWebResponse
import org.apache.wicket.request.cycle.RequestCycle
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.spring.injection.annot.SpringBean

import javax.inject.Inject
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

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
        private AuthenticationManager authMgr;
        private SessionUtils sessionUtils;
        private String username;
        private String password;
        private String loginStatus;

        WebMarkupContainer loginFeedback = new WebMarkupContainer("loginFeedback") {
            @Override
            public final boolean isVisible() {
                return loginStatus != null;
            }
        };

        public LoginForm(String id, AuthenticationManager authMgr, SessionUtils sessionUtils) {
            super(id);
            this.authMgr = authMgr;
            this.sessionUtils = sessionUtils;
            setDefaultModel(new CompoundPropertyModel(this));

            FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
            add(feedbackPanel);
            TextField usernameTextfield = new TextField("username");
            usernameTextfield.setRequired(true);
            add(usernameTextfield);

            add(new PasswordTextField("password"));
            loginFeedback.add(new Label("loginStatus"));
            add(loginFeedback);
        }

        @Override
        public final void onSubmit() {
            try {
                HttpServletRequest req = RequestCycle.get().getRequest().getContainerRequest();
                HttpServletResponse resp = RequestCycle.get().getResponse().getContainerResponse();

                Optional<Resource> signedInPerson = authMgr.authenticate(username, password);
                if (signedInPerson.isPresent()) {
                    forward(resp, req, signedInPerson );
//                    setResponsePage(new RedirectPage("/"));

                } else {
                    loginStatus = "Wrong username or password !";
                }
            } catch (AuthenticationException e) {
                loginStatus = "Wrong username or password !";
            }
        }

        private void forward(HttpServletResponse resp, HttpServletRequest req,
                            Optional<Resource> signedInPerson) throws ServletException, IOException {
            HttpSession session = req.getSession();
            Profile profile = Profile.getProfile(session)
            profile.setName(signedInPerson.get().getProperty(Dcterms.title)?.getString())
            profile.setEmail(signedInPerson.get().getProperty(Foaf.mbox)?.getString())
            sessionUtils.setIfAdmin(signedInPerson, profile)
            sessionUtils.setProfile(session, profile);
            sessionUtils.forwardCorrectly(resp, session, null)
        }
    }
}
