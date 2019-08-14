package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

public class LoginStep extends WebMarkupContainer {

    private final AuthStatusHeaderIcon authStepIcon;
    private boolean isEnabled = false;

    public LoginStep(String id) {
        super(id);

        authStepIcon = new AuthStatusHeaderIcon("authStepIcon");
        add(authStepIcon);
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabledMode(boolean enabled, String lockIcon) {
        authStepIcon.add(new AttributeAppender("class", Model.of(" " + lockIcon)));
        isEnabled = enabled;
    }
}
