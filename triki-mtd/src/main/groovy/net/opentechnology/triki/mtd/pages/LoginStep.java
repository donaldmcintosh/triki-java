package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.markup.html.WebMarkupContainer;

public class LoginStep extends WebMarkupContainer {

    private boolean isEnabled = false;

    public LoginStep(String id) {
        super(id);
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabledMode(boolean enabled) {
        isEnabled = enabled;
    }
}
