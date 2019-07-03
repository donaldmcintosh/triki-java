package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.markup.html.WebMarkupContainer;

public class LoginStep extends WebMarkupContainer {
    public LoginStep(String id) {
        super(id);
    }

    @Override
    public boolean isEnabled() {
        // Drive this off login/session status
        // and then add property to this element to control
        // if is completed or not
        return super.isEnabled();
    }
}
