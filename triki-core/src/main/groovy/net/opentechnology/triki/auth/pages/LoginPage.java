package net.opentechnology.triki.auth.pages;

import java.util.Optional;
import javax.inject.Inject;
import net.opentechnology.triki.auth.AuthenticationException;
import net.opentechnology.triki.auth.AuthenticationManager;
import org.apache.jena.rdf.model.Resource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

public class LoginPage extends ParentPage {

  @Inject
  private AuthenticationManager authMgr;

  public LoginPage(final PageParameters parameters) {
    super(parameters);
    add(new LoginForm("loginForm"));
  }

  class LoginForm extends Form {
    private String username;
    private String password;
    private String loginStatus;

    WebMarkupContainer loginFeedback = new WebMarkupContainer("loginFeedback"){
      @Override
      public final boolean isVisible(){
        return loginStatus != null;
      }
    };

    public LoginForm(String id) {
      super(id);
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
        Optional<Resource> signedInPerson = authMgr.authenticate(username, password);
        if(signedInPerson.isPresent()){
          loginStatus = "Congratulations!!";
        }
        else {
          loginStatus = "Wrong username or password !";
        }
      } catch (AuthenticationException e) {
        loginStatus = "Wrong username or password !";
      }
    }
  }
}
