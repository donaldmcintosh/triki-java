package net.opentechnology.triki.mtd.pages;

import java.util.Arrays;
import java.util.List;
import net.opentechnology.triki.auth.components.FeedbackListContainer;
import net.opentechnology.triki.auth.components.FeedbackStringContainer;
import net.opentechnology.triki.auth.resources.SessionUtils;
import net.opentechnology.triki.mtd.validators.FormFieldValidator;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class MtdVatObligations extends MtdVatManage {

  @SpringBean
  private SessionUtils sessionUtils;

  public MtdVatObligations() {
    add(new MtdVatObligationsForm("mtdVatObligationsForm", sessionUtils));
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
  }

  private class MtdVatObligationsForm extends Form<MtdVatObligationsForm> {

    private SessionUtils sessionUtils;
    private String vrn;
    private String dateRange;
    private String status;
    private String hmrcHeaders;
    private FeedbackStringContainer obligationsFeedback;

    public MtdVatObligationsForm(String id, SessionUtils sessionUtils) {
      super(id);
      this.sessionUtils = sessionUtils;

      setDefaultModel(new CompoundPropertyModel(this));

      TextField vrn = new TextField("vrn");
      FormFieldValidator formFieldValidator = new FormFieldValidator("VRN");
      vrn.add(formFieldValidator);
      add(vrn);
      FeedbackListContainer vrnFeedback = new FeedbackListContainer("vrnFeedback");
      vrnFeedback.setFilter(new ComponentFeedbackMessageFilter(vrn));
      add(vrnFeedback);

      List<String> dateRange = Arrays.asList("12 months", "2 years");
      add(new DropDownChoice<String>("dateRange", new Model(), dateRange));
      List<String> status = Arrays.asList("Fulfilled", "Open");
      add(new DropDownChoice<String>("status", new Model(), status));

      HiddenField hmrcHeaders = new HiddenField("hmrcHeaders");
      add(hmrcHeaders);

      obligationsFeedback = new FeedbackStringContainer("obligationsFeedback");
      add(obligationsFeedback);
    }
  }
}
