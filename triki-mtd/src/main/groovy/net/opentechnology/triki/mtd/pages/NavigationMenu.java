package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

public class NavigationMenu extends WebMarkupContainer {

    private final WebMarkupContainer obligationsLink;
    private final WebMarkupContainer returnsLink;
    private final WebMarkupContainer liabilitiesLink;
    private final WebMarkupContainer paymentsLink;

    public NavigationMenu(String id) {
        super(id);

        obligationsLink = new WebMarkupContainer("obligationsLink");
        add(obligationsLink);
        returnsLink = new WebMarkupContainer("returnsLink");
        add(returnsLink);
        liabilitiesLink = new WebMarkupContainer("liabilitiesLink");
        add(liabilitiesLink);
        paymentsLink = new WebMarkupContainer("paymentsLink");
        add(paymentsLink);
    }

    public void obligationsLinkActive() {
        obligationsLink.add(new AttributeAppender("class", Model.of(" active")));
    }

    public void returnsLinkActive() {
        returnsLink.add(new AttributeAppender("class", Model.of(" active")));
    }

    public void liabilitiesLinkActive() {
        liabilitiesLink.add(new AttributeAppender("class", Model.of(" active")));
    }

    public void paymentsLinkActive() {
        paymentsLink.add(new AttributeAppender("class", Model.of(" active")));
    }
}
