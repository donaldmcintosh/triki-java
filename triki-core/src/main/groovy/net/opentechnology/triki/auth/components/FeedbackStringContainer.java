package net.opentechnology.triki.auth.components;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;

public class FeedbackStringContainer extends WebMarkupContainer {

  private String status;

  public FeedbackStringContainer(String id) {
    super(id);
    this.add(new Label("status"));
    setDefaultModel(new CompoundPropertyModel(this));
  }

  @Override
  public final boolean isVisible() {
    return status != null;
  }

  public void setMsg(String msg) {
    this.status = msg;
  }
}
