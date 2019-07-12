package net.opentechnology.triki.mtd.pages;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.opentechnology.triki.auth.components.FeedbackListContainer;
import net.opentechnology.triki.auth.components.FeedbackStringContainer;
import net.opentechnology.triki.auth.resources.SessionUtils;
import net.opentechnology.triki.mtd.enums.DateRange;
import net.opentechnology.triki.mtd.enums.DateRange.DateRanges;
import net.opentechnology.triki.mtd.security.HmrcIdentityProvider;
import net.opentechnology.triki.mtd.validators.FormFieldValidator;
import net.opentechnology.triki.mtd.enums.VatObligationStatus;
import net.opentechnology.triki.mtd.vatapi.client.HmrcClientUtils;
import net.opentechnology.triki.mtd.vatapi.client.HmrcVatClient;
import net.opentechnology.triki.mtd.vatapi.dto.VatObligations;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import retrofit2.Call;
import retrofit2.Response;

public class MtdVatObligations extends MtdVatManage {

  @SpringBean
  private SessionUtils sessionUtils;

  @SpringBean
  private HmrcClientUtils hmrcVatClient;

  public MtdVatObligations() {
    add(new MtdVatObligationsForm("mtdVatObligationsForm", sessionUtils, hmrcVatClient));
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
  }

  private class MtdVatObligationsForm extends Form<MtdVatObligationsForm> {

    private SessionUtils sessionUtils;
    private HmrcClientUtils hmrcClientUtils;
    private String vrn;
    private String dateRange = DateRanges.THIS_YEAR_AND_LAST.getLabel();
    private String status = "All";
    private String hmrcHeaders;
    private FeedbackStringContainer obligationsFeedback;

    public MtdVatObligationsForm(String id, SessionUtils sessionUtils, HmrcClientUtils hmrcClientUtils) {
      super(id);
      this.sessionUtils = sessionUtils;
      this.hmrcClientUtils = hmrcClientUtils;

      setDefaultModel(new CompoundPropertyModel(this));

      TextField vrn = new TextField("vrn");
      FormFieldValidator formFieldValidator = new FormFieldValidator("VRN");
      vrn.add(formFieldValidator);
      add(vrn);
      FeedbackListContainer vrnFeedback = new FeedbackListContainer("vrnFeedback");
      vrnFeedback.setFilter(new ComponentFeedbackMessageFilter(vrn));
      add(vrnFeedback);

      List<String> dateRange = Arrays.stream(DateRanges.values()).map(DateRanges::getLabel).collect(Collectors.toList());
      add(new DropDownChoice<String>("dateRange", new PropertyModel(this, "dateRange"), dateRange));
      List<String> statuses = Arrays.stream(VatObligationStatus.values())
                                  .map(VatObligationStatus::getDescription)
                                  .collect(Collectors.toList());
      statuses.add("All");
      add(new DropDownChoice<String>("status", new PropertyModel(this, "status"), statuses));

      HiddenField hmrcHeaders = new HiddenField("hmrcHeaders");
      add(hmrcHeaders);

      obligationsFeedback = new FeedbackStringContainer("obligationsFeedback");
      add(obligationsFeedback);
    }

    @Override
    protected void onSubmit() {
      super.onSubmit();

      String accessToken = sessionUtils.getModuleToken(HmrcIdentityProvider.HMRC_TOKEN);
      try {
        Map<String, String> headers = new HashMap<>();
//      @FormParam("test_scenario") String scenario
//        if (hmrcHeaders) {
//          headers = slurper.parseText(hmrcHeaders)
//        }
        // Put in headers too
        headers.put("Gov-Client-Connection-Method", "WEB_APP_VIA_SERVER");


        DateRange dateRangeValid = DateRange.getDateRange(dateRange);
        HmrcVatClient hmrcVatClient = hmrcClientUtils.buildAuthBearerServiceClient(accessToken);
        Call<VatObligations> callable = hmrcVatClient.getObligations(vrn,
            DateRange.format(dateRangeValid.getStart()), DateRange.format(dateRangeValid.getEnd()),
            VatObligationStatus.getVatObligationStatus(status).name(), headers, "");
        Response<VatObligations> vatObligationsResponse = callable.execute();


      } catch (Exception e) {
        obligationsFeedback.setMsg("Problems calling HMRC");
      }
    }

  }
}
