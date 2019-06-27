package net.opentechnology.triki.auth.components;

import org.apache.wicket.markup.html.panel.FeedbackPanel;

public class FeedbackListContainer extends FeedbackPanel {

  public FeedbackListContainer(String id) {
    super(id);
  }

  @Override
  public final boolean isVisible() {
    return getCurrentMessages().size() > 0;
  }
}
